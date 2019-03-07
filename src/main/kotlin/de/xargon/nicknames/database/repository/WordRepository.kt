package de.xargon.nicknames.database.repository

import de.xargon.nicknames.database.model.Word
import org.springframework.data.repository.CrudRepository

interface WordRepository : CrudRepository<Word, Long> {
    fun existsByValue(value: String): Boolean

    fun deleteByValue(value: String): Long
}