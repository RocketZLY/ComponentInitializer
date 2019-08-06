package rocketzly.componentinitializer

import android.app.Application
import rocketzly.componentinitializer.api.ComponentInitializer

/**
 * Created by rocketzly on 2019/7/17.
 */
open class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ComponentInitializer.builder()
//            .inject(ComponentInitializerHelper_())
            .debug(true)
            .start(this)
    }
}