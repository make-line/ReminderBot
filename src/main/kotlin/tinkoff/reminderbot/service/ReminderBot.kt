package tinkoff.reminderbot.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import tinkoff.reminderbot.util.getFullPattern
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Service
class ReminderBot(val botService: StorageBotService) : TelegramLongPollingBot() {

    @Value("\${telegram.botName}")
    private val botName: String = ""


    @Value("\${telegram.token}")
    private val token: String = ""
    val formatterFull: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    override fun getBotUsername(): String = botName

    override fun getBotToken(): String = token

    override fun onUpdateReceived(update: Update?) {
        if (update != null) {
            if (update.hasMessage()) {
                val message = update.message
                val chatId = message.chatId
                var userLevel = 0
                try {
                    userLevel = botService.getUserLevel(chatId)!!
                } catch (e: NoSuchElementException) {
                    botService.addUser(chatId)
                }
                when (userLevel) {
                    0 -> {
                        val responseText = if (message.hasText()) {
                            when (message.text) {
                                "/start" -> "Привет! Этот бот умеет создавать постоянные, однократные и безвременные напоминания! Интерфейс оформлен в виде кнопок, чтобы тебе было удобнее"
                                "Создать напоминание" -> "Введите напоминание"
                                "Посмотреть все напоминания" -> "Посмотреть все напоминания"
                                "Создать постоянное напоминание" -> "Введите постоянное напоминание"
                                else -> "Не понял"
                            }
                        } else {
                            "Я понимаю только текст"
                        }
                        sendNotification(chatId, responseText)
                    }
                    1 -> initReminder(chatId, message.text)
                    2 -> updateReminder(chatId, message.text)
                    3 -> setTime(chatId, message.text)
                }

            }
            if (update.hasCallbackQuery()) {
                botService.deleteById(update.callbackQuery.data.toLong())
                val deleteMessage = DeleteMessage()
                deleteMessage.chatId = update.callbackQuery.message.chatId.toString()
                deleteMessage.messageId = update.callbackQuery.message.messageId
                execute(deleteMessage)
            }
        }
    }

    private fun setTime(chatId: Long, massage: String) {
        if (botService.getUser(chatId).repeatEveryMinutes != null) {
            if (massage == "Отмена") {
                cancel(chatId)
            } else {
                try {
                    println(botService.getUser(chatId))
                    println(LocalDateTime.parse(getFullPattern(massage, true), formatterFull))
                    botService.changeLevelAndSetReminderAndSaveConst(
                        chatId,
                        0,
                        LocalDateTime.parse(getFullPattern(massage, true), formatterFull)
                    )
                    var responseMessage = SendMessage(chatId.toString(), "Супер, все готово!")
                    responseMessage.replyMarkup = getReplyMarkup(
                        listOf(
                            listOf("Создать напоминание"),
                            listOf("Посмотреть все напоминания", "Создать постоянное напоминание")
                        )
                    )
                    execute(responseMessage)
                } catch (e: DateTimeParseException) {
                    var responseMessage = SendMessage(
                        chatId.toString(),
                        "Неправильный формат"
                    )
                    execute(responseMessage)
                }
            }
        } else {
            if (massage == "Отмена") {

                botService.changeLevel(chatId, 2)
                var responseMessage =
                    SendMessage(chatId.toString(), "Ты можешь выбрать время для напоминия, или закончить")
                responseMessage.enableMarkdown(true)
                responseMessage.replyMarkup = getReplyMarkup(
                    listOf(
                        listOf("Закончить", "Выбрать время")
                    )
                )
                execute(responseMessage)

            } else {
                try {
                    val date: LocalDateTime = when (massage) {
                        "Через час" -> {
                            LocalDateTime.now().plusHours(1)
                        }
                        "Через 24 часа" -> {
                            LocalDateTime.now().plusDays(1)
                        }
                        "Через 15 минут" -> {
                            LocalDateTime.now().plusMinutes(15)
                        }
                        else -> {
                            LocalDateTime.parse(getFullPattern(massage, false), formatterFull)
                        }

                    }
                    botService.changeLevelAndSetReminderAndSave(chatId, 0, date)
                    var responseMessage = SendMessage(chatId.toString(), "Супер, все готово!")
                    responseMessage.replyMarkup = getReplyMarkup(
                        listOf(
                            listOf("Создать напоминание"),
                            listOf("Посмотреть все напоминания", "Создать постоянное напоминание")
                        )
                    )
                    execute(responseMessage)
                } catch (e: DateTimeParseException) {
                    var responseMessage = SendMessage(
                        chatId.toString(),
                        "Неправильный формат или прошедшая дата, введите время напоминания по следующим образцам:\nдд.мм.гггг чч:мм\nдд.мм чч:мм\nчч:мм (По умолчанию дата сегодняшняя)"
                    )
                    execute(responseMessage)
                }
            }
        }

    }

    private fun updateReminder(chatId: Long, massage: String) {
        var responseMessage = SendMessage(chatId.toString(), "Напоминие создано")
        if (botService.getUser(chatId).repeatEveryMinutes != null) {
            responseMessage = SendMessage(chatId.toString(), "Напоминие стерто")
            when (massage) {
                "Каждый день" -> {
                    responseMessage = SendMessage(
                        chatId.toString(), "Введите время напоминания по следующим образцам:\n" +
                                "чч:мм"
                    )
                    responseMessage.replyMarkup = getReplyMarkup(
                        listOf(
                            listOf("Отмена")
                        )
                    )
                    botService.changeLevelAndTime(chatId, 3, 1440)
                }
                "Каждую неделю" -> {
                    responseMessage = SendMessage(
                        chatId.toString(), "Введите время и дату первого напоминания по следующему образцу:\n" +
                                "дд.мм чч:мм"
                    )
                    responseMessage.replyMarkup = getReplyMarkup(
                        listOf(
                            listOf("Отмена")
                        )
                    )
                    botService.changeLevelAndTime(chatId, 3, 10080)
                }
                else -> {
                    botService.changeLevelAndClean(chatId, 0)
                    responseMessage.enableMarkdown(true)
                    responseMessage.replyMarkup = getReplyMarkup(
                        listOf(
                            listOf("Создать напоминание"),
                            listOf("Посмотреть все напоминания", "Создать постоянное напоминание")
                        )
                    )
                }
            }
            execute(responseMessage)


        } else {
            if (massage == "Выбрать время") {
                responseMessage = SendMessage(
                    chatId.toString(), "Введите время напоминания по следующим образцам:\n" +
                            "дд.мм.гггг чч:мм\n" +
                            "дд.мм чч:мм\n" +
                            "чч:мм (По умолчанию дата сегодняшняя)"
                )
                botService.changeLevel(chatId, 3)
                responseMessage.enableMarkdown(true)
                responseMessage.replyMarkup = getReplyMarkup(
                    listOf(
                        listOf("Через час", "Через 24 часа"),
                        listOf("Через 15 минут", "Отмена")
                    )
                )
            } else {
                botService.changeLevelAndCleanAndSave(chatId, 0)
                responseMessage.enableMarkdown(true)
                responseMessage.replyMarkup = getReplyMarkup(
                    listOf(
                        listOf("Создать напоминание"),
                        listOf("Посмотреть все напоминания", "Создать постоянное напоминание")
                    )
                )
            }
            responseMessage.enableMarkdown(true)
            execute(responseMessage)
        }
    }


    private fun initReminder(chatId: Long, reminder: String) {
        if (reminder == "Отмена") {
            cancel(chatId)

        } else {
            if (botService.getUser(chatId).repeatEveryMinutes != null) {
                val responseMessage = SendMessage(chatId.toString(), "Когда тебе напоминать?")
                botService.changeLevelAndSetReminder(chatId, 2, reminder)
                responseMessage.enableMarkdown(true)
                responseMessage.replyMarkup = getReplyMarkup(
                    listOf(
                        listOf("Каждый день", "Каждую неделю"),
                        listOf("Отмена")
                    )
                )
                execute(responseMessage)
            } else {
                botService.changeLevelAndSetReminder(chatId, 2, reminder)
                val responseMessage =
                    SendMessage(
                        chatId.toString(),
                        "Отлично! Теперь ты можешь выбрать время для напоминия, или закончить"
                    )
                responseMessage.enableMarkdown(true)
                responseMessage.replyMarkup = getReplyMarkup(
                    listOf(
                        listOf("Закончить", "Выбрать время")
                    )
                )
                execute(responseMessage)
            }
        }
    }


    private fun sendNotification(chatId: Long, responseText: String) {
        var responseMessage = SendMessage(chatId.toString(), responseText)
        responseMessage.enableMarkdown(true)
        when (responseText) {
            "Введите напоминание" -> {
                botService.changeLevel(chatId, 1)
                responseMessage.replyMarkup = getReplyMarkup(
                    listOf(
                        listOf("Отмена"),
                    )
                )
            }
            "Введите постоянное напоминание" -> {
                botService.changeLevelConst(chatId, 1)
                responseMessage.replyMarkup = getReplyMarkup(
                    listOf(
                        listOf("Отмена"),
                    )
                )
            }
            "Посмотреть все напоминания" -> {
                botService.getAllRemindersByUser(chatId).forEach {
                    var status = ""
                    if (it.repeatEveryMinutes != null) status = "Постоянное"
                    execute(it.id?.let { it1 ->
                        sendInlineKeyBoardMessage(
                            chatId, it.reminder + "\n\n" + ((it.remindTime?.format(formatterFull) + "\n\n" + status)
                                ?: "") + "\n", it1
                        )
                    })
                }
                responseMessage.replyMarkup = getReplyMarkup(
                    listOf(
                        listOf("Создать напоминание"),
                        listOf("Посмотреть все напоминания", "Создать постоянное напоминание")
                    )
                )
                responseMessage =
                    SendMessage(chatId.toString(), "Всего напоминий ${botService.getAllRemindersByUser(chatId).size}")
            }
            else -> responseMessage.replyMarkup = getReplyMarkup(
                listOf(
                    listOf("Создать напоминание"),
                    listOf("Посмотреть все напоминания", "Создать постоянное напоминание")
                )
            )
        }
        execute(responseMessage)
    }

    private fun getReplyMarkup(allButtons: List<List<String>>): ReplyKeyboardMarkup {
        val markup = ReplyKeyboardMarkup()
        markup.keyboard = allButtons.map { rowButtons ->
            val row = KeyboardRow()
            rowButtons.forEach { rowButton -> row.add(rowButton) }
            row
        }
        return markup
    }


    fun sendReminder(chatId: Long,id: Long) {
        val responseMessage = SendMessage(chatId.toString(),"Напоминаю - \n\n"+ botService.getReminderMessage(id)!!)
        responseMessage.enableMarkdown(true)
        responseMessage.replyMarkup = getReplyMarkup(
            listOf(
                listOf("Создать напоминание"),
                listOf("Посмотреть все напоминания", "Создать постоянное напоминание")
            )
        )
        execute(responseMessage)
    }

    fun sendInlineKeyBoardMessage(chatId: Long, message: String, id: Long): SendMessage? {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val inlineKeyboardButton1 = InlineKeyboardButton()
        inlineKeyboardButton1.text = "Удалить"
        inlineKeyboardButton1.callbackData = id.toString()
        val keyboardButtonsRow1: MutableList<InlineKeyboardButton> = ArrayList()
        keyboardButtonsRow1.add(inlineKeyboardButton1)
        val rowList: MutableList<List<InlineKeyboardButton>> = ArrayList()
        rowList.add(keyboardButtonsRow1)
        inlineKeyboardMarkup.keyboard = rowList
        val send = SendMessage()
        send.text = message
        send.chatId = chatId.toString()
        send.replyMarkup = inlineKeyboardMarkup
        return send
    }

    fun cancel(chatId: Long) {
        botService.changeLevelAndClean(chatId, 0)
        var responseMessage = SendMessage(chatId.toString(), "Напоминание стерто")
        responseMessage.enableMarkdown(true)
        responseMessage.replyMarkup = getReplyMarkup(
            listOf(
                listOf("Создать напоминание"),
                listOf("Посмотреть все напоминания", "Создать постоянное напоминание")
            )
        )
        execute(responseMessage)
    }


}