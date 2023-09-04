package assimilation.commands;

import arc.util.Log;
import assimilation.AssimilationPlugin;
import assimilation.hex.Hex;
import assimilation.hex.HexData;
import assimilation.hex.Map;
import assimilation.utils.Utils;
import mindustry.core.GameState;

import static assimilation.PluginVars.serverCommands;
import static mindustry.Vars.*;

public class ServerCommands {

    public static void init(AssimilationPlugin assimilation) {

        serverCommands.register("test", "desc", args -> {
            Log.info("tested");
        });

        serverCommands.register("reply", "[echo_str]", "desc", args -> {
            Log.info("reply");
            Log.info(args[0]);
            Log.info("end of reply");
        });

        serverCommands.register("bing", "desc", args -> {
            Log.info("bong");
        });

        serverCommands.register("hexed", "Begin hosting with the Hexed gamemode.", args -> {
            if(!state.is(GameState.State.menu)){
                Log.err("Stop the server first.");
                return;
            }

            HexData hexData = new HexData();

            logic.reset();
            Log.info("Generating map...");
            Map generator = new Map();
            world.loadGenerator(Hex.size, Hex.size, generator);
            hexData.initHexes(generator.getHex());
            Utils.info("Map generated.");
            assimilation.setRules();
            logic.play();
            netServer.openServer();
        });

        serverCommands.register("time", "Get the time the ground has gone for.", args -> {
            Log.info("Time round has gone for: &lc@ minutes", (int)(AssimilationPlugin.roundTime) / 60 / 60);
        });
    }
}