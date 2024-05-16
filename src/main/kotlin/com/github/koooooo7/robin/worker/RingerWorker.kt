package com.github.koooooo7.robin.worker

import com.github.koooooo7.robin.db.RingerSettings
import com.github.koooooo7.robin.db.WeekDay
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.jetbrains.rd.util.ConcurrentHashMap
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*


class RingerWorker {
    private var setup = false

    private var currentDayCacheDay = WeekDay.Mon
    private val runtimeRingedCache = ConcurrentHashMap<Int, Int>()

    fun start(ringerSettings: RingerSettings) {

        val t = Thread {
            while (true) {
                Thread.sleep(10_000)
                val ringers = ringerSettings.getRingers().values
                val today = LocalDate.now()
                val currentDay = today.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                if (currentDayCacheDay != WeekDay.valueOf(currentDay)) {
                    currentDayCacheDay = WeekDay.valueOf(currentDay)
                    runtimeRingedCache.clear()
                }

                val currentDateTime = LocalDateTime.now()
                val currentHour = currentDateTime.hour
                val currentMinute = currentDateTime.minute
                for (ringer in ringers) {
                    val selectedHour = ringer.selectedHour
                    val selectedMinute = ringer.selectedMinute
                    val selectedWeekend = ringer.weekend
                    if (!selectedWeekend[WeekDay.valueOf(currentDay).idx]) {
                        continue
                    }

                    if (currentHour != selectedHour.toInt()) {
                        continue
                    }

                    if (currentMinute < selectedMinute.toInt()) {
                        continue
                    }

                    if (setup) {
                        if (currentMinute != selectedMinute.toInt()) {
                            continue
                        }
                    } else {
                        // for setup usage, if the setup within the 1 min of the ringer, popup
                        setup = true
                        if (currentMinute > selectedMinute.toInt() + 1) {
                            continue
                        }
                    }

                    // current time has been trigger, avid to call multi times within 1 min
                    if (runtimeRingedCache.containsKey(currentHour)) {
                        val lastTriggerMin = runtimeRingedCache[currentHour]!!
                        if (currentMinute <= lastTriggerMin) {
                            continue
                        } else {
                            // update
                            runtimeRingedCache[currentHour] = currentMinute
                        }
                    } else {
                        // first cache
                        runtimeRingedCache[currentHour] = currentMinute
                    }

                    val title = "[$currentDay ${String.format("%02d", currentHour)}:${String.format("%02d", currentMinute)}] Robin ring :>"

                    ApplicationManager.getApplication().invokeLater {
                        Messages.showMessageDialog(ringer.description, title, Messages.getInformationIcon())
                    }
                }
            }
        }

        t.isDaemon = true
        t.start()

    }


}