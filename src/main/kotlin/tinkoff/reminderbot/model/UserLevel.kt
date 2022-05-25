package tinkoff.reminderbot.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class UserLevel
    (
    @Id
    @Column(name = "chat_id", nullable = false)
    val chatId: Long? = null,
    @Column(name = "level", nullable = false)
    var level: Int? = 0,
    @Column(name = "reminder", nullable = true)
    var reminder:String? = null,

    @Column(name = "remind_time", nullable = true)
    var remindTime: LocalDateTime? = null,

    @Column(name = "repeat_every_minutes", nullable = true)
    var repeatEveryMinutes: Long? = null
)
