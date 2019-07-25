package rocketzly.componentinitializer;

import java.util.List;

/**
 * 初始化方法容器接口
 * Created by rocketzly on 2019/7/18.
 */
public interface IInitContainer {

    /**
     * 获取同步初始化方法 ps：已经按优先级排序
     *
     * @return
     */
    List<InitMethodInfo> getSyncInitMethodList();

    /**
     * 获取异步初始化方法 ps：已经按优先级排序
     *
     * @return
     */
    List<InitMethodInfo> getAsyncInitMethodList();


}
