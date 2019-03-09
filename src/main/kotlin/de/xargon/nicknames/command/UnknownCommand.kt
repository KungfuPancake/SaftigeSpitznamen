package de.xargon.nicknames.command

import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.event.message.reaction.SingleReactionEvent

class UnknownCommand : Command {
    override fun processCommand(event: MessageCreateEvent) {
        // ignore unknown commands
        return
    }

    override fun processReaction(event: SingleReactionEvent) {
        return
    }
}