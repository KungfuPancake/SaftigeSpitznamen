package de.xargon.nicknames

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NicknamesApplication

fun main(args: Array<String>) {
    runApplication<NicknamesApplication>(*args)
}

