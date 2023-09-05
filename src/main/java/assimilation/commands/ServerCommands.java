package assimilation.commands;

import arc.util.Log;
import assimilation.AssimilationPlugin;

import static assimilation.PluginVars.serverCommands;

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

            assimilation.initHexed();
        });

        serverCommands.register("time", "Get the time the ground has gone for.", args -> {
            Log.info("Time round has gone for: &lc@ minutes", (int)(AssimilationPlugin.roundTime) / 60 / 60);
        });
    }
}