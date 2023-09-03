package assimilation.hex;

import arc.math.geom.Intersector;
import arc.util.Nullable;
import arc.util.Timekeeper;
import assimilation.AssimilationPlugin;
import mindustry.game.Team;
import mindustry.game.Teams.TeamData;
import mindustry.gen.Groups;
import mindustry.type.ItemStack;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

import java.util.Arrays;

import static mindustry.Vars.*;

public class Hex{
    private float[] progress = new float[256];

    public final static int size = 695;
    public final static int diameter = 80;
    public final static int radius = diameter / 2;
    public final static int spacing = diameter + 10;
    public final static int itemRequirement = 210;

    public final int id;
    public final int x, y;
    public final float wx, wy;
    public final float rad = radius * tilesize;

    public @Nullable Team controller;
    public Timekeeper spawnTime = new Timekeeper(AssimilationPlugin.spawnDelay);

    public Hex(int id, int x, int y){
        this.id = id;
        this.x = x;
        this.y = y;
        wx = x * tilesize;
        wy = y * tilesize;
    }

    public void updateController(){
        controller = findController();
    }

    public float getProgressPercent(Team team){
        return progress[team.id] / itemRequirement * 100;
    }

    public float getProgress(Team team){
        return progress[team.id];
    }

    public boolean hasCore(){
        return world.tile(x, y).team() != Team.derelict && world.tile(x, y).block() instanceof CoreBlock;
    }

    public @Nullable Team findController(){
        if(hasCore()){
            return world.tile(x, y).team();
        }

        Arrays.fill(progress, 0);
        Groups.unit.intersect(wx - rad, wy - rad, rad*2, rad*2).each(e -> {
            if(contains(e.x, e.y)){
                progress[e.team.id] += e.health / 10f;
            }
        });

        for(int cx = x - radius; cx < x + radius; cx++){
            for(int cy = y - radius; cy < y + radius; cy++){
                Tile tile = world.tile(cx, cy);
                if(tile != null && tile.synthetic() && contains(tile) && tile.block().requirements != null){
                    for(ItemStack stack : tile.block().requirements){
                        progress[tile.team().id] += stack.amount * stack.item.cost;
                    }
                }
            }
        }

        TeamData data = state.teams.getActive().max(t -> progress[t.team.id]);
        if(data != null && data.team != Team.derelict && progress[data.team.id] >= itemRequirement){
            return data.team;
        }
        return null;
    }

    public boolean contains(float x, float y){
        return Intersector.isInsideHexagon(wx, wy, rad * 2, x, y);
    }

    public boolean contains(Tile tile){
        return contains(tile.worldx(), tile.worldy());
    }
}