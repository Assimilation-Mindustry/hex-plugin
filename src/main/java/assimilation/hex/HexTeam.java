package assimilation.hex;

import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Timekeeper;
import assimilation.AssimilationPlugin;
import assimilation.utils.GameUtils;
import assimilation.utils.Utils;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Player;

import java.util.LinkedHashSet;

public class HexTeam {
    public Team team;
    public boolean dying;
    public @Nullable Hex location;
    public boolean lastCaptured;
    public Timekeeper lastMessage = new Timekeeper(AssimilationPlugin.messageTime);
    public LinkedHashSet<Integer> playerIds = new LinkedHashSet<>();

    public HexTeam(Team team) {

        HexLogic.teamData[team.id] = this;

        Utils.info("Made hexTeam for " + team.name);
    }

    private Team initHex(Player player) {

        // Find a viable hex furthest from the center

        Seq<Hex> hexes = HexLogic.hexes().copy();
        hexes.shuffle();

        Hex bestHex = null;
        int bestRange = 0;

        for (Hex hex : hexes) {

            if (hex.controller != null) continue;
            if (!hex.spawnTime.get()) continue;

            int range = Utils.rangeXY(hex.x, hex.y, Map.size / 2, Map.size / 2);
            if (range < bestRange) continue;

            bestHex = hex;
            bestRange = range;
        }

        if (bestHex == null) {

            Call.infoMessage(player.con, "There are currently no viable hexes available.\nAssigning into spectator mode.");
            player.unit().kill();
            player.team(Team.green);
            return Team.green;
        }

        GameUtils.loadout(player, bestHex.x, bestHex.y);

        bestHex.findController();

        lastMessage.reset();
        return this.team;
    }

    public void addPlayer(Player player) {

        team = player.team();
        playerIds.add(player.id);
        HexLogic.playersByHexTeam.put(player.id, this);
    }

    public void removePlayer(Player player) {

        playerIds.remove(player.id);
        HexLogic.playersByHexTeam.remove(player.id);
    }
}
