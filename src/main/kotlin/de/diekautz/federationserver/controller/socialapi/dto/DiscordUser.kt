package de.diekautz.federationserver.controller.socialapi.dto

class DiscordUser (
    val id: Long,
    val username: String,
    val discriminator: String,
    val avatar: String,
)