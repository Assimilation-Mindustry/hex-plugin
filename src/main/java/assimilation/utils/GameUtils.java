package assimilation.utils;

import arc.math.Mathf;
import arc.util.Time;
import assimilation.hex.HexData;
import mindustry.game.Team;
import mindustry.world.Tile;

import static mindustry.Vars.world;

public class GameUtils {

    public static void killTiles(Team team, HexData hexData){
        hexData.data(team).dying = true;
        Time.runTask(8f, () -> hexData.data(team).dying = false);
        for(int x = 0; x < world.width(); x++){
            for(int y = 0; y < world.height(); y++){
                Tile tile = world.tile(x, y);
                if(tile.build != null && tile.team() == team){
                    Time.run(Mathf.random(60f * 6), tile.build::kill);
                }
            }
        }
    }
}