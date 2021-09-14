package de.diekautz.federationserver.controller

enum class SessionType(val subDomain: String) {
    NONE(""), DISCORD("dc"), GITHUB("gh"), TWITTER("tw")
}