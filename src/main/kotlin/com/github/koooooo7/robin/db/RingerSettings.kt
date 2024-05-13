package com.github.koooooo7.robin.db

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.annotations.MapAnnotation
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@State(name = "RingerSettings", storages = [Storage("Robin_ringer_mapsettings.xml")])
class RingerSettings : PersistentStateComponent<RingerSettings.Ringers> {

    companion object {
        val INSTANCE: RingerSettings = ApplicationManager.getApplication().getService(RingerSettings::class.java);
    }

    data class Ringers(
        @MapAnnotation var ringers: MutableMap<String, Ringer> = ConcurrentHashMap()
    )

    private var state = Ringers()

    override fun getState(): Ringers {
        return state
    }

    override fun loadState(state: Ringers) {
        this.state = state
    }

    fun getRingers(): List<Ringer> {
        return state.ringers.values.toList()
    }

    fun refreshRingers(ringers: List<Ringer>) {
        state.ringers.clear()
        for (ringer in ringers) {
            state.ringers[UUID.randomUUID().toString()] = ringer
        }

    }
}

