package de.diekautz.federationserver.model

import de.diekautz.federationserver.controller.SessionType
import java.util.*

data class UserSession(
    val id: UUID = UUID.randomUUID(),
    var sessionType: SessionType = SessionType.NONE,
    var username: String = "",
)