package com.example.notemd

import android.app.Application
import com.example.notemd.data.NoteRepository
import com.example.notemd.data.local.NoteDatabase
import com.example.notemd.data.local.NoteFileStore

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
}

private class DefaultAppContainer(application: Application) : AppContainer {

    private val database by lazy { NoteDatabase.getDatabase(application) }
    private val noteFileStore by lazy { NoteFileStore(application) }

    override val noteRepository: NoteRepository by lazy {
        NoteRepository(
            noteDao = database.noteDao(),
            noteFileStore = noteFileStore
        )
    }
}
