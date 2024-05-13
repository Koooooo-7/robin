package com.github.koooooo7.robin.ui;

import com.github.koooooo7.robin.db.Ringer
import com.github.koooooo7.robin.db.RingerSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.util.concurrent.CopyOnWriteArrayList
import javax.swing.*


class RobinConfigPanelDialog(project: Project?) : DialogWrapper(project) {

    private val ringerSettings: RingerSettings = RingerSettings.INSTANCE
    private val runtimeRingerSettings = CopyOnWriteArrayList<Ringer>()
    private val daysOfWeek = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val defaultRinger = Ringer()
    private val mainPanel: JPanel
    private val runtimeRingersPanel: JPanel
    private var addButton: JButton
    private var resetButton: JButton

    init {
        val persistedRingers = ringerSettings.getRingers()
        if (persistedRingers.isNotEmpty()) {
            runtimeRingerSettings.clear()
            runtimeRingerSettings.addAll(ringerSettings.getRingers())
        }
        mainPanel = JPanel()
        runtimeRingersPanel = JPanel()
        addButton = JButton("Add +")
        resetButton = JButton("Reset All")
        init()
    }

    override fun createTitlePane(): JComponent {
        return JLabel("\uD83D\uDC24 Hello Robin ᡣ\uD802\uDF69  ")
    }

    override fun createCenterPanel(): JPanel {
        runtimeRingersPanel.layout = BoxLayout(runtimeRingersPanel, BoxLayout.Y_AXIS)
        if (runtimeRingerSettings.isEmpty()) {
            runtimeRingersPanel.add(genSingleRingerPanelWithDescription(defaultRinger))
        } else {
            for (ringerData in runtimeRingerSettings) {
                runtimeRingersPanel.add(genSingleRingerPanelWithDescription(ringerData))
            }
        }

        mainPanel.add(runtimeRingersPanel)

        return mainPanel
    }

    override fun createButtonsPanel(buttons: MutableList<out JButton>): JPanel {
        val btmPanel = JPanel()
        btmPanel.add(addButton)
        btmPanel.add(resetButton)
        addButton.addActionListener {
            runtimeRingersPanel.add(genSingleRingerPanelWithDescription(defaultRinger))
            runtimeRingersPanel.revalidate()
            runtimeRingersPanel.repaint()
        }

        resetButton.addActionListener {
            runtimeRingerSettings.clear()
            runtimeRingersPanel.removeAll()
            mainPanel.add(runtimeRingersPanel)
            runtimeRingersPanel.repaint()
            mainPanel.repaint()
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
            ringer.description = ((singleRingerPanelWithDescription.components[1] as JTextField).text ?: "").take(80)

            val weekSelected = booleanArrayOf(false, false, false, false, false, false, false)
            val weekPanel = singleRingerPanel.components[1] as JPanel


            for (ii in 0..6) {
                val component = weekPanel.components[ii]
                if (component is JCheckBox) {
                    weekSelected[ii] = component.isSelected
                }
            }

            // exclude not selected weekend data
            if (weekSelected.all { false }) {
                continue
            }

            ringer.weekend = weekSelected.toList()
            runtimeRingerSettings.add(ringer)
        }

        // do validate
        ringerSettings.refreshRingers(runtimeRingerSettings)
        super.doOKAction()
    }

    private fun genSingleRingerPanelWithDescription(ringerData: Ringer): JPanel {
        val singleRingerPanel = JPanel()
        singleRingerPanel.layout = BoxLayout(singleRingerPanel, BoxLayout.Y_AXIS)

        val ringer = JPanel()
        ringer.layout = BoxLayout(ringer, BoxLayout.X_AXIS)
        ringer.add(genComboBoxes(ringerData.selectedHour.toInt(), ringerData.selectedMinute.toInt()))
        ringer.add(genWeekendJPanel(ringerData.weekend.toBooleanArray()))

        singleRingerPanel.add(ringer)
        singleRingerPanel.add(JTextField(ringerData.description))

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
}
