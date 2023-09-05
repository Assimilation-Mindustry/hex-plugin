package assimilation;

import arc.util.Time;
import assimilation.hex.HexLogic;
import assimilation.utils.GameUtils;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;

public class TickManager {
    public static void run(AssimilationPlugin assimilation) {

        if (!GameUtils.isActive()) return;

        HexLogic.updateStats();

        for(Player player : Groups.player){
            if(player.team() != Team.derelict && player.team().cores().isEmpty()){
                player.clearUnit();
                GameUtils.killTiles(player.team());
                Call.sendMessage("[yellow](!)[] [accent]" + player.name + "[lightgray] has been eliminated![yellow] (!)");
                Call.infoMessage(player.con, "Your cores have been destroyed. You are defeated.");
                player.team(Team.derelict);
            }

            if (HexLogic.getControlled(player).size == HexLogic.hexes().size){
                GameUtils.endGame(assimilation);
                break;
            }
        }

        if(assimilation.interval.get(AssimilationPlugin.timerBoard, AssimilationPlugin.leaderboardTime)){
            Call.infoToast(UI.getLeaderboard(), 15f);
        }

        if(assimilation.interval.get(AssimilationPlugin.timerUpdate, AssimilationPlugin.updateTime)){
            HexLogic.updateControl();
        }

        assimilation.counter += Time.delta;
    }
}
