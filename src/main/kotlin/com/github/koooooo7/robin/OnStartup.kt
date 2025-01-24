package com.github.koooooo7.robin

import com.github.koooooo7.robin.db.RingerSettings
import com.github.koooooo7.robin.worker.RingerWorker
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity


internal class OnStartup : StartupActivity {

    @Volatile
    private var isScheduled = false
    override fun runActivity(project: Project) {

        if (!isScheduled) {
            RingerWorker.getInstance().start(RingerSettings.getInstance())
            isScheduled = true
        }
    }

}
