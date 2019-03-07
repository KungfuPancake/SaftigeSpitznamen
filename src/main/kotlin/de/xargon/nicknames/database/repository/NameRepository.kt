package de.xargon.nicknames.database.repository

import de.xargon.nicknames.database.model.Name
import org.springframework.data.repository.CrudRepository

interface NameRepository : CrudRepository<Name, Long> {
    fun findByValue(value: String): Name?

    fun findTop10ByOrderByUpvotesDesc(): List<Name>
}