package rocketzly.componentinitializer;

/**
 * Created by rocketzly on 2019/7/18.
 */
public interface InitConstant {
    /**
     * 配置参数的key
     */
    String INITIALIZER_MODULE_NAME = "INITIALIZER_MODULE_NAME";
    /**
     * 支持注解的全类名
     */
    String SUPPORT_ANNOTATION_QUALIFIED_NAME = "rocketzly.componentinitializer.annotation.Init";
    /**
     * 生成类名前缀
     */
    String GENERATE_CLASS_NAME_PREFIX = "InitializerContainer_";
    /**
     * 生成类的包名
     */
    String GENERATE_PACKAGE_NAME = "rocketzly.componentinitializer.generate";
    /**
     * 生成同步list名
     */
    String GENERATE_FIELD_SYNC_LIST = "syncList";
    /**
     * 生成异步list名
     */
    String GENERATE_FIELD_ASYNC_LIST = "asyncList";
    /**
     * 获取同步初始化方法的方法名
     */
    String GENERATE_METHOD_GET_SYNC_NAME = "getSyncInitMethodList";
    /**
     * 获取异步初始化方法的方法名
     */
    String GENERATE_METHOD_GET_ASYNC_NAME = "getAsyncInitMethodList";
    /**
     * log
     */
    String NO_MODULE_NAME_TIPS = "These no module name, at 'build.gradle', like :\n" +
            "android {\n" +
            "    defaultConfig {\n" +
            "        ...\n" +
            "        javaCompileOptions {\n" +
            "            annotationProcessorOptions {\n" +
            "                arguments = [INITIALIZER_MODULE_NAME: project.name]\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

}
