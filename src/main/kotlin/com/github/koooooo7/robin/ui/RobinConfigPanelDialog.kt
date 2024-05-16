package com.github.koooooo7.robin.ui;

import com.github.koooooo7.robin.db.Ringer
import com.github.koooooo7.robin.db.RingerSettings
import com.github.koooooo7.robin.db.WeekDay
import com.intellij.icons.AllIcons.General
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import org.apache.commons.collections.functors.MapTransformer
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.swing.*


class RobinConfigPanelDialog(project: Project?) : DialogWrapper(project) {

    private val uidKey = "_UID";
    private val ringerSettings: RingerSettings = RingerSettings.INSTANCE
    private val runtimeRingerSettings = ConcurrentHashMap<String, Ringer>()
    private val daysOfWeek = WeekDay.values().map { it.abbreviation }
    private val defaultRinger = Ringer()
    private val mainPanel: JPanel
    private val runtimeRingersPanel: JPanel
    private var addButton: JButton
    private var resetButton: JButton

    init {
        val persistedRingers = ringerSettings.getRingers()
        if (persistedRingers.isNotEmpty()) {
            runtimeRingerSettings.clear()
            runtimeRingerSettings.putAll(ringerSettings.getRingers())
        }
        mainPanel = JPanel()
        runtimeRingersPanel = JPanel()
        addButton = JButton("Add", General.Add)
        resetButton = JButton("Clear All")
        init()
    }

    override fun createTitlePane(): JComponent {
        return JLabel("\uD83D\uDC24 Hello Robin ᡣ\uD802\uDF69  ")
    }

    override fun createCenterPanel(): JPanel {
        runtimeRingersPanel.layout = BoxLayout(runtimeRingersPanel, BoxLayout.Y_AXIS)
        if (runtimeRingerSettings.isEmpty()) {
            refreshRuntimeRingersPanel(mapOf(genUID() to defaultRinger))
        } else {
            refreshRuntimeRingersPanel(runtimeRingerSettings)
        }

        mainPanel.add(runtimeRingersPanel)

        return mainPanel
    }

    override fun createButtonsPanel(buttons: MutableList<out JButton>): JPanel {
        val btmPanel = JPanel()
        btmPanel.add(addButton)
        btmPanel.add(resetButton)
        addButton.addActionListener {
            val uid = genUID()
            runtimeRingerSettings[uid] = defaultRinger
            runtimeRingersPanel.add(genSingleRingerPanelWithDescription(uid, defaultRinger), 0)
            runtimeRingersPanel.revalidate()
            runtimeRingersPanel.repaint()
        }

        resetButton.addActionListener {
            runtimeRingerSettings.clear()
            refreshRuntimeRingersPanel(mapOf(genUID() to defaultRinger))
        }

        for (button in buttons) {
            btmPanel.add(button)
        }
        return btmPanel
    }

    override fun doOKAction() {
        // refresh
        runtimeRingerSettings.clear()
        for (i in 0 until runtimeRingersPanel.componentCount) {
            val ringer = Ringer()
            val singleRingerPanelWithDescription = runtimeRingersPanel.getComponent(i) as JPanel
            val singleRingerPanel = singleRingerPanelWithDescription.components[0] as JPanel

            val ringerPanel = singleRingerPanel.components[0] as JPanel
            // 【0】 【2】 JLabel => Time xx : xx
            ringer.selectedHour = (ringerPanel.components[1] as JComboBox<*>).selectedItem?.toString() ?: "00"
            ringer.selectedMinute = (ringerPanel.components[3] as JComboBox<*>).selectedItem?.toString() ?: "00"
            val descriptionAndRemove = singleRingerPanelWithDescription.components[1] as JPanel
            ringer.description = ((descriptionAndRemove.components[0] as JTextField).text ?: "").take(80)

            val weekSelected = booleanArrayOf(false, false, false, false, false, false, false)
            val weekPanel = singleRingerPanel.components[1] as JPanel


            for (ii in 0..6) {
                val component = weekPanel.components[ii]
                if (component is JCheckBox) {
                    weekSelected[ii] = component.isSelected
                }
            }

            ringer.weekend = weekSelected.toList()
            runtimeRingerSettings[genUID()] = ringer
        }

        ringerSettings.refreshRingers(distinctRuntimeRingerSettings())
        super.doOKAction()
    }

    private fun genSingleRingerPanelWithDescription(ringerUID: String, ringerData: Ringer): JPanel {
        val singleRingerPanel = JPanel()
        singleRingerPanel.layout = BoxLayout(singleRingerPanel, BoxLayout.Y_AXIS)

        val ringer = JPanel()
        ringer.layout = BoxLayout(ringer, BoxLayout.X_AXIS)
        ringer.add(genComboBoxes(ringerData.selectedHour.toInt(), ringerData.selectedMinute.toInt()))
        ringer.add(genWeekendJPanel(ringerData.weekend.toBooleanArray()))

        singleRingerPanel.add(ringer)
        val descAndRemovePanel = JPanel()
        descAndRemovePanel.layout = BoxLayout(descAndRemovePanel, BoxLayout.LINE_AXIS)

        val removeBtn = JButton(General.Remove)
        removeBtn.putClientProperty(uidKey, ringerUID);
        removeBtn.addActionListener {
            val needRemoveRingerUID = removeBtn.getClientProperty(uidKey);
            runtimeRingerSettings.remove(needRemoveRingerUID)
            refreshRuntimeRingersPanel(runtimeRingerSettings)
        }

        descAndRemovePanel.add(JTextField(ringerData.description))
        descAndRemovePanel.add(removeBtn)
        singleRingerPanel.add(descAndRemovePanel)

        return singleRingerPanel
    }

    private fun genWeekendJPanel(checked: BooleanArray): JPanel {
        val weekend = JPanel()
        for (i in 0..6) {
            val day = JCheckBox(daysOfWeek[i], checked[i]);
            weekend.add(day)
        }

        return weekend
    }


    private fun genComboBoxes(hour: Int = 0, min: Int = 0): JPanel {
        val timePanel = JPanel()
        val hourComboBox = JComboBox<String>()
        val minuteComboBox = JComboBox<String>()
        for (i in 0..23) {
            hourComboBox.addItem(String.format("%02d", i))
        }
        for (i in 0..59) {
            minuteComboBox.addItem(String.format("%02d", i))
        }

        hourComboBox.selectedIndex = hour
        minuteComboBox.selectedIndex = min
        timePanel.add(JLabel("Time:"))
        timePanel.add(hourComboBox)
        timePanel.add(JLabel(":"))
        timePanel.add(minuteComboBox)
        return timePanel
    }

    private fun genUID(): String {
        return UUID.randomUUID().toString();
    }

    private fun refreshRuntimeRingersPanel(ringerSettings: Map<String, Ringer>) {
        runtimeRingersPanel.removeAll();
        for ((uid, ringerData) in ringerSettings) {
            runtimeRingersPanel.add(genSingleRingerPanelWithDescription(uid, ringerData))
        }
        runtimeRingersPanel.revalidate()
        runtimeRingersPanel.repaint()
    }

    private fun distinctRuntimeRingerSettings(): Map<String, Ringer> {
        return runtimeRingerSettings.values.distinctBy {
            it.selectedHour + "-" + it.selectedMinute + "-" + it.weekend.joinToString("-") { d -> if (d) "1" else "0" }
        }.associateBy { genUID() }
    }
}
