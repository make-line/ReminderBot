package tinkoff.reminderbot.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun getFullPattern(s: String,isConst:Boolean): String {
    var sb = StringBuffer(s)
    if (s.length == 5) {

        sb.insert(0, "" +getCorrectNumber(LocalDateTime.now().dayOfMonth.toString()) + "." + getCorrectNumber(LocalDateTime.now().monthValue.toString()) + "." + LocalDateTime.now().year + " ")

    }
    else if (s.length == 11) {
        sb.insert(5, "." + LocalDateTime.now().year)
    }

    if(LocalDateTime.parse(sb, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")).isBefore(LocalDateTime.now()) && !isConst) {

        return "error"
    }
    return sb.toString()
}

fun getCorrectNumber(s: String):String{
    var st:String = if (s.toInt() >= 10) {
       s
    } else
        "0$s"
    return st
}


