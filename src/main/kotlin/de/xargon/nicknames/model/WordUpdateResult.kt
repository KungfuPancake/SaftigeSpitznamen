package de.xargon.nicknames.model

data class WordUpdateResult(
        var wordsAdded: ArrayList<String> = ArrayList(),
        var wordsNotAdded: ArrayList<String> = ArrayList(),
        var wordsRemoved: ArrayList<String> = ArrayList(),
        var wordsNotRemoved: ArrayList<String> = ArrayList()
)