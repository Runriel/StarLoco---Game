package org.starloco.locos.game.world.scheduler.entity;

import org.starloco.locos.game.client.Player;
import org.starloco.locos.game.common.SocketManager;
import org.starloco.locos.database.Database;
import org.starloco.locos.game.entity.Prism;
import org.starloco.locos.game.entity.monster.Monster;
import org.starloco.locos.core.network.client.GameServer;
import org.starloco.locos.game.world.scheduler.Updatable;
import org.starloco.locos.game.world.world.World;
import org.starloco.locos.core.config.Config;
import org.starloco.locos.game.object.GameObject;

import java.util.ArrayList;

public class WorldSave extends Updatable {

    public final static Updatable updatable = new WorldSave(1800000);
    private static Thread thread;

    private WorldSave(int wait) {
        super(wait);
    }

    @Override
    public void update() {
        if(this.verify())
            if (!Config.INSTANCE.isSaving()) {
                thread = new Thread(() -> WorldSave.cast(1));
                thread.setName(WorldSave.class.getName());
                thread.setDaemon(true);
                thread.start();
            }
    }

    public static void cast(int trys) {
        if(trys != 0) GameServer.INSTANCE.setState(2);

        try {
            World.world.logger.debug("Starting the save of the world..");
            SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1164;");
            Config.INSTANCE.setSaving(true);

            /** Save of data **/
            World.world.logger.info("-> of accounts.");
            World.world.getAccounts().stream().filter(account -> account != null).forEach(account -> Database.getStatics().getAccountData().update(account));

            World.world.logger.info("-> of players.");
            World.world.logger.info("-> of members of guilds.");
            World.world.getPlayers().stream().filter(player -> player != null).filter(Player::isOnline).forEach(player -> {
                Database.getStatics().getPlayerData().update(player);
                if (player.getGuildMember() != null)
                    Database.getDynamics().getGuildMemberData().update(player);
            });

            World.world.logger.info("-> of prisms.");
            for (Prism prism : World.world.getPrisms().values())
                if (World.world.getMap(prism.getMap()).getSubArea().getPrismId() != prism.getId())
                    Database.getDynamics().getPrismData().delete(prism.getId());
                else
                    Database.getDynamics().getPrismData().update(prism);

            World.world.logger.info("-> of guilds.");
            World.world.getGuilds().values().stream().forEach(guild -> Database.getStatics().getGuildData().update(guild));

            World.world.logger.info("-> of collectors.");
            World.world.getCollectors().values().stream().filter(collector -> collector.getInFight() <= 0).forEach(collector -> Database.getDynamics().getCollectorData().update(collector));

            World.world.logger.info("-> of houses.");
            World.world.getHouses().values().stream().filter(house -> house.getOwnerId() > 0).forEach(house -> Database.getDynamics().getHouseData().update(house));

            World.world.logger.info("-> of trunks.");
            World.world.getTrunks().values().stream().forEach(trunk -> Database.getDynamics().getTrunkData().update(trunk));

            World.world.logger.info("-> of parks.");
            World.world.getMountparks().values().stream().filter(mp -> mp.getOwner() > 0 || mp.getOwner() == -1).forEach(mp -> Database.getDynamics().getMountParkData().update(mp));

            World.world.logger.info("-> of mounts.");
            World.world.getMounts().values().stream().forEach(mount -> Database.getStatics().getMountData().update(mount));

            World.world.logger.info("-> of areas.");
            World.world.getAreas().values().stream().forEach(area -> Database.getDynamics().getAreaData().update(area));
            World.world.getSubAreas().values().stream().forEach(subArea -> Database.getDynamics().getSubAreaData().update(subArea));

            World.world.logger.info("-> of objects.");
            try {
                for (GameObject object : new ArrayList<>(World.world.getGameObjects())) {
                    if (object == null) continue;
                    if (object.getModification() == 0)
                        Database.getStatics().getObjectData().insert(object);
                    else if (object.getModification() == 1)
                        Database.getStatics().getObjectData().update(object);
                    object.setModification(-1);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

            /*World.world.logger.info("-> of group-monsters.");
            if(Config.INSTANCE.getHEROIC) {
                for (GameMap map : World.world.getMaps())
                    map.getMobGroups().values().stream()
                            .filter(group -> !group.getObjects().isEmpty())
                            .forEach(group -> Database.getDynamics().getHeroicMobsGroups().update(map.getId(), group));
                Database.getDynamics().getHeroicMobsGroups().updateFix();
            }*/
            /** end save of data **/

            World.world.logger.debug("The save has been doing successfully !");
            SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1165;");
        } catch (Exception exception) {
            exception.printStackTrace();
            World.world.logger.error("Error when trying save of the world : " + exception.getMessage());
            if (trys < 10) {
                World.world.logger.error("Fail of the save, num of try : " + (trys + 1) + ".");
                WorldSave.cast(trys + 1);
                return;
            }
            Config.INSTANCE.setSaving(false);
        } finally {
            Config.INSTANCE.setSaving(false);
        }

        if(trys != 0) GameServer.INSTANCE.setState(1);

        if(thread != null) {
            World.world.getMaps().stream().filter(map -> map != null && map.getMobGroups() != null)
                    .forEach(map -> map.getMobGroups().values()
                            .stream().filter(group -> group != null).forEach(Monster.MobGroup::addStarBonus));
            Thread copy = thread;
            thread = null;
            copy.interrupt();
        }
    }

    @Override
    public GameObject get() {
        return null;
    }
}