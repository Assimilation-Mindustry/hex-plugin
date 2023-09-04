package assimilation;

import assimilation.hex.HexData;
import mindustry.gen.Player;

public class UI {
    public static String getLeaderboard(HexData hexData){
        StringBuilder builder = new StringBuilder();
        builder.append("[accent]Leaderboard\n[scarlet]");
        int count = 0;
        for(Player player : hexData.getLeaderboard()){
            builder.append("[yellow]").append(++count).append(".[white] ")
                    .append(player.name).append("[orange] (").append(hexData.getControlled(player).size).append(" hexes)\n[white]");

            if(count > 4) break;
        }
        return builder.toString();
    }
}
