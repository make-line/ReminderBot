package tinkoff.reminderbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import tinkoff.reminderbot.model.Reminder
import java.time.LocalDateTime

interface ReminderRepository:JpaRepository<Reminder,Long> {
    fun findAllByChatId(chatId:Long):MutableList<Reminder>
    fun findAllByRemindTime(time: LocalDateTime): MutableList<Reminder>
}