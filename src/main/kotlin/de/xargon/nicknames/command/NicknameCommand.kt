package de.xargon.nicknames.command

import com.vdurmont.emoji.EmojiParser
import de.xargon.nicknames.WordController
import de.xargon.nicknames.database.model.Name
import de.xargon.nicknames.database.repository.NameRepository
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.event.message.reaction.ReactionAddEvent
import org.javacord.api.event.message.reaction.ReactionRemoveEvent
import org.javacord.api.event.message.reaction.SingleReactionEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class NicknameCommand : Command {
    @Autowired
    lateinit var wordController: WordController

    @Autowired
    lateinit var nameRepository: NameRepository

    companion object {
        private val messageIdToName: HashMap<String, String> = HashMap()

        fun mappingExists(messageId: String): Boolean {
            return this.messageIdToName.containsKey(messageId)
        }
    }

    override fun processCommand(event: MessageCreateEvent) {
        val name = this.wordController.getRandomWords(2).joinToString("")
        val dbName = this.nameRepository.findByValue(name)

        val message = event.channel.sendMessage(this.generateMessage(name, dbName)).get()
        if (message != null) {
            NicknameCommand.messageIdToName[message.id.toString()] = name
            message.addReaction(EmojiParser.parseToUnicode(":ok_hand:")).get()
        }
    }

    @Transactional
    override fun processReaction(event: SingleReactionEvent) {
        // bail out if there is no mapping
        if (!NicknameCommand.mappingExists(event.messageId.toString())) {
            return
        }

        val name = NicknameCommand.messageIdToName[event.messageId.toString()] ?: return

        if (!event.emoji.equalsEmoji(EmojiParser.parseToUnicode(":ok_hand:"))) {
            return
        }

        if (event is ReactionAddEvent && event.count.get() <= 1) {
            return
        }

        var dbName = this.nameRepository.findByValue(name)
        if (dbName == null) {
            dbName = Name(0, name, 0)
        }

        when (event) {
            is ReactionAddEvent -> dbName.upvotes++
            is ReactionRemoveEvent -> dbName.upvotes--
        }
        this.nameRepository.save(dbName)

        event.message.get().edit(this.generateMessage(name, dbName))
    }

    private fun generateMessage(name: String, dbName: Name?): String {
        var content = "Hier ist dein Spitzname: **$name**. Saftiger Name? Stimme mit :ok_hand: ab!"
        if (dbName != null) {
            content = when (dbName.upvotes) {
                1L -> content.plus(" (Bisher ${dbName.upvotes} Stimme)")
                else -> content.plus(" (Bisher ${dbName.upvotes} Stimmen)")
            }
        }

        return content
    }
}