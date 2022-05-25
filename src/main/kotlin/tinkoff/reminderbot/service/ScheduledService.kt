package tinkoff.reminderbot.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import tinkoff.reminderbot.ReminderBot
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

@Component
class ScheduledService(val reminderBot: ReminderBot) {
    private val log: Logger = LoggerFactory.getLogger(ScheduledService::class.java)

    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    @Scheduled(cron = "*/60 * * * * *")
    fun reportCurrentTime() {
        reminderBot.botService.getAllRemindersNow()!!.forEach {
            reminderBot.sendReminder(it.chatId!!, it.id!!)
//        log.info("The time is now {}", dateFormat.format(Date()))
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
//        log.info("The time is now {}", dateFormat.format(Date()))
    }
}


//    @Scheduled(cron = "0 0 8-10 * * *")
//    fun reportCurrentTime2() {
//            reminderBot.sendReminderN(404079174)
//            reminderBot.sendReminderN(501434120)
////        log.info("The time is now {}", dateFormat.format(Date()))
//        }