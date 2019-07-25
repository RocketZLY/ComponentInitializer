package rocketzly.componentinitializer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要初始化的方法
 * Created by rocketzly on 2019/7/17.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Init {

    /**
     * 初始化优先级，越大优先级越大
     *
     * @return
     */
    int priority() default 1;

    /**
     * 初始化线程，默认主线程
     *
     * @return
     */
    ThreadMode thread() default ThreadMode.MAIN;
}
