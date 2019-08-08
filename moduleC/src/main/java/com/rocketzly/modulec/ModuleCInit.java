package com.rocketzly.modulec;

import android.app.Application;
import rocketzly.componentinitializer.annotation.Init;
import rocketzly.componentinitializer.annotation.ThreadMode;

/**
 * Created by rocketzly on 2019/8/5.
 */
public class ModuleCInit {

        @Init(priority = 3, thread = ThreadMode.MAIN)
        public void sync3(Application application) {
        }

        @Init(priority = 100, thread = ThreadMode.BACKGROUND)
        public void async100(Application application) {
        }
}
