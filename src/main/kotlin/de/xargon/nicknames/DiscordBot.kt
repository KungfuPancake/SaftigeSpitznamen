package de.xargon.nicknames

import com.vdurmont.emoji.EmojiParser
import de.xargon.nicknames.database.model.Name
import de.xargon.nicknames.database.repository.NameRepository
import de.xargon.nicknames.database.repository.UserRepository
import de.xargon.nicknames.model.VoteState
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.activity.ActivityType
import org.javacord.api.entity.emoji.Emoji
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.event.message.reaction.ReactionAddEvent
import org.javacord.api.event.message.reaction.ReactionRemoveEvent
import org.javacord.api.event.message.reaction.SingleReactionEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DiscordBot(val configuration: Configuration) {
    @Autowired
    lateinit var wordController: WordController

    @Autowired
    lateinit var userController: UserController

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var nameRepository: NameRepository

    private val messageIdToName: HashMap<String, String> = HashMap()

    @EventListener(ApplicationReadyEvent::class)
    fun run() {
        val api = DiscordApiBuilder().setToken(configuration.discord.token).login().join()
        api.updateActivity(ActivityType.PLAYING, "Taschenbillard")
        api.addMessageCreateListener {
            when {
                it.messageContent.startsWith("!wort") -> this.handleWordCommand(it)
                it.messageContent.startsWith("!spitzname") -> this.handleNicknameCommand(it)
                it.messageContent.startsWith("!benutzer") -> this.handleUserCommand(it)
                it.messageContent.startsWith("!top10") -> this.handleTop10Command(it)
            }
        }

        api.addReactionAddListener { this.handleVote(it) }
        api.addReactionRemoveListener { this.handleVote(it) }
        println("You can invite the bot by using the following url: " + api.createBotInvite())
    }

    private fun handleTop10Command(event: MessageCreateEvent) {
        val sentences = ArrayList<String>()

        for (name in this.nameRepository.findTop10ByOrderByUpvotesDesc()) {
            sentences.add("**${name.value}** (${name.upvotes})")
        }

        event.channel.sendMessage(sentences.joinToString("\n"))
    }

    @Transactional
    private fun handleVote(event: SingleReactionEvent) {
        val name = this.messageIdToName[event.messageId.toString()] ?: return

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

    private fun handleUserCommand(event: MessageCreateEvent) {
        if (this.userRepository.existsByName(event.messageAuthor.id.toString())) {
            val words = event.messageContent.split(Regex("[\\n\\r\\s]+"))

            val result = this.userController.processUser(words)

            if (!result.isBlank()) {
                event.channel.sendMessage(result)
            }
        }
    }

    private fun handleNicknameCommand(event: MessageCreateEvent) {
        val name = this.wordController.getRandomWords(2).joinToString("")
        val dbName = this.nameRepository.findByValue(name)

        val message = event.channel.sendMessage(this.generateMessage(name, dbName)).get()
        if (message != null) {
            this.messageIdToName[message.id.toString()] = name
            message.addReaction(EmojiParser.parseToUnicode(":ok_hand:")).get()
        }
    }

    private fun handleWordCommand(event: MessageCreateEvent) {
        if (this.userRepository.existsByName(event.messageAuthor.id.toString())) {
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