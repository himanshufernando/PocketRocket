package tkhug.project.pocketrocket

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import tkhug.project.pocketrocket.data.db.AppDatabase

class PocketRocketApp : Application() {

    /** Application-level scope – survives across ViewModels */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Lazily initialised Room database (seed triggered on first creation) */
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this, applicationScope)
    }
}

