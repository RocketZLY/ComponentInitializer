package rocketzly.componentinitializer

import android.app.Application
import android.util.Log
import rocketzly.componentinitializer.annotation.Init
import rocketzly.componentinitializer.annotation.ThreadMode

/**
 * Created by rocketzly on 2019/7/24.
 */

class AppInit {

    @Init(priority = 3, thread = ThreadMode.MAIN)
    fun sync3() {
        Log.i("zhuliyuan", "sync3")
    }

    @Init(priority = 1, thread = ThreadMode.MAIN)
    fun sync1(application: Application) {
        Log.i("zhuliyuan", "sync1")
    }

    @Init(priority = 1, thread = ThreadMode.BACKGROUND)
    fun async1(application: Application) {
        Log.i("zhuliyuan", "async1")
    }

    @Init(priority = 100, thread = ThreadMode.BACKGROUND)
    fun async100(application: Application) {
        Log.i("zhuliyuan", "async100")
    }

    @Init(priority = 10, thread = ThreadMode.MAIN)
    fun sync10(application: Application) {
        Log.i("zhuliyuan", "sync10")
    }

    @Init(priority = 30, thread = ThreadMode.BACKGROUND)
    fun async30(application: Application) {
        Log.i("zhuliyuan", "async30")
    }
}