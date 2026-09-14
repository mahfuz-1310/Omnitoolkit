package com.example

import android.app.Application
import android.os.StrictMode
import androidx.room.Room
import com.example.data.datastore.SettingsRepository
import com.example.data.datastore.dataStore
import com.example.data.db.AppDatabase
import com.example.data.repository.FavoriteRepository
import com.example.data.repository.HistoryRepository
import com.example.feature.common.startup.StartupDefer

class NameGenApplication : Application() {
    
    lateinit var database: AppDatabase
        private set
        
    lateinit var historyRepository: HistoryRepository
        private set
        
    lateinit var favoriteRepository: FavoriteRepository
        private set
        
    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var sharedViewModel: com.example.ui.MainViewModel
        private set

    val appManagerViewModel: com.example.ui.AppManagerViewModel by lazy {
        com.example.ui.AppManagerViewModel(this, database.appHistoryDao())
    }

    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    .penaltyLog()
                    .build()
            )
        }
        
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "namegen_database"
        ).fallbackToDestructiveMigration().build()
        
        historyRepository = HistoryRepository(database.historyDao())
        favoriteRepository = FavoriteRepository(database.favoriteDao())
        settingsRepository = SettingsRepository(dataStore)
        sharedViewModel = com.example.ui.MainViewModel(historyRepository, favoriteRepository, settingsRepository)

        // Defer audio engine synthesis and hardware AudioTrack allocations to run strictly post-first-frame
        StartupDefer.schedulePostFirstFrame(this) {
            com.example.utils.SoundEffectManager.init(this@NameGenApplication)
        }
    }
}
