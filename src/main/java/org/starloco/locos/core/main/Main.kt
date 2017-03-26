package org.starloco.locos.core.main

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory
import org.starloco.locos.core.config.Config
import org.starloco.locos.core.config.ConfigReader
import org.starloco.locos.database.Database
import org.starloco.locos.core.network.login.ExchangeClient
import org.starloco.locos.game.area.map.GameMap
import org.starloco.locos.game.area.map.entity.InteractiveObject
import org.starloco.locos.game.entity.mount.Mount
import org.starloco.locos.game.event.EventManager
import org.starloco.locos.core.network.client.GameServer
import org.starloco.locos.game.world.scheduler.entity.WorldPlayerOption
import org.starloco.locos.game.world.scheduler.entity.WorldPub
import org.starloco.locos.game.world.scheduler.entity.WorldSave
import org.starloco.locos.game.world.world.World
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*

object Main {

    val runnables: MutableList<Runnable> = LinkedList()

    var mapAsBlocked = false
    var fightAsBlocked = false
    var tradeAsBlocked = false

    private val logger = LoggerFactory.getLogger(Main::class.java) as Logger
    private val shutdownThread = Thread { closeServer() }

    @Throws(SQLException::class)
    @JvmStatic fun main(args: Array<String>) {
        Runtime.getRuntime().addShutdownHook(shutdownThread)
        start()
    }

    private fun start() {
        logger.info("You use ${System.getProperty("java.vendor")} with the version ${System.getProperty("java.version")}")
        logger.debug("Starting of the server : ${SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.FRANCE).format(Date())}")
        logger.debug("Current timestamp ms : ${System.currentTimeMillis()}")
        logger.debug("Current timestamp ns : ${System.nanoTime()}")

        if (!Database.launchDatabase()) {
            logger.error("An error occurred when the server have try a connection on the Mysql server. Please verify your identification.")
            return
        }

        Config.isRunning = true


        World.world.createWorld()
        if(!GameServer.INSTANCE.start()) {
            stop("Can't init world server",2)
            return
        }
        if(!ExchangeClient.INSTANCE.start()) {
            stop("Can't init discussion with login",3)
            return
        }

        logger.info("Server is ready ! Waiting for connection..\n")
        while (Config.isRunning) {
            try {
                WorldSave.updatable.update()
                GameMap.updatable.update()
                InteractiveObject.updatable.update()
                Mount.updatable.update()
                WorldPlayerOption.updatable.update()
                WorldPub.updatable.update()
                EventManager.getInstance().update()

                if (!runnables.isEmpty()) {
                    for (runnable in LinkedList(runnables)) {
                        try {
                            if (runnable != null) {
                                runnable.run()
                                runnables.remove(runnable)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }

                Thread.sleep(100)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun closeServer() {
        if (Config.isRunning) {
            Config.isRunning = false

            GameServer.INSTANCE.setState(0)
            WorldSave.cast(0)
            if (!Config.isHeroic) {
                Database.getDynamics().heroicMobsGroups.deleteAll()
                for (map in World.world.maps) {
                    map.mobGroups.values.filterNot { it.isFix }.forEach { Database.getDynamics().heroicMobsGroups.insert(map.id, it, null) }
                }
            }
            GameServer.INSTANCE.setState(0)

            GameServer.INSTANCE.kickAll(true)
            Database.getStatics().serverData.loggedZero()
        }
        GameServer.INSTANCE.stop()
        ExchangeClient.INSTANCE.stop()
        Main.logger.info("The server is now closed.")
    }

    @JvmOverloads
    fun stop(reason: String, exitCode : Int = 0) {
        logger.error("Start closing server : {}", reason)
        Runtime.getRuntime().removeShutdownHook(shutdownThread)
        closeServer()
        System.exit(exitCode)
    }

}
