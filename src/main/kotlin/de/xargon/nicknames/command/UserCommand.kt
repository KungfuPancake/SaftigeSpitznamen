package de.xargon.nicknames.command

import de.xargon.nicknames.database.model.User
import de.xargon.nicknames.database.repository.UserRepository
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.event.message.reaction.SingleReactionEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserCommand : Command {
    @Autowired
    lateinit var userRepository: UserRepository

    override fun processCommand(event: MessageCreateEvent) {
        if (!this.userRepository.existsByName(event.messageAuthor.id.toString())) {
            return
        }

        val words = event.messageContent.split(Regex("[\\n\\r\\s]+"))
        val result = this.processUser(words)

        if (!result.isBlank()) {
            event.channel.sendMessage(result)
        }
    }

    override fun processReaction(event: SingleReactionEvent) {
        return
    }

    @Transactional
    private fun processUser(words: List<String>): String {
        var message = ""
        if (words.size > 1) {
            val user = words[1].trim()
            val id = user.replace(Regex("[^0-9]*"), "")

            if (!id.isBlank()) {
                if (user.startsWith("-")) {
                    if (this.userRepository.existsByName(id)) {
                        this.userRepository.deleteByName(id)
                        message = "Benutzer entfernt: <@$id>"
                        println("User removed: $id")
                    } else {
                        println("User does not exist: $id")
                    }
                } else {
                    if (!this.userRepository.existsByName(id)) {
                        this.userRepository.save(User(0, id))
                        message = "Benutzer hinzugefügt: <@$id>"
                        println("User added: $id")
                    } else {
                        println("User already exists: $id")
                    }
                }
            }
        }

        return message
    }
}