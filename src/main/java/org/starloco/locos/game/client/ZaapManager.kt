package org.starloco.locos.game.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.starloco.locos.game.area.map.GameMap
import org.starloco.locos.game.common.Formulas
import org.starloco.locos.game.world.world.World
import java.util.*

/**
 * Created by guillaume on 3/14/17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ZaapManager {
    private @JsonProperty var zaaps : MutableSet<Short> = TreeSet()

    /**
     * Add a zaap to the list of known zaap
     * @param id zaap id
     * @return true if the zaap wasn't known, false otherwise
     */
    fun learnZaap(id: Short): Boolean {
        return zaaps.add(id)
    }

    /**
     * @param id zaap id
     * @return true if the zaap is known, false otherwise
     */
    fun isZaapKnown(id: Short): Boolean {
        return zaaps.contains(id)
    }

    /**
     * Zaap packet used when you try to use a zaap (include cost)
     *
     * @return a packet describing known zaap and their cost
     */
    fun getZaapPacket(str: StringBuilder, currentMap: GameMap) {
        for (i in zaaps) {
            if (World.world.getMap(i) == null)
                continue
            if (World.world.getMap(i).subArea.area.superArea != currentMap.subArea.area.superArea)
                continue
            var cost = Formulas.calculZaapCost(currentMap, World.world.getMap(i))
            if (i == currentMap.id)
                cost = 0
            str.append("|").append(i.toInt()).append(";").append(cost)
        }
    }

    @Deprecated("Change for storing as json")
    fun knownZaapToDbFormat(): String {
        val str = StringBuilder()
        var first = true

        if (zaaps.isEmpty())
            return ""
        for (i in zaaps) {
            if (!first)
                str.append(",")
            first = false
            str.append(i)
        }
        return str.toString()
    }

}
