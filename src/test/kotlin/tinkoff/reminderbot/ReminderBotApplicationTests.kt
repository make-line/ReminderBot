package tinkoff.reminderbot

import org.springframework.boot.test.context.SpringBootTest
import tinkoff.reminderbot.service.ReminderBot
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.springframework.beans.factory.annotation.Autowired
import tinkoff.reminderbot.model.Reminder
import tinkoff.reminderbot.service.ScheduledService
import java.time.LocalDateTime

@SpringBootTest
class ReminderBotApplicationTests() {
    @Autowired
    private lateinit var scheduledService: ScheduledService
    @Test
    fun testScheduler() {
        var reminderBot = mockk<ReminderBot>()
        every { reminderBot.botService.getAllReminders() } returns listOf(Reminder(1, "ok", LocalDateTime.now(), null))
        every { reminderBot.sendReminder(any(), any()) } returns Unit
        assertEquals(scheduledService.reportCurrentTime(), true)
    }


}
