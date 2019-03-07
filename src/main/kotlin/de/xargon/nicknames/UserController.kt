package de.xargon.nicknames

import de.xargon.nicknames.database.model.User
import de.xargon.nicknames.database.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserController {
    @Autowired
    lateinit var userRepository: UserRepository

    @Transactional
    fun processUser(words: List<String>): String {
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