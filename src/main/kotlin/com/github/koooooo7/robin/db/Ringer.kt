package com.github.koooooo7.robin.db

data class Ringer(
    var selectedHour: String = "00",
    var selectedMinute: String = "00",
    var weekend: List<Boolean> = listOf(false, false, false, false, false, false, false),
    var description: String = ""
)
