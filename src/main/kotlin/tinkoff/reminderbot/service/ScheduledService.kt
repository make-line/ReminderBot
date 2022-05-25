package tinkoff.reminderbot.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.time.LocalDateTime

@Component
class ScheduledService(val reminderBot: ReminderBot) {

    @Scheduled(cron = "*/60 * * * * *")
    fun reportCurrentTime() {
        reminderBot.botService.getAllRemindersNow()!!.forEach {
            reminderBot.sendReminder(it.chatId!!, it.id!!)
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    fun deleteOldNotify() {
        reminderBot.botService.getAllReminders()!!.forEach {
            if (it.remindTime != null)
                if (it.remindTime!!.isBefore(LocalDateTime.now().minusDays(1))) {
                    reminderBot.botService.reminderRepository.delete(it)
                }
        }
    }
}

