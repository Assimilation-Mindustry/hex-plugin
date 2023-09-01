package assimilation.commands;

import arc.util.Log;

import static assimilation.PluginVars.serverCommands;

public class ServerCommands {

    public static void init() {

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
    }
}