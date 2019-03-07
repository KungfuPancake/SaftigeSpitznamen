package de.xargon.nicknames

import de.xargon.nicknames.database.repository.WordRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class NicknameGenerator {
    @Autowired
    lateinit var wordController: WordController

    @Autowired
    lateinit var wordRepository: WordRepository

    @GetMapping("/")
    fun random(model: Model): String {
        val words = this.wordController.getRandomWords(2)
        model.addAttribute("name", words[0] + words[1])

        return "random"
    }

    @GetMapping("/words")
    fun wordForm(model: Model): String {
        return "words"
    }

    @PostMapping("/words")
    fun saveWords(model: Model, @RequestParam("words") words: String) : String {
        val wordsRaw = words.split(Regex("[\\n\\r\\s]+"))

        println("Input: ${wordsRaw.joinToString(", ")}")

        val result = this.wordController.processWords(wordsRaw)

        model.addAttribute("count", "${result.wordsAdded} neue Wörter")

        return "savewords"
    }
}