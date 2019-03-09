package de.xargon.nicknames.command

import de.xargon.nicknames.database.repository.NameRepository
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.event.message.reaction.SingleReactionEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TopTenCommand : Command {
    @Autowired
    lateinit var nameRepository: NameRepository

    override fun processCommand(event: MessageCreateEvent) {
        val sentences = ArrayList<String>()

        for (name in this.nameRepository.findTop10ByOrderByUpvotesDesc()) {
            sentences.add("**${name.value}** (${name.upvotes})")
        }

        event.channel.sendMessage(sentences.joinToString("\n"))
    }

    override fun processReaction(event: SingleReactionEvent) {
        return
    }
}