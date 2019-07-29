package rocketzly.componentinitializer;

/**
 * Created by rocketzly on 2019/7/18.
 */
public interface InitConstant {
    /**
     * 生成类的名称
     */
    String GENERATE_CLASS_NAME = "_ComponentInitializerHelper";
    /**
     * 生成类的包名
     */
    String GENERATE_PACKAGE_NAME = "rocketzly.componentinitializer";
    /**
     * 生成debug变量名
     */
    String GENERATE_FIELD_ISDEBUG = "isDebug";
    /**
     * 生成初始化调用类方法名
     */
    String GENERATE_METHOD_INIT = "init";
    /**
     * 生成具体执行逻辑的方法名
     */
    String GENERATE_METHOD_EXECUTE = "execute";
    /**
     * 生成需要实现的接口方法名
     */
    String GENERATE_METHOD_START = "start";
    /**
     * 生成application变量名
     */
    String GENERATE_VARIABLE_APPLICATION = "application";

}
