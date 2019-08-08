package com.rocketzly.moduleb;

import android.app.Application;
import rocketzly.componentinitializer.annotation.Init;
import rocketzly.componentinitializer.annotation.ThreadMode;

/**
 * Created by rocketzly on 2019/8/5.
 */
public class ModuleBInit {

        @Init(priority = 1, thread = ThreadMode.BACKGROUND)
        public void async1(Application application) {
        }

        @Init(priority = 33, thread = ThreadMode.BACKGROUND)
        public void async33(Application application) {
        }
}
