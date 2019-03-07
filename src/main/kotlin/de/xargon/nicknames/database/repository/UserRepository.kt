package de.xargon.nicknames.database.repository

import de.xargon.nicknames.database.model.User
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Long> {
    fun existsByName(name: String): Boolean

    fun deleteByName(name: String): Long
}