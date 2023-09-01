package assimilation.commands;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Call;
import mindustry.gen.Player;

import static assimilation.PluginVars.clientCommands;

public class ClientCommands {

    public static void init() {

        registerCommand("bing", "provides feedback to the caller", (args, player) -> {

            player.sendMessage("bong");
        });

        registerCommand("discord", "provides UI with link to join the server's discord server", (args, player) -> {

            Call.openURI(player.con, "https://discord.gg/4WkGkvzVYs");
            /* player.sendMessage("Hello, " + player.name + "!"); */
        });

        registerCommand("invincible", "gives the player infinite health", (args, player) -> {

            player.unit().health(1.0E8f);
            player.sendMessage("Made you invindible");
        });

        registerCommand("die", "commits die", (args, player) -> {

            player.unit().kill();
            player.sendMessage("you died");
        });
    }

    public static void registerCommand(String commandName, String description, CommandRunner<Player> commander) {

       clientCommands.<Player>register(commandName, description, (args, player) -> {

           commander.accept(args, player);
       });
    }
}