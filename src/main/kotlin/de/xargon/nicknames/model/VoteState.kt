package de.xargon.nicknames.model

data class VoteState(
        var name: String,
        val voters: ArrayList<String> = ArrayList()
)