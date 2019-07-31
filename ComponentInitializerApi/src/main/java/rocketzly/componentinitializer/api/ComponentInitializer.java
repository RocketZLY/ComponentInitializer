package rocketzly.componentinitializer.api;

import android.app.Application;
import rocketzly.componentinitializer.IInitializer;
import rocketzly.componentinitializer.InitConstant;


/**
 * Created by rocketzly on 2019/7/23.
 */
public class ComponentInitializer {

    private boolean isDebug;
    private IInitializer initializer;

    public ComponentInitializer(boolean isDebug) {
        this(isDebug, null);
    }

    public ComponentInitializer(boolean isDebug, IInitializer initializer) {
        this.isDebug = isDebug;
        this.initializer = initializer;
        Logger.setDebug(isDebug);
    }

    public void start(Application application) {
        if (application == null) {
            Logger.e("Application不能为null");
            return;
        }
        IInitializer initializer = getInitializer(application.getClassLoader());
        if (initializer == null) {
            return;
        }
        initializer.start(application);
    }

    private IInitializer getInitializer(ClassLoader classLoader) {
        if (this.initializer != null) {
            return this.initializer;
        }
        return loadInitializer(classLoader);
    }

    @SuppressWarnings("unchecked")
    private IInitializer loadInitializer(ClassLoader classLoader) {
        Class<IInitializer> clazz = null;
        IInitializer initializer = null;
        String qualifiedClassName = "";
        try {
            clazz = (Class<IInitializer>) classLoader.loadClass(qualifiedClassName = InitConstant.GENERATE_PACKAGE_NAME + "." + InitConstant.GENERATE_CLASS_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (clazz == null) {
            Logger.e(qualifiedClassName + "加载失败！！");
            return null;
        }

        try {
            initializer = clazz.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        if (initializer == null) {
            Logger.e(qualifiedClassName + "实例化失败");
            return null;
        }
        return initializer;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean isDebug = false;
        private IInitializer initializer;

        public Builder debug(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        public Builder initializer(IInitializer initializer) {
            this.initializer = initializer;
            return this;
        }

        public void start(Application application) {
            new ComponentInitializer(isDebug, initializer).start(application);
        }
    }
}
