package rocketzly.componentinitializer

import android.app.Application
import rocketzly.componentinitializer.annotation.Init
import rocketzly.componentinitializer.annotation.ThreadMode

/**
 * Created by rocketzly on 2019/7/24.
 */
class AppInit {

    @Init(priority = 3, thread = ThreadMode.MAIN)
    fun sync3(application: Application) {
    }

    @Init(priority = 1, thread = ThreadMode.MAIN)
    fun sync1(application: Application) {
    }

    @Init(priority = 1, thread = ThreadMode.BACKGROUND)
    fun async1(application: Application) {
    }

    @Init(priority = 100, thread = ThreadMode.BACKGROUND)
    fun async100(application: Application) {
    }

    @Init(priority = 10, thread = ThreadMode.MAIN)
    fun sync10(application: Application) {
    }

    @Init(priority = 30, thread = ThreadMode.BACKGROUND)
    fun async30(application: Application) {
    }
}