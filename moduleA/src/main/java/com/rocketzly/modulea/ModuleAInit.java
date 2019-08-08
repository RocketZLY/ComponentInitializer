package com.rocketzly.modulea;

import android.app.Application;
import rocketzly.componentinitializer.annotation.Init;
import rocketzly.componentinitializer.annotation.ThreadMode;

/**
 * Created by rocketzly on 2019/8/5.
 */
public class ModuleAInit {

    @Init(priority = 10, thread = ThreadMode.MAIN)
    public void sync10(Application application) {
    }

    @Init(priority = 30, thread = ThreadMode.BACKGROUND)
    public void async30(Application application) {
    }
}
