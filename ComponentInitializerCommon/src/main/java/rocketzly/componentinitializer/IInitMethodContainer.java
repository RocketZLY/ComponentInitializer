package rocketzly.componentinitializer;

import java.util.List;

/**
 * 初始化方法容器接口 用来获取需要初始化的方法信息
 * Created by rocketzly on 2019/7/18.
 */
public interface IInitMethodContainer {

    /**
     * 获取同步初始化方法
     *
     * @return
     */
    List<InitMethodInfo> getSyncInitMethodList();

    /**
     * 获取异步初始化方法
     *
     * @return
     */
    List<InitMethodInfo> getAsyncInitMethodList();


}
