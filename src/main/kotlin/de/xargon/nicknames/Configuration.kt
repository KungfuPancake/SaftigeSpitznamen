package de.xargon.nicknames

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component

@Component
@PropertySource("classpath:application.yml")
@ConfigurationProperties(prefix = "app")
class Configuration {
    val discord: Discord = Discord()
    val users: List<String> = ArrayList()

    class Discord {
        lateinit var token: String
    }
}