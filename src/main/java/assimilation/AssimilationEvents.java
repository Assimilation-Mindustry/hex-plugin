package assimilation;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import arc.util.Time;
import assimilation.hex.Hex;
import assimilation.hex.HexData;
import assimilation.hex.Map;
import assimilation.utils.GameUtils;
import assimilation.utils.Utils;
import mindustry.content.Items;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.type.ItemStack;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.state;

public class AssimilationEvents {
    public static void init(AssimilationPlugin assimilation) {

        Events.on(EventType.PlayerLeave.class, event -> {
            Utils.info("THIS PLAYER HAS LEFT: @", event.player.name());
        });

        Events.on(EventType.BlockDestroyEvent.class, event -> {
            //reset last spawn times so this hex becomes vacant for a while.
            if(event.tile.block() instanceof CoreBlock){
                Hex hex = assimilation.hexData.getHex(event.tile.pos());

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

            Utils.info("Player joined. Is team active: " + event.player.team().active());
            // If team is still alive
            if (event.player.team().active()) {

                // Let the player stay on the team
                return;
            }

            // Find a viable hex furthest from the center

            Seq<Hex> hexes = assimilation.hexData.hexes().copy();
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
                event.player.team(Team.derelict);
                return;
            }

            GameUtils.loadout(event.player, bestHex.x, bestHex.y);
            Core.app.post(() -> assimilation.hexData.data(event.player).chosen = false);
            bestHex.findController();

            assimilation.hexData.data(event.player).lastMessage.reset();
        });

        Events.on(HexData.ProgressIncreaseEvent.class, event -> assimilation.updateText(event.player));

        Events.on(HexData.HexCaptureEvent.class, event -> assimilation.updateText(event.player));

        Events.on(HexData.HexMoveEvent.class, event -> assimilation.updateText(event.player));
    }
}