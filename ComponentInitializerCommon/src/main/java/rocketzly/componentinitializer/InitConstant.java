package rocketzly.componentinitializer;

/**
 * Created by rocketzly on 2019/7/18.
 */
public interface InitConstant {
    /**
     * 生成类的名称
     */
    String GENERATE_CLASS_NAME = "$$ComponentInitializerHelper";
    /**
     * 生成类的包名
     */
    String GENERATE_PACKAGE_NAME = "rocketzly.componentinitializer";
    /**
     * 生成同步list名
     */
    String GENERATE_SYNC_LIST_NAME = "syncList";
    /**
     * 生成异步list名
     */
    String GENERATE_ASYNC_LIST_NAME = "asyncList";
    /**
     * 获取同步初始化方法的方法名
     */
    String GENERATE_GET_SYNC_METHOD_NAME = "getSyncInitMethodList";
    /**
     * 获取异步初始化方法的方法名
     */
    String GENERATE_GET_ASYNC_METHOD_NAME = "getAsyncInitMethodList";
}
