package org.starloco.locos.kernel

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory
import org.starloco.locos.area.map.GameMap
import org.starloco.locos.area.map.entity.InteractiveObject
import org.starloco.locos.database.Database
import org.starloco.locos.entity.mount.Mount
import org.starloco.locos.event.EventManager
import org.starloco.locos.exchange.ExchangeClient
import org.starloco.locos.game.GameServer
import org.starloco.locos.game.scheduler.entity.WorldPlayerOption
import org.starloco.locos.game.scheduler.entity.WorldPub
import org.starloco.locos.game.scheduler.entity.WorldSave
import org.starloco.locos.game.world.World
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

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
        Main.start()
    }

    private fun start() {
        Main.logger.info("You use ${System.getProperty("java.vendor")} with the version ${System.getProperty("java.version")}")
        Main.logger.debug("Starting of the server : ${SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.FRANCE).format(Date())}")
        Main.logger.debug("Current timestamp ms : ${System.currentTimeMillis()}")
        Main.logger.debug("Current timestamp ns : ${System.nanoTime()}")

        if (!Database.launchDatabase()) {
            logger.error("An error occurred when the server have try a connection on the Mysql server. Please verify your identification.")
            return;
        }

        Config.isRunning = true


        World.world.createWorld()
        if(!GameServer.INSTANCE.start()) {
            stop("Can't init game server",2)
            return
        }

        if(!ExchangeClient.INSTANCE.start()) {
            stop("Can't init discussion with login",3)
            return
        }

        Main.logger.info("Server is ready ! Waiting for connection..\n")

        while (Config.isRunning) {
            try {
                WorldSave.updatable.update()
                GameMap.updatable.update()
                InteractiveObject.updatable.update()
                Mount.updatable.update()
                WorldPlayerOption.updatable.update()
                WorldPub.updatable.update()
                EventManager.getInstance().update()

                if (!Main.runnables.isEmpty()) {
                    for (runnable in LinkedList(Main.runnables)) {
                        try {
                            if (runnable != null) {
                                runnable.run()
                                Main.runnables.remove(runnable)
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
            if (!Config.HEROIC) {
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
        Main.logger.info("The server is now closed.")
    }

    @JvmOverloads
    fun stop(reason: String, exitCode : Int = 0) {
        logger.error("Start closing server : {}", reason)
        Runtime.getRuntime().removeShutdownHook(shutdownThread);
        closeServer()
        System.exit(exitCode)
    }

}
