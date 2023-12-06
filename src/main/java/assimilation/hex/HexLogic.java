package assimilation.hex;

import arc.Events;
import arc.math.geom.Point2;
import arc.struct.IntMap;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Nullable;
import assimilation.utils.Utils;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import java.util.Optional;

public class HexLogic {
    /** All hexes on the map. No order. */
    public static Seq<Hex> hexes = new Seq<>();
    /** Maps world pos -> hex */
    public static IntMap<Hex> hexPos = new IntMap<>();
    /** Maps team ID -> player */
    public static IntMap<Player> teamMap = new IntMap<>();
    /** Player Id -> HexTeam */
    public static IntMap<HexTeam> playersByHexTeam = new IntMap<>();
    /** Maps team ID -> list of controlled hexes */
    public static IntMap<Seq<Hex>> control = new IntMap<>();
    /** Data of specific teams. */
    public static HexTeam[] teamData = new HexTeam[256];

    public static void updateStats(){
        teamMap.clear();
        for(Player player : Groups.player){
            teamMap.put(player.team().id, player);
        }
        for(Seq<Hex> arr : control.values()){
            arr.clear();
        }

        for(Player player : Groups.player){
            if(player.dead()) continue;

            HexTeam team = getPlayerHexTeam(player);
            Hex newHex = hexes.min(h -> player.dst2(h.wx, h.wy));
            if(team.location != newHex){
                team.location = newHex;
                team.lastCaptured = newHex.controller == player.team();
                Events.fire(new HexMoveEvent(player));
            }

            boolean captured = newHex.controller == player.team();
            if(team.lastCaptured != captured){
                team.lastCaptured = captured;
                if(captured && !newHex.hasCore()){
                    Events.fire(new HexCaptureEvent(player, newHex));
                }
            }
        }

        for(Hex hex : hexes){
            if(hex.controller != null){
                if(!control.containsKey(hex.controller.id)){
                    control.put(hex.controller.id, new Seq<>());
                }
                control.get(hex.controller.id).add(hex);
            }
        }
    }

    public static void updateControl(){
        hexes.each(Hex::updateController);
    }

    /** Allocates a new array of players sorted by score, descending. */
    public static Seq<Player> getLeaderboard(){
        Seq<Player> players = new Seq<>();
        Groups.player.copy(players);
        players.sort(p -> -getControlled(p).size);
        return players;
    }

    public static @Nullable Player getPlayer(Team team){
        return teamMap.get(team.id);
    }

    public static Seq<Hex> getControlled(Player player){
        return getControlled(player.team());
    }

    public static Seq<Hex> getControlled(Team team){
        if(!control.containsKey(team.id)){
            control.put(team.id, new Seq<>());
        }
        return control.get(team.id);
    }

    public static void initHexes(IntSeq ints){
        for(int i = 0; i < ints.size; i++){
            int pos = ints.get(i);
            hexes.add(new Hex(i, Point2.x(pos), Point2.y(pos)));
            hexPos.put(pos, hexes.peek());
        }
    }

    public static Seq<Hex> hexes(){
        return hexes;
    }

    public static @Nullable Hex getHex(int position){
        return hexPos.get(position);
    }

    public static @Nullable HexTeam getTeamHexTeam(Team team){
        if(teamData[team.id] == null) {

            return null;
        }

        return teamData[team.id];
    }

    public static @Nullable HexTeam getPlayerHexTeam(Player player){
        return getTeamHexTeam(player.team());
    }

    public static class HexCaptureEvent{
        public final Player player;
        public final Hex hex;

        public HexCaptureEvent(Player player, Hex hex){
            this.player = player;
            this.hex = hex;
        }
    }

    public static class HexMoveEvent{
        public final Player player;

        public HexMoveEvent(Player player){
            this.player = player;
        }
    }
}