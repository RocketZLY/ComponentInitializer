# ComponentInitializer

这是一个帮助组件初始化的库，致力解决组件化后每个模块初始化逻辑多处调用、顺序控制等难以维护的问题，使用了该库后初始化逻辑只需拆分到组件自己的module中，调用线程和顺序控制交由库来帮你完成。

## 依赖和配置

| 模块     | componentinitializer-api | componentinitializer-compiler |
| :------- | ----------- | ----------- |
| 最新版本 | [![download](https://img.shields.io/badge/download-1.0.1-blue)](https://bintray.com/beta/#/zhuliyuan/maven/componentinitializer-api) | [![download](https://img.shields.io/badge/download-1.0.1-blue)](https://bintray.com/beta/#/zhuliyuan/maven/componentinitializer-compiler) |

```groovy
android {
    defaultConfig {
        ...
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [INITIALIZER_MODULE_NAME: project.getName()]
            }
        }
    }
}

dependencies {
    implementation 'com.rocketzly:componentinitializer-api:x.x.x'
    annotationProcessor 'com.rocketzly:componentinitializer-compiler:x.x.x'
}
```

## 使用

1. 新建一个类用来做模块初始化逻辑，类名可以自己定义，**需要有空参构造方法**，本例中叫ModuleAInit
2. 在该类中新增要进行初始化逻辑的方法，方法名可以自己定义，**参数目前支持一个参数类型为Application和无参两种情况，并需要在方法上添加@Init注解**

@init注解提供两个属性

- priority：优先级配置，越大优先级越高，默认为1
- thread：运行线程配置，有MAIN, BACKGROUND可选，默认MAIN

```java
public class ModuleAInit {

    @Init(priority = 10, thread = ThreadMode.MAIN)
    public void sync10(Application application) {
    }

    @Init(priority = 30, thread = ThreadMode.BACKGROUND)
    public void async30(Application application) {
    }
}
```

3. 在Application中触发初始化逻辑，我是直接写在了BaseApplication中

```java
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ComponentInitializer.builder()
                .debug(true)
                .start(this);
    }
}
```

debug为true的时候可以通过ComponentInitializer标签查看日志信息

![](http://rocketzly.androider.top/init_sucess.png)





