package org.starloco.locos.game.command

import org.starloco.locos.core.Loggers
import org.starloco.locos.game.client.Player

/**
 * Created by Runriel on 18/03/17.
 */

typealias LaunchCommand = (Player, Array<String>) -> Unit

object NewCommand {

    data class Command(
            val canBeLaunch : (Player) -> Boolean,
            val onLaunch : LaunchCommand,
            val help: String)

    private val commands : MutableMap<String, Command> = mutableMapOf()

    /**
     * Add a command
     * @param commandName name of the command
     * @param command command to be executed
     */
    fun addCommand(commandName : String, command : Command){
        commands.put(commandName, command)
    }

    /**
     * Add a command with a condition only on level of administration
     * @param commandName Name of the command
     * @param onLaunch function that should be called on the player
     * @param adminLevelRequired minimal admin level needed (default is 1)
     * @param help help message for the command
     */
    fun addCommand(commandName: String, onLaunch : LaunchCommand, adminLevelRequired: Int = 1, help : String = "") {
        addCommand(commandName,
                Command(fun (player) = player.account.adminLevel >= adminLevelRequired, onLaunch, help))
    }

    /**
     * List all available commands for a player at a moment
     * @param player taken as reference for command
     * @return concatenation of command name and their help
     */
    fun availableCommands(player: Player) : String =
        commands.filter { it.value.canBeLaunch(player) }.map { "${it.key} : ${it.value.help}" }.joinToString("/n")



    fun initCommands() {
        addCommand("report", fun(p : Player,args : Array<String>) {
            Loggers.report.info("report from {} : \n {}",p , args)
        })

    }
}