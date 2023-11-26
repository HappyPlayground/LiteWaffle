package com.discord.sapokr.litewaffle

data class BotStatus(
    val Litewaffle: LitewaffleStatus,
    val webhooks: MutableMap<String, Boolean>
)

data class LitewaffleStatus(
    val isOnline: String,
    val account: String,
    val listeners: List<String>
)