package de.xargon.nicknames

import de.xargon.nicknames.database.model.Word
import de.xargon.nicknames.model.WordUpdateResult
import de.xargon.nicknames.database.repository.WordRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class WordController {
    @Autowired
    private lateinit var wordRepository: WordRepository

    fun getWordCount(): Long {
        return this.wordRepository.count()
    }

    fun getRandomWords(length: Int): List<String> {
        val words = ArrayList(this.wordRepository.findAll().toList())
        val list: ArrayList<String> = ArrayList()

        for (count in 1..length) {
            if (words.isEmpty()) {
                list.add("Schnitzel")
            } else {
                val word = words.elementAt((Math.random() * words.count()).toInt())
                list.add(word.value)
                words.remove(word)
            }
        }

        return list
    }

    @Transactional
    fun processWords(wordsRaw: List<String>): WordUpdateResult {
        val result = WordUpdateResult()

        for (wordRaw in wordsRaw) {
            var word = wordRaw.trim()

            if (word[0] == '-') {
                word = word.substring(1).capitalize()
                if (this.wordRepository.deleteByValue(word) > 0) {
                    result.wordsRemoved.add(word)
                } else {
                    result.wordsNotRemoved.add(word)
                }

            } else {
                word = word.capitalize()
                if (!this.wordRepository.existsByValue(word) && !result.wordsAdded.contains(word)) {
                    this.wordRepository.save(Word(0, word))
                    result.wordsAdded.add(word)
                } else {
                    result.wordsNotAdded.add(word)
                }
            }
        }

        println("New words: ${result.wordsAdded.joinToString(", ")}, removed words: ${result.wordsRemoved.joinToString(", ")}")

        return result
    }
}