package assimilation.hex;

import arc.func.Cons;
import arc.math.Mathf;
import arc.math.geom.Bresenham2;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Structs;
import arc.util.Tmp;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.maps.filters.GenerateFilter;
import mindustry.maps.filters.GenerateFilter.GenerateInput;
import mindustry.maps.filters.OreFilter;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import java.lang.Math;

import static mindustry.Vars.maps;
import static mindustry.Vars.state;

public class Map implements Cons<Tiles> {
    public static int size = Hex.size, height = Hex.size;
    public int center = size / 2;

    // 5x6
    // elevation --->
    // temperature
    // |
    // v
    Block[][] floors = {
            {Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.grass},
            {Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.grass, Blocks.grass},
            {Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.grass, Blocks.shale},
            {Blocks.darksandTaintedWater, Blocks.darksandTaintedWater, Blocks.moss, Blocks.moss, Blocks.sporeMoss, Blocks.stone},
            {Blocks.ice, Blocks.iceSnow, Blocks.snow, Blocks.dacite, Blocks.hotrock, Blocks.salt}
    };

    Block[][] blocks = {
            {Blocks.shaleWall, Blocks.shaleWall, Blocks.rhyoliteWall, Blocks.rhyoliteWall, Blocks.dirtWall, Blocks.dirtWall},
            {Blocks.shaleWall, Blocks.shaleWall, Blocks.rhyoliteWall, Blocks.rhyoliteWall, Blocks.dirtWall, Blocks.dirtWall},
            {Blocks.shaleWall, Blocks.shaleWall, Blocks.rhyoliteWall, Blocks.rhyoliteWall, Blocks.dirtWall, Blocks.dirtWall},
            {Blocks.sporeWall, Blocks.sporeWall, Blocks.stoneWall, Blocks.stoneWall, Blocks.ferricStoneWall, Blocks.ferricStoneWall},
            {Blocks.sporeWall, Blocks.sporeWall, Blocks.stoneWall, Blocks.stoneWall, Blocks.ferricStoneWall, Blocks.ferricStoneWall},
//            {Blocks.stoneWall, Blocks.stoneWall, Blocks.sandWall, Blocks.sandWall},
//            {Blocks.stoneWall, Blocks.stoneWall, Blocks.duneWall, Blocks.duneWall},
//            {Blocks.stoneWall, Blocks.stoneWall, Blocks.duneWall, Blocks.duneWall},
//            {Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall, Blocks.stoneWall},
//            {Blocks.stoneWall, Blocks.sandWall, Blocks.duneWall, Blocks.dirtWall, Blocks.daciteWall, Blocks.yellowStoneWall, Blocks.rhyoliteWall, Blocks.regolithWall, Blocks.ferricStoneWall, Blocks.carbonWall, Blocks.beryllicStoneWall, Blocks.shaleWall},
    };

    @Override
    public void get(Tiles tiles){
        int seed1 = Mathf.random(0, 10000), seed2 = Mathf.random(0, 10000);
        Seq<GenerateFilter> ores = new Seq<>();
        maps.addDefaultOres(ores);
        ores.each(o -> ((OreFilter)o).threshold -= 0.05f);
        ores.insert(0, new OreFilter(){{
            ore = Blocks.oreScrap;
            scl += 2 / 2.1F;
        }});
        ores.each(GenerateFilter::randomize);
        GenerateInput in = new GenerateInput();
        IntSeq hex = getHex();

        for(int x = 0; x < size; x++){
            for(int y = 0; y < height; y++){
                int temp = Mathf.clamp((int)((Simplex.noise2d(seed1, 12, 0.6, 1.0 / 400, x, y) - 0.5) * 10 * blocks.length), 0, blocks.length-1);
                int elev = Mathf.clamp((int)(((Simplex.noise2d(seed2, 12, 0.6, 1.0 / 700, x, y) - 0.5) * 10 + 0.15f) * blocks[0].length), 0, blocks[0].length-1);

                Block floor = Blocks.darksand;
                Block wall = blocks[temp][elev];
                Block ore = Blocks.air;

                for(GenerateFilter f : ores){
                    in.floor = Blocks.stone;
                    in.block = wall;
                    in.overlay = ore;
                    in.x = x;
                    in.y = y;
                    in.width = in.height = Hex.size;
                    f.apply(in);
                    if(in.overlay != Blocks.air){
                        ore = in.overlay;
                    }
                }

                tiles.set(x, y, new Tile(x, y, floor.id, ore.id, wall.id));
            }
        }

        for(int i = 0; i < hex.size; i++){
            int x = Point2.x(hex.get(i));
            int y = Point2.y(hex.get(i));
            Geometry.circle(x, y, size, height, Hex.diameter, (cx, cy) -> {

                if (this.isInsideCircle(x, y, Hex.diameter, cx, cy)) {

                    Tile tile = tiles.getn(cx, cy);
                    tile.setBlock(Blocks.air);
                }
            });
            float angle = 360f / 3 / 2f - 90;
            for(int a = 0; a < 3; a++){
                float f = a * 120f + angle;

                Tmp.v1.trnsExact(f, Hex.spacing + 12);
                if(Structs.inBounds(x + (int)Tmp.v1.x, y + (int)Tmp.v1.y, size, height)){
                    Tmp.v1.trnsExact(f, Hex.spacing / 2 + 7);
                    Bresenham2.line(x, y, x + (int)Tmp.v1.x, y + (int)Tmp.v1.y, (cx, cy) -> {
                        Geometry.circle(cx, cy, size, height, 3, (c2x, c2y) -> tiles.getn(c2x, c2y).setBlock(Blocks.air));
                    });
                }
            }
        }

        for(int x = 0; x < size; x++) {
            for (int y = 0; y < height; y++) {

                Tile tile = tiles.get(x, y);


            }
        }

        state.map = new mindustry.maps.Map(StringMap.of("name", "Hex"));
    }

    private boolean isInsideCircle(int x, int y, float diameter, int cx, int cy) {

        double distance = Math.sqrt(Math.pow(cx - x, 2) + Math.pow(cy - y, 2));

        return distance < (diameter / 2);
    }

    public IntSeq getHex(){
        IntSeq array = new IntSeq();
        double h = Math.sqrt(3) * Hex.spacing/2;
        //base horizontal spacing=1.5w
        //offset = 3/4w
        for(int x = 0; x < size / Hex.spacing - 2; x++){
            for(int y = 0; y < height / (h/2) - 2; y++){
                int cx = (int)(x * Hex.spacing*1.5 + (y%2)* Hex.spacing*3.0/4) + Hex.spacing/2;
                int cy = (int)(y * h / 2) + Hex.spacing/2;
                array.add(Point2.pack(cx, cy));
            }
        }
        return array;
    }
}
