package de.xargon.nicknames

import de.xargon.nicknames.command.CommandRouter
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.activity.ActivityType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Suppress("unused")
@Component
class DiscordBot(val configuration: Configuration) {
    @Autowired
    private lateinit var commandRouter: CommandRouter

    @Suppress("unused")
    @EventListener(ApplicationReadyEvent::class)
    fun run() {
        val api = DiscordApiBuilder().setToken(configuration.discord.token).login().join()

        this.setStatus(api)
        api.addReconnectListener { this.setStatus(api) }
        api.addMessageCreateListener { this.commandRouter.get(it).processCommand(it) }
        api.addReactionAddListener { this.commandRouter.get(it).processReaction(it) }
        api.addReactionRemoveListener { this.commandRouter.get(it).processReaction(it) }

        println("You can invite the bot by using the following url: " + api.createBotInvite())
    }

    private fun setStatus(api: DiscordApi) {
        api.updateActivity(ActivityType.PLAYING, "Taschenbillard")
    }
}