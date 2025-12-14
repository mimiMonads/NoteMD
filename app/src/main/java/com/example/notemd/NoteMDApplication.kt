package com.example.notemd

import android.app.Application
import com.example.notemd.data.NoteRepository
import com.example.notemd.data.local.NoteDatabase
import com.example.notemd.data.local.NoteFileStore
import com.example.notemd.data.SettingsRepository
import com.example.notemd.token.TokenSessionManager

class NoteMDApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}

interface AppContainer {
    val noteRepository: NoteRepository
    val settingsRepository: SettingsRepository
    val tokenSessionManager: TokenSessionManager
}

private class DefaultAppContainer(application: Application) : AppContainer {

    private val database by lazy { NoteDatabase.getDatabase(application) }
    private val noteFileStore by lazy { NoteFileStore(application) }
    private val settingsRepositoryInternal by lazy { SettingsRepository(application) }
    private val tokenSessionManagerInternal by lazy { TokenSessionManager() }

    override val noteRepository: NoteRepository by lazy {
        NoteRepository(
            noteDao = database.noteDao(),
            noteFileStore = noteFileStore
        )
    }

    override val settingsRepository: SettingsRepository
        get() = settingsRepositoryInternal

    override val tokenSessionManager: TokenSessionManager
        get() = tokenSessionManagerInternal
}
