package assimilation.commands;

import arc.math.Mathf;
import arc.util.CommandHandler.CommandRunner;
import arc.util.Log;
import arc.util.Time;
import assimilation.AssimilationPlugin;
import assimilation.UI;
import assimilation.hex.Hex;
import assimilation.hex.HexData;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.world.Tile;
import assimilation.utils.GameUtils;

import static assimilation.PluginVars.clientCommands;
import static assimilation.PluginVars.serverCommands;
import static mindustry.Vars.state;
import static mindustry.Vars.world;

public class ClientCommands {

    public static void init(HexData hexData) {

        registerCommand("rules", "provides info on specific configurations for this server", (args, player) -> {
            player.sendMessage("max interaction rate: 1/sec");
        });

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

        registerCommand("die", "surrender all of your hexes to the Terran faction", (args, player) -> {

            if(player.team() == Team.green){
                player.sendMessage("[scarlet]You're already terran.");
                return;
            }

            GameUtils.killTiles(player.team(), hexData);
            player.unit().kill();
        });

        registerCommand("spectate", "Enter spectator mode. This destroys your base.", (args, player) -> {
            if(player.team() == Team.green){
                player.sendMessage("[scarlet]You're already terran.");
                return;
            }

            GameUtils.killTiles(player.team(), hexData);
            player.unit().kill();
            player.team(Team.green);
        });

        registerCommand("hexes", "Dispay the number of hexes you have captured.", (args, player) -> {

            player.sendMessage("[lightgray]Your team has captured[accent] " + hexData.getControlled(player).size + "[] hexes.");
        });

        registerCommand("leaderboard", "Display the leaderboard", (args, player) -> {
            player.sendMessage(UI.getLeaderboard(hexData));
        });

        registerCommand("hexstatus", "Get hex status at your position.", (args, player) -> {
            Hex hex = hexData.data(player).location;
            if(hex != null){
                hex.updateController();
                StringBuilder builder = new StringBuilder();
                builder.append("| [lightgray]Hex #").append(hex.id).append("[]\n");
                builder.append("| [lightgray]Owner:[] ").append(hex.controller != null && hexData.getPlayer(hex.controller) != null ? hexData.getPlayer(hex.controller).name : "<none>").append("\n");
                for(Teams.TeamData data : state.teams.getActive()){
                    if(hex.getProgressPercent(data.team) > 0){
                        builder.append("|> [accent]").append(hexData.getPlayer(data.team).name).append("[lightgray]: ").append((int)hex.getProgressPercent(data.team)).append("% captured\n");
                    }
                }
                player.sendMessage(builder.toString());
            }else{
                player.sendMessage("[scarlet]No hex found.");
            }
        });

        registerCommand("time", "Get the time the ground has gone for.", (args, player) -> {
            player.sendMessage("Time round has gone for: " + ((AssimilationPlugin.roundTime) / 60 / 60) + " minutes");
        });
    }

    public static void registerCommand(String commandName, String description, CommandRunner<Player> commander) {

       clientCommands.<Player>register(commandName, description, (args, player) -> {

           commander.accept(args, player);
       });
    }
}