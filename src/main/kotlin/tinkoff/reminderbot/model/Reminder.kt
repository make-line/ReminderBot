package tinkoff.reminderbot.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Reminder(

    @Column(name = "chat_id", nullable = false)
    val chatId:Long? = null,

    @Column(name = "reminder", nullable = false)
    val reminder:String? = null,

    @Column(name = "remind_time", nullable = true)
    var remindTime: LocalDateTime? = null,

    @Column(name = "repeat_every_minutes", nullable = true)
    val repeatEveryMinutes: Long? = null
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id:Long? = null
}
