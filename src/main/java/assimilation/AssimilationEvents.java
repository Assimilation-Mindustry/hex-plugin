package assimilation;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import assimilation.hex.Hex;
import assimilation.hex.HexLogic;
import assimilation.hex.HexTeam;
import assimilation.hex.Map;
import assimilation.utils.GameUtils;
import assimilation.utils.Utils;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.world.blocks.storage.CoreBlock;

public class AssimilationEvents {
    public static void init(AssimilationPlugin assimilation) {

        Events.on(EventType.PlayerLeave.class, event -> {
            Utils.info("THIS PLAYER HAS LEFT: @", event.player.name());
        });

        Events.on(EventType.BlockDestroyEvent.class, event -> {
            //reset last spawn times so this hex becomes vacant for a while.
            if(event.tile.block() instanceof CoreBlock){
                Hex hex = HexLogic.getHex(event.tile.pos());

                if(hex != null){
                    //update state
                    hex.spawnTime.reset();
                    hex.updateController();
                }
            }
        });

        Events.on(EventType.PlayerLeave.class, event -> {
//            if(AssimilationPlugin.isActive() && event.player.team() != Team.derelict){
//                killTiles(event.player.team());
//            }


        });

//        Events.on(EventType.WithdrawEvent.class, event -> {
//
//            event.preventDefault();
//        });

        Events.on(EventType.PlayerJoin.class, event -> {

            if (!GameUtils.isActive()) return;
            if(event.player.team() == Team.green) return;

//            Utils.info("Player joined. Is team active: " + event.player.team().cores().isEmpty());
//            Utils.info("Player team core " + event.player.team().core());

            HexTeam existingHexTeam = HexLogic.playersByHexTeam.get(event.player.id);
            Utils.info("Player joined, team: " + existingHexTeam);

            // If they already have a hex team
            if (existingHexTeam == null) {

                // Let the player stay on the team
                return;
            }

            // Find a viable hex furthest from the center

            Seq<Hex> hexes = HexLogic.hexes().copy();
            hexes.shuffle();

            Hex bestHex = null;
            int bestRange = 0;

            for (Hex hex : hexes) {

                if (hex.controller != null) continue;
                if (!hex.spawnTime.get()) continue;

                int range = Utils.rangeXY(hex.x, hex.y, Map.size / 2, Map.size / 2);
                if (range < bestRange) continue;

                bestHex = hex;
                bestRange = range;
            }

            if (bestHex == null) {

                Call.infoMessage(event.player.con, "There are currently no viable hexes available.\nAssigning into spectator mode.");
                event.player.unit().kill();
                event.player.team(Team.green);
                return;
            }

            GameUtils.loadout(event.player, bestHex.x, bestHex.y);

            HexTeam hexTeam = new HexTeam(event.player.team());
            hexTeam.addPlayer(event.player);

            Core.app.post(() -> {

                HexLogic.teamData[hexTeam.teamId].chosen = false;
            });
            bestHex.findController();

            hexTeam.lastMessage.reset();
        });

        Events.on(HexLogic.HexCaptureEvent.class, event -> {

        });

        Events.on(HexLogic.HexMoveEvent.class, event -> {

        });
    }
}