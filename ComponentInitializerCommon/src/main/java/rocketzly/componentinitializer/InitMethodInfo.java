package rocketzly.componentinitializer;

import rocketzly.componentinitializer.annotation.ThreadMode;

/**
 * 初始化的方法信息
 * Created by rocketzly on 2019/7/17.
 */
public class InitMethodInfo implements Comparable<InitMethodInfo> {

    public InitMethodInfo(String className, String methodName, boolean isParams, int priority, ThreadMode thread) {
        this.className = className;
        this.methodName = methodName;
        this.isParams = isParams;
        this.priority = priority;
        this.thread = thread;
    }

    //该方法所在类名
    public String className;
    //方法名
    public String methodName;
    //方法是否有参数，如果有参数的话则为Application（有参数的话默认只支持一个参数为Application这种情况）
    public boolean isParams;
    //优先级
    public int priority;
    //调用线程
    public ThreadMode thread;

    @Override
    public int compareTo(InitMethodInfo o) {
        return this.priority - o.priority;
    }

    @Override
    public String toString() {
        return "InitMethodInfo{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", isParams=" + isParams +
                ", priority=" + priority +
                ", thread=" + thread +
                '}';
    }
}
