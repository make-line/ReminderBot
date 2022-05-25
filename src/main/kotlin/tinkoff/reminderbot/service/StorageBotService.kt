package tinkoff.reminderbot.service

import org.springframework.stereotype.Service
import tinkoff.reminderbot.model.Reminder
import tinkoff.reminderbot.model.UserLevel
import tinkoff.reminderbot.repository.ReminderRepository
import tinkoff.reminderbot.repository.UserLevelRepository
import java.time.LocalDateTime


@Service
class StorageBotService(val reminderRepository: ReminderRepository, val userLevelRepository: UserLevelRepository) {

    fun getUserLevel(chatId: Long) = userLevelRepository.findById(chatId).get().level

    fun getUser(chatId: Long) = userLevelRepository.findById(chatId).get()
    fun addUser(chatId: Long) {
        userLevelRepository.save(UserLevel(chatId, 0))
    }

    fun changeLevelAndTime(chatId: Long, lvl: Int, minutes: Long) {
        var userLevel = userLevelRepository.findById(chatId).get()
        userLevel.level = lvl
        userLevel.repeatEveryMinutes = minutes
        userLevelRepository.save(userLevel)
    }


    fun changeLevel(chatId: Long, lvl: Int) {
        var userLevel = userLevelRepository.findById(chatId).get()
        userLevel.level = lvl
        userLevelRepository.save(userLevel)
    }

    fun changeLevelConst(chatId: Long, lvl: Int) {
        var userLevel = userLevelRepository.findById(chatId).get()
        userLevel.level = lvl
        userLevel.repeatEveryMinutes = 1
        userLevelRepository.save(userLevel)
    }

    fun changeLevelAndCleanAndSave(chatId: Long, lvl: Int) {
        var userLevel = userLevelRepository.findById(chatId).get()
        userLevel.level = lvl
        reminderRepository.save(Reminder(chatId, userLevel.reminder))
        userLevel.reminder = null
        userLevel.repeatEveryMinutes = null
        userLevelRepository.save(userLevel)
    }

    fun changeLevelAndClean(chatId: Long, lvl: Int) {
        var userLevel = userLevelRepository.findById(chatId).get()
        userLevel.level = lvl
        userLevel.reminder = null
        userLevel.repeatEveryMinutes = null
        userLevelRepository.save(userLevel)
    }


    fun changeLevelAndSetReminder(chatId: Long, lvl: Int, reminder: String) {
        var userLevel = userLevelRepository.findById(chatId).get()
        userLevel.level = lvl
        userLevel.reminder = reminder
        userLevelRepository.save(userLevel)

    }

    fun changeLevelAndSetReminderAndSave(chatId: Long, lvl: Int, date: LocalDateTime) {
        var userLevel = userLevelRepository.findById(chatId).get()
        userLevel.level = lvl
        userLevelRepository.save(userLevel)
        reminderRepository.save(Reminder(chatId, userLevel.reminder, date))
    }

    fun changeLevelAndSetReminderAndSaveConst(chatId: Long, lvl: Int, date: LocalDateTime) {
        var userLevel = userLevelRepository.findById(chatId).get()
        reminderRepository.save(Reminder(chatId, userLevel.reminder, date, userLevel.repeatEveryMinutes))
        userLevel.level = lvl
        userLevel.remindTime = null
        userLevel.repeatEveryMinutes = null
        userLevelRepository.save(userLevel)
    }


    fun getAllReminders(): List<Reminder> {
        return reminderRepository.findAll()
    }

    fun deleteById(id: Long) {
        reminderRepository.deleteById(id)
    }


    fun getAllRemindersByUser(chatId: Long): List<Reminder> {
        return reminderRepository.findAllByChatId(chatId)
    }

    fun getReminderMessage(id: Long) = reminderRepository.findById(id).get().reminder

    fun getAllRemindersNow(): List<Reminder> {
        var list = reminderRepository.findAll()
        var list2 = list.toList()
        list.removeAll {
            it.remindTime == null ||
                    it.remindTime!! < LocalDateTime.now().minusSeconds(59) || it.remindTime!! > LocalDateTime.now()
                .plusSeconds(59)
        }
        list2.forEach {
            if ((it.remindTime != null && it.repeatEveryMinutes != null) &&
                (it.remindTime!! > LocalDateTime.now().minusSeconds(59) && it.remindTime!! < LocalDateTime.now()
                    .plusSeconds(59))
            ) {
                it.remindTime = it.remindTime!!.plusMinutes(
                    it.repeatEveryMinutes
                )
                reminderRepository.save(it)
            }
        }
        return list
    }


}