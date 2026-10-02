package com.scootah.app

import android.app.Application
import com.scootah.app.data.repository.UserRepository
import org.osmdroid.config.Configuration
import java.io.File

class ScootahApp : Application() {

    lateinit var userRepository: UserRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // OSMDroid: use internal cache dir — no WRITE_EXTERNAL_STORAGE needed on API 29+
        Configuration.getInstance().apply {
            load(this@ScootahApp, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
            osmdroidTileCache = File(cacheDir, "osmdroid")
        }

        userRepository = UserRepository.getInstance(this)
    }
}
