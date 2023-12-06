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
            if (existingHexTeam != null) {

                // Let the player stay on the team
                return;
            }


        });

        Events.on(HexLogic.HexCaptureEvent.class, event -> {

        });

        Events.on(HexLogic.HexMoveEvent.class, event -> {

        });
    }
}