package tinkoff.reminderbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class ReminderBotApplication

fun main(args: Array<String>) {
    runApplication<ReminderBotApplication>(*args)
}
