package com.rocketzly.moduleb;

import android.app.Application;
import android.util.Log;
import rocketzly.componentinitializer.annotation.Init;
import rocketzly.componentinitializer.annotation.ThreadMode;

/**
 * Created by rocketzly on 2019/8/5.
 */
public class ModuleBInit {

        @Init(priority = 1, thread = ThreadMode.BACKGROUND)
        public void async1(Application application) {
            Log.i("zhuliyuan", "ModuleBInit_async1");
        }

        @Init(priority = 33, thread = ThreadMode.BACKGROUND)
        public void async33(Application application) {
            Log.i("zhuliyuan", "ModuleBInit_async33");
        }
}
