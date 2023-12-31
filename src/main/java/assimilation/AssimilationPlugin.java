package assimilation;

import arc.*;
import arc.struct.*;
import arc.util.*;
import assimilation.commands.ClientCommands;
import assimilation.commands.ServerCommands;
import assimilation.hex.HexLogic;
import assimilation.hex.Hex;
import assimilation.hex.HexTeam;
import assimilation.hex.Map;
import assimilation.utils.Utils;
import assimilation.utils.GameUtils;
import mindustry.content.*;
import mindustry.core.GameState;
import mindustry.core.NetServer.*;
import mindustry.core.Version;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.type.*;

import static arc.util.Log.*;
import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.*;

public class AssimilationPlugin extends Plugin implements ApplicationListener {
    //in seconds
    public static final float spawnDelay = 60 * 4;
    //health requirement needed to capture a hex; no longer used
    public static final float healthRequirement = 3500;
    //item requirement to captured a hex
    public static final int itemRequirement = 210;

    public static final int messageTime = 1;
    //in ticks: 60 minutes
    public final static int roundTime = 60 * 60 * 90;
    //in ticks: 3 minutes
    final static int leaderboardTime = 60 * 60 * 2;

    final static int updateTime = 60 * 2;

    private final static int winCondition = 10;

    final static int timerBoard = 0;
    final static int timerUpdate = 1;
    private final static int timerWinCheck = 2;

    private final Rules rules = new Rules();
    Interval interval = new Interval(5);
    public boolean restarting = false;
    private boolean registered = false;

    double counter = 0f;

    @Override
    public void init() {

        Version.build = -1;

        AssimilationEvents.init(this);
        this.config();

        Events.run(Trigger.update, () -> {

            if(GameUtils.isActive()){
                HexLogic.updateStats();

                for(Player player : Groups.player){

                    if (HexLogic.playersByHexTeam.get(player.id) == null) {

                        continue;
                    }

                    if(player.team() != Team.green && player.team().cores().isEmpty()){
                        player.clearUnit();
                        GameUtils.killTiles(player.team());
                        Call.sendMessage("[yellow](!)[] [accent]" + player.name + "[lightgray] has been eliminated![yellow] (!)");
                        Call.infoMessage(player.con, "Your cores have been destroyed. You are defeated.");
                        player.team(Team.green);
                    }

                    if(player.team() == Team.green){
                        player.clearUnit();
                    }else if(HexLogic.getControlled(player).size == HexLogic.hexes().size){
                        GameUtils.endGame(this);
                        break;
                    }
                }

                if(interval.get(timerBoard, leaderboardTime)){
                    Call.infoToast(UI.getLeaderboard(), 15f);
                }

                if(interval.get(timerUpdate, updateTime)){
                    HexLogic.updateControl();
                }

                if(interval.get(timerWinCheck, 60 * 2)){
                    Seq<Player> players = HexLogic.getLeaderboard();
                    if(!players.isEmpty() && HexLogic.getControlled(players.first()).size >= winCondition && players.size > 1 && HexLogic.getControlled(players.get(1)).size <= 1){
                        GameUtils.endGame(this);
                    }
                }

                counter += Time.delta;

                //kick everyone and restart w/ the script
                if(counter > roundTime && !restarting){
                    GameUtils.endGame(this);
                }
            }else{
                counter = 0;
            }
        });

        TeamAssigner prev = netServer.assigner;
        netServer.assigner = (player, players) -> {
            Seq<Player> playerSeq = Seq.with(players);

            if (!GameUtils.isActive()) return prev.assign(player, players);

            // Try to put them back in the team they came from, if they had one
            HexTeam playerHexTeam = HexLogic.playersByHexTeam.get(player.id);
            Utils.info("returning team " + playerHexTeam);
            if (playerHexTeam != null) {

                return playerHexTeam.team;
            }

            //pick first inactive team
            for(Team team : Team.all) {
                if (team.id <= 5) continue;
                if (team.active()) continue;
                if (playerSeq.contains(p -> p.team().id == team.id)) continue;

                HexTeam hexTeam = HexLogic.getTeamHexTeam(team);
                if (hexTeam != null && !hexTeam.dying) continue;

                hexTeam = new HexTeam(team);
                hexTeam.addPlayer(player);

                return team;
            }

            Call.infoMessage(player.con, "There are currently no empty hex spaces available.\nAssigning into spectator mode.");
            return Team.derelict;
        };
    }

    public void config() {
        Utils.info("config");

        serverName.set("\uF869 [red]\uE861 [purple]Assimilation [grey]> [red]Hex [white]PVP [grey]> [white]Serpulo");
        desc.set("Conquer every [red]hex [grey]on the map");
        motd.set("Welcome to [purple]Assimilation [red]Hex [white]PVP!\n[white]your goal is to convert all cores under your control. Good Luck.");
        allowCustomClients.set(true);
        antiSpam.set(true);
        interactRateWindow.set(1);
        interactRateLimit.set(25);
        interactRateKick.set(50);
        messageSpamKick.set(5);
        packetSpamLimit.set(500);
        messageRateLimit.set(1);
        enableVotekick.set(false);
        // startCommands.set("hexed");

    }

    public void setRules() {

        state.rules.pvp = true;
        state.rules.tags.put("hexed", "true");
        state.rules.canGameOver = false;
        state.rules.loadout = ItemStack.list(Items.copper, 1200, Items.lead, 1000, Items.graphite, 150, Items.metaglass, 150, Items.silicon, 150, Items.titanium, 150);
        state.rules.buildSpeedMultiplier = 1.5f;
        state.rules.unitDamageMultiplier = .5f;
        state.rules.unitHealthMultiplier = .5f;
        state.rules.blockDamageMultiplier = .5f;
        state.rules.blockHealthMultiplier = 1f;
        state.rules.coreIncinerates = true;
        state.rules.damageExplosions = false;
        state.rules.disableOutsideArea = true;
        state.rules.borderDarkness = true;
        state.rules.ghostBlocks = true;
        state.rules.hideBannedBlocks = true;
        state.rules.logicUnitBuild = false;
        state.rules.mission = "Capture every hex";
        state.rules.modeName = "Assimilate";
        // state.rules.onlyDepositCore = true;
        state.rules.reactorExplosions = true;
        state.rules.unitCap = 24;
        state.rules.unitCrashDamageMultiplier = .0f;
        state.rules.unitCapVariable = true;
        // state.rules.bannedBlocks = new ObjectSet<>("");
        state.rules.coreCapture = true;
        state.rules.pvpAutoPause = false;
        state.rules.enemyCoreBuildRadius = Hex.radius * 8;
        state.rules.defaultTeam = Team.green;
    }

    @Override
    //run code that needs to be run every tick
    public void update(){

        info("start tick");

        // TickManager.run(this);

        info("end tick");
    }

    @Override
    public void registerServerCommands(CommandHandler handler){

        PluginVars.serverCommands = handler;
        ServerCommands.init(this);
    }

    @Override
    public void registerClientCommands(CommandHandler handler){

/*
        if (registered) return;
        registered = true;
*/

        PluginVars.clientCommands = handler;
        ClientCommands.init(this);
    }

    @Override
    //run code that shuts the server down
    public void dispose(){

    }

    public void initHexed() {

        if(!state.is(GameState.State.menu)){
            Log.err("Stop the server first.");
            return;
        }

        logic.reset();
        Log.info("Generating map...");
        Map generator = new Map();
        world.loadGenerator(Hex.size, Hex.size, generator);
        HexLogic.initHexes(generator.getHex());
        Utils.info("Map generated.");
        this.setRules();
        logic.play();
        netServer.openServer();
    }
}