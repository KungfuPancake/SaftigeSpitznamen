package de.xargon.nicknames.database.model

import javax.persistence.*

@Entity
data class Name(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,

        @Column(unique = true, nullable = false, length = 191)
        var value: String,

        var upvotes: Long
)