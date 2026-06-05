package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.ElevatorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ElevatorApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ElevatorRepository(database.elevatorDao()) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            repository.prePopulateIfEmpty()
        }
    }
}
