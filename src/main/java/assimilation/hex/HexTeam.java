package assimilation.hex;

import arc.util.Nullable;
import arc.util.Timekeeper;
import assimilation.AssimilationPlugin;
import assimilation.utils.Utils;
import mindustry.game.Team;
import mindustry.gen.Player;

import java.util.LinkedHashSet;

public class HexTeam {
    public int teamId;
    public boolean dying;
    public boolean chosen;
    public @Nullable Hex location;
    public boolean lastCaptured;
    public Timekeeper lastMessage = new Timekeeper(AssimilationPlugin.messageTime);
    public LinkedHashSet<Integer> playerIds = new LinkedHashSet<>();

    public HexTeam(Team team) {

        teamId = team.id;
        HexLogic.teamData[team.id] = this;

        Utils.info("Made hexTeam for " + team.name);
    }

    public void addPlayer(Player player) {

        playerIds.add(player.id);
        HexLogic.playersByHexTeam.put(player.id, this);
    }

    public void removePlayer(Player player) {

        playerIds.remove(player.id);
        HexLogic.playersByHexTeam.remove(player.id);
    }
}
