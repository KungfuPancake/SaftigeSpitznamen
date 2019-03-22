package de.xargon.nicknames.database.model

import javax.persistence.*

@Entity
data class Word(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,

        @Column(nullable = false, unique = false, length = 191)
        var value: String,

        @Column(nullable = true, unique = false, length = 191)
        var prefix: String?
)