package assimilation.utils;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import assimilation.AssimilationPlugin;
import assimilation.PluginVars;
import assimilation.hex.HexLogic;
import mindustry.content.Blocks;
import mindustry.core.GameState;
import mindustry.game.Schematic;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Packets;
import mindustry.type.ItemStack;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.*;

public class GameUtils {

    public static void killTiles(Team team){
        HexLogic.getTeamHexTeam(team).dying = true;
        Time.runTask(8f, () -> HexLogic.getTeamHexTeam(team).dying = false);
        for(int x = 0; x < world.width(); x++){
            for(int y = 0; y < world.height(); y++){
                Tile tile = world.tile(x, y);
                if(tile.build != null && tile.team() == team){
                    Time.run(Mathf.random(60f * 6), tile.build::kill);
                }
            }
        }
    }

    public static void endGame(AssimilationPlugin assimilation){
        if(assimilation.restarting) return;

        assimilation.restarting = true;
        Seq<Player> players = HexLogic.getLeaderboard();
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < players.size && i < 3; i++){
            if(HexLogic.getControlled(players.get(i)).size > 1){
                builder.append("[yellow]").append(i + 1).append(".[accent] ").append(players.get(i).name)
                        .append("[lightgray] (x").append(HexLogic.getControlled(players.get(i)).size).append(")[]\n");
            }
        }

        if(!players.isEmpty()){
            boolean dominated = HexLogic.getControlled(players.first()).size == HexLogic.hexes().size;

            for(Player player : Groups.player){
                Call.infoMessage(player.con, "[accent]--ROUND OVER--\n\n[lightgray]"
                        + (player == players.first() ? "[accent]You[] were" : "[yellow]" + players.first().name + "[lightgray] was") +
                        " victorious, with [accent]" + HexLogic.getControlled(players.first()).size + "[lightgray] hexes conquered." + (dominated ? "" : "\n\nFinal scores:\n" + builder));
            }
        }

        Utils.info("&ly--SERVER RESTARTING--");
        Time.runTask(60f * 10f, () -> {
            netServer.kickAll(Packets.KickReason.serverRestarting);
            Time.runTask(5f, () -> System.exit(2));
        });
    }

    public static void loadout(Player player, int x, int y){
        Schematic.Stile coreTile = PluginVars.coreNucleus.tiles.find(s -> s.block instanceof CoreBlock);
        if(coreTile == null) throw new IllegalArgumentException("Schematic has no core tile. Exiting.");
        int ox = x - coreTile.x, oy = y - coreTile.y;
        PluginVars.coreNucleus.tiles.each(st -> {
            Tile tile = world.tile(st.x + ox, st.y + oy);
            if(tile == null) return;

            if(tile.block() != Blocks.air){
                tile.removeNet();
            }

            tile.setNet(st.block, player.team(), st.rotation);

            if(st.config != null){
                tile.build.configureAny(st.config);
            }
            if(tile.block() instanceof CoreBlock){
                for(ItemStack stack : state.rules.loadout){
                    Call.setItem(tile.build, stack.item, stack.amount);
                }
            }
        });
    }

    public static boolean isActive() {
        return state.rules.tags.getBool("hexed") && !state.is(GameState.State.menu);
    }
}