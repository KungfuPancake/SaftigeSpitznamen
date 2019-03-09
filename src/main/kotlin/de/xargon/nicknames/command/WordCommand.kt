package de.xargon.nicknames.command

import de.xargon.nicknames.WordController
import de.xargon.nicknames.database.repository.UserRepository
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.event.message.reaction.SingleReactionEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class WordCommand : Command {
    @Autowired
    lateinit var wordController: WordController

    @Autowired
    lateinit var userRepository: UserRepository

    override fun processCommand(event: MessageCreateEvent) {
        if (!this.userRepository.existsByName(event.messageAuthor.id.toString())) {
            return
        }

        var wordsRaw = event.messageContent.split(Regex("[\\n\\r\\s,]+"))
        wordsRaw = wordsRaw.subList(1, wordsRaw.count())

        if (wordsRaw.isEmpty()) {
            val wordCount = this.wordController.getWordCount()
            when (wordCount.toInt()) {
                0 -> event.channel.sendMessage("Ich kenne noch keine Wörter!")
                1 -> event.channel.sendMessage("Ich kenne erst ein Wort!")
                else -> event.channel.sendMessage("Ich kenne $wordCount Wörter!")
            }
        } else {
            val result = this.wordController.processWords(wordsRaw)
            val sentences = ArrayList<String>()

            if (!result.wordsAdded.isEmpty()) {
                if (result.wordsAdded.size == 1) {
                    sentences.add("Ich habe ein Wort gelernt: **${result.wordsAdded.joinToString(", ")}**")
                } else if (result.wordsAdded.size > 1) {
                    sentences.add("Ich habe diese Wörter gelernt: **${result.wordsAdded.joinToString(", ")}**")
                }
            }

            if (!result.wordsNotAdded.isEmpty()) {
                if (result.wordsNotAdded.size == 1) {
                    sentences.add("Dieses Wort kenne ich schon: **${result.wordsNotAdded.joinToString(", ")}**")
                } else if (result.wordsNotAdded.size > 1) {
                    sentences.add("Diese Wörter kenne ich schon: **${result.wordsNotAdded.joinToString(", ")}**")
                }
            }

            if (!result.wordsRemoved.isEmpty()) {
                if (result.wordsRemoved.size == 1) {
                    sentences.add("Ich habe ein Wort verlernt: **${result.wordsRemoved.joinToString(", ")}**")
                } else if (result.wordsRemoved.size > 1) {
                    sentences.add("Ich habe diese Wörter verlernt: **${result.wordsRemoved.joinToString(", ")}**")
                }
            }

            if (!result.wordsNotRemoved.isEmpty()) {
                if (result.wordsNotRemoved.size == 1) {
                    sentences.add("Ich kenne dieses Wort nicht: **${result.wordsNotRemoved.joinToString(", ")}**")
                } else if (result.wordsNotRemoved.size > 1) {
                    sentences.add("Ich kenne diese Wörter nicht: **${result.wordsNotRemoved.joinToString(", ")}**")
                }
            }

            event.channel.sendMessage(sentences.joinToString("\n"))
        }
    }

    override fun processReaction(event: SingleReactionEvent) {
        return
    }
}