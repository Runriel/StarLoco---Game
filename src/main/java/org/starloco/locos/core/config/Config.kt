package org.starloco.locos.core.config

object Config {

    val startTime = System.currentTimeMillis()
    var isHalloween = ConfigReader.data[ConfigReader.mode.halloween]
    var isChristmas = ConfigReader.data[ConfigReader.mode.christmas]
    var isHeroic = ConfigReader.data[ConfigReader.mode.heroic]
    var isTeamMatch = ConfigReader.data[ConfigReader.options.teamMatch]
    var isDeathMatch = ConfigReader.data[ConfigReader.options.deathMatch]
    var isAutoEvent = ConfigReader.data[ConfigReader.options.event.active]
    var isAutoReboot = ConfigReader.data[ConfigReader.options.autoReboot]
    var isAllZaap = ConfigReader.data[ConfigReader.options.allZaap]
    var isAllEmote = ConfigReader.data[ConfigReader.options.allEmote]

    var isSaving = false
    var isRunning = false

    var encryptPacket = ConfigReader.data[ConfigReader.options.encryptPacket]
    var timePerEvent: Short = ConfigReader.data[ConfigReader.options.event.timePerEvent].toShort()

    var NAME: String = "Terra ~ X"
    var url: String = ""
    var startMessage = "$NAME gracias a Locos y Runriel por la base."
    var colorMessage = "B9121B"

    var startMap = ConfigReader.data[ConfigReader.options.start.map]
    var startCell = ConfigReader.data[ConfigReader.options.start.cell]
    var rateKamas = ConfigReader.data[ConfigReader.rate.kamas]
    var rateFarm = ConfigReader.data[ConfigReader.rate.farm]
    var rateHonor = ConfigReader.data[ConfigReader.rate.honor]
    var rateJob = ConfigReader.data[ConfigReader.rate.job]
    var rateXp = ConfigReader.data[ConfigReader.rate.xp]

    var exchangePort: Int = ConfigReader.data[ConfigReader.exchange.port]
    var gamePort: Int = ConfigReader.data[ConfigReader.server.port]
    var exchangeIp: String = ConfigReader.data[ConfigReader.exchange.host]
    var loginHostDB: String = ConfigReader.data[ConfigReader.database.login.host]
    var loginPortDB: Int = ConfigReader.data[ConfigReader.database.login.port]
    var loginNameDB: String = ConfigReader.data[ConfigReader.database.login.name]
    var loginUserDB: String = ConfigReader.data[ConfigReader.database.login.user]
    var loginPassDB: String = ConfigReader.data[ConfigReader.database.login.pass]
    var hostDB: String? = ConfigReader.data[ConfigReader.database.game.host]
    var portDB: Int = ConfigReader.data[ConfigReader.database.game.port]
    var nameDB: String? = ConfigReader.data[ConfigReader.database.game.name]
    var userDB: String? = ConfigReader.data[ConfigReader.database.game.user]
    var passDB: String? = ConfigReader.data[ConfigReader.database.game.pass]
    var ip: String? = ConfigReader.data[ConfigReader.server.host]

    var serverId: Int = ConfigReader.data[ConfigReader.server.id]
    var serverKey: String = ConfigReader.data[ConfigReader.server.key]
    var subscription = ConfigReader.data[ConfigReader.options.subscription]

    var startKamas: Long = ConfigReader.data[ConfigReader.options.start.kamas]
    var startLevel: Int = ConfigReader.data[ConfigReader.options.start.level]
}