package de.xargon.nicknames.command

import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.event.message.reaction.SingleReactionEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CommandRouter {
    // this is ugly, but we need autowired for now
    @Autowired
    private lateinit var wordCommand: WordCommand

    @Autowired
    private lateinit var nicknameCommand: NicknameCommand

    @Autowired
    private lateinit var userCommand: UserCommand

    @Autowired
    private lateinit var topTenCommand: TopTenCommand

    fun get(event: MessageCreateEvent): Command {
        return when {
            event.messageContent.startsWith("!wort") -> this.wordCommand
            event.messageContent.startsWith("!spitzname") -> this.nicknameCommand
            event.messageContent.startsWith("!benutzer") -> this.userCommand
            event.messageContent.startsWith("!top10") -> this.topTenCommand
            else -> UnknownCommand()
        }
    }

    fun get(event: SingleReactionEvent): Command {
        // we currently only support reactions on nicknames, so..
        return when {
            NicknameCommand.mappingExists(event.messageId.toString()) -> this.nicknameCommand
            else -> UnknownCommand()
        }
    }
}