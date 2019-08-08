package com.rocketzly.common;

import android.app.Application;
import rocketzly.componentinitializer.api.ComponentInitializer;

/**
 * Created by rocketzly on 2019/8/8.
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ComponentInitializer.builder()
                .debug(true)
                .start(this);
    }
}
