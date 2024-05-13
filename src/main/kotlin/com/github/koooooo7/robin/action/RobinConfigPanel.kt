package com.github.koooooo7.robin.action

import com.github.koooooo7.robin.ui.RobinConfigPanelDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class RobinConfigPanel : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val dialog = RobinConfigPanelDialog(e.project)
        dialog.show()
    }
}