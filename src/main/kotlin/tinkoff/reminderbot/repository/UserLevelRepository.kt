package tinkoff.reminderbot.repository;

import org.springframework.data.jpa.repository.JpaRepository
import tinkoff.reminderbot.model.UserLevel

interface UserLevelRepository : JpaRepository<UserLevel, Long> {
}