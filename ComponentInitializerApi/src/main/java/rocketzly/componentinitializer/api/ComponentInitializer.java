package rocketzly.componentinitializer.api;

import android.app.Application;
import android.content.pm.PackageManager;
import rocketzly.componentinitializer.IInitMethodContainer;
import rocketzly.componentinitializer.InitConstant;
import rocketzly.componentinitializer.InitMethodInfo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by rocketzly on 2019/7/23.
 */
public class ComponentInitializer {

    private boolean isDebug;
    private ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>(16);

    public ComponentInitializer(boolean isDebug) {
        this.isDebug = isDebug;
        Logger.setDebug(isDebug);
    }

    public void start(Application application) {
        if (application == null) {
            Logger.e("Application不能为null");
            return;
        }
        Set<String> fileNameByPackageName = null;
        try {
            fileNameByPackageName = ClassUtils.getFileNameByPackageName(application, InitConstant.GENERATE_PACKAGE_NAME);
            Logger.i("通过包名找到的类:" + fileNameByPackageName.toString());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (fileNameByPackageName == null) {
            Logger.e("未找到初始化方法容器类");
            return;
        }

        List<InitMethodInfo> syncMethodList = new ArrayList<>();
        List<InitMethodInfo> asyncMethodList = new ArrayList<>();
        for (String className : fileNameByPackageName) {
            try {
                IInitMethodContainer initMethodContainer = (IInitMethodContainer) Class.forName(className).newInstance();
                syncMethodList.addAll(initMethodContainer.getSyncInitMethodList());
                asyncMethodList.addAll(initMethodContainer.getAsyncInitMethodList());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(syncMethodList);
        Collections.sort(asyncMethodList);
        Logger.i("同步方法:" + syncMethodList.toString());
        Logger.i("异步方法:" + asyncMethodList.toString());
        execute(application, syncMethodList, asyncMethodList);
    }

    private void execute(final Application application, List<InitMethodInfo> syncMethodList, final List<InitMethodInfo> asyncMethodList) {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                execute(application, asyncMethodList);
                executor.shutdown();
            }
        });
        execute(application, syncMethodList);
    }

    private void execute(Application application, List<InitMethodInfo> list) {
        for (InitMethodInfo methodInfo : list) {
            Object instance = null;
            if (!(map.containsKey(methodInfo.className))) {
                try {
                    instance = Class.forName(methodInfo.className).newInstance();
                    map.put(methodInfo.className, instance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if ((instance = map.get(methodInfo.className)) == null) {
                Logger.e(methodInfo.className + "实例获取失败");
                continue;
            }

            try {
                Method method = instance.getClass().getMethod(methodInfo.methodName,
                        methodInfo.isParams ? new Class<?>[]{Application.class} : new Class<?>[]{});
                method.setAccessible(true);
                method.invoke(instance, methodInfo.isParams ? new Object[]{application} : new Object[]{});
                Logger.i(methodInfo.className + "#" + methodInfo.methodName + "()调用成功，thread:"+Thread.currentThread().getName());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                Logger.e(methodInfo.className + "#" + methodInfo.methodName + "()方法未找到");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Logger.e(methodInfo.className + "#" + methodInfo.methodName + "()方法无法访问");
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                Logger.e(methodInfo.className + "#" + methodInfo.methodName + "()方法调用失败");
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean isDebug = false;

        public Builder debug(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        public void start(Application application) {
            new ComponentInitializer(isDebug).start(application);
        }
    }
}
