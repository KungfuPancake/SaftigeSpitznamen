package de.xargon.nicknames.command

import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.event.message.reaction.SingleReactionEvent

interface Command {
    fun processCommand(event: MessageCreateEvent)
    fun processReaction(event: SingleReactionEvent)
}