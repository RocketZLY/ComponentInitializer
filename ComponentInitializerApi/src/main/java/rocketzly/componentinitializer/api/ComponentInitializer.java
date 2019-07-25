package rocketzly.componentinitializer.api;

import android.app.Application;
import rocketzly.componentinitializer.IInitContainer;
import rocketzly.componentinitializer.InitConstant;
import rocketzly.componentinitializer.InitMethodInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by rocketzly on 2019/7/23.
 */
public class ComponentInitializer {

    private boolean isDebug;
    private ExecutorService executor;
    private ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>(16);

    public ComponentInitializer(boolean isDebug, ExecutorService executor) {
        this.isDebug = isDebug;
        this.executor = executor == null ? Executors.newSingleThreadExecutor() : executor;
        Logger.setDebug(isDebug);
    }

    @SuppressWarnings("unchecked")
    public void start(Application application) {
        if (application == null) {
            Logger.e("Application不能为null");
            return;
        }
        Class<IInitContainer> clazz = null;
        IInitContainer initContainer = null;
        try {
            clazz = (Class<IInitContainer>) application.getClassLoader().loadClass(InitConstant.GENERATE_PACKAGE_NAME + "." + InitConstant.GENERATE_CLASS_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (clazz == null) {
            Logger.e(InitConstant.GENERATE_CLASS_NAME + "加载失败！！");
            return;
        }

        try {
            initContainer = clazz.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        if (initContainer == null) {
            Logger.e("IInitContainer实例化失败");
            return;
        }
        execute(application, initContainer);
    }

    private void execute(final Application application, final IInitContainer initContainer) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                execute(application, initContainer.getAsyncInitMethodList());
                executor.shutdown();
            }
        });
        execute(application, initContainer.getSyncInitMethodList());
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
                Logger.i(methodInfo.className + "#" + methodInfo.methodName + "()调用成功");
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
        private ExecutorService executor;

        public Builder debug(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        public Builder executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public void start(Application application) {
            new ComponentInitializer(isDebug, executor).start(application);
        }
    }
}
