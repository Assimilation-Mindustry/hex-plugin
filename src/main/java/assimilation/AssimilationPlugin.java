package assimilation;

import arc.*;
import arc.struct.*;
import arc.util.*;
import assimilation.commands.ClientCommands;
import assimilation.hex.HexData;
import assimilation.hex.Hex;
import assimilation.hex.Map;
import assimilation.utils.Utils;
import assimilation.utils.GameUtils;
import mindustry.content.*;
import mindustry.core.GameState.*;
import mindustry.core.NetServer.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.game.Schematic.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Packets.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;

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
    private final static int roundTime = 60 * 60 * 90;
    //in ticks: 3 minutes
    private final static int leaderboardTime = 60 * 60 * 2;

    private final static int updateTime = 60 * 2;

    private final static int winCondition = 10;

    private final static int timerBoard = 0, timerUpdate = 1, timerWinCheck = 2;

    private final Rules rules = new Rules();
    private Interval interval = new Interval(5);

    public HexData hexData;
    private boolean restarting = false, registered = false;

    private Schematic coreNucleus;
    private Schematic coreShard;
    private double counter = 0f;

    @Override
    public void init() {

        AssimilationEvents.init();
        this.config();

        coreNucleus = Schematics.readBase64("bXNjaAF4nE2TbY6bQBBEG2yYLwzeG+QCPtEqP1g8iSyxgMDO7l49UuRMz7MS25Ifnqmu6mlAgrzsZD/171GaYV7jt+k2jPG2STjHbVgvy/UyTyJSj/1bHDcpX783EoZ5WeJ6+ujHUewwT7/i17zKrl8HMdvQX69xlfo9TudEv8wfSTvN5yjmLe99ib1N49zrdk49/Uvd5rFfT0s/xVFenv6c0tXPKKG/rKcf/XCdk4nIp/z/FKAEOxZqYIAFDniUB9CCDhyfHAsuCqwLtVbsQYWgBgZY4IBHGUADDqAFHTg+naDU2DJ/U2SpsfuEik5qYIAFDqS8KiHYVHL/rdJUXGSTBsuWgA4cHwOThzBVqDaZ76TKeTYZWhYC2y3iDmSHvZa63GRqodIBhYRGp1cUutEiy0UV6orjVizWLNa6qGhZzHuGBg0NGsaiHaUfo2NRhIo7ZPSkqmqpeTjkPi1GlvlaNdLTVbZMw5L7n/s9nzg1oTDAAge8lPuEgEGLXQdyhiPD8eg4DukYsuPRcdxKR5AjyBHkCHIapMpAQQMOoAUdyLGPR9oT64n1vAyeIE+QJ8gT5NF6rD3WHmuv1n8Bx4RLdw==");
        coreShard = Schematics.readBase64("bXNjaAF4nE2SXW7CMBAGNyT+2w136AU4UdUHA5aKFCAKSG2fevWyGSqBhCZZ+Rt7s5atbHsZLvXcRA/Xpb3dPutyFDu222E5zffT9SIicar7Nt1k8/4RxQ7XeW7L7qtOk9jtOtVlN9dLmyTPdXkU67fEc7sc2yJpX+/3tvwg3yEX+X38pZP1twE9KECBvazreOgIdAQ6GUAACWRQgIKnbHzZdONON25AT3EAAUSQQAYFKDAwvjTSo+5d7eKeYgARJJZkUCgqMLA6B3ofXPYYl1u88wgSxcxbYaWC1RKIB9oOfhZ3DP4csQQaC8QD8UA8koue6x4YQKZYwBpIbJQIJALJA37KTLGAZ2DdIZPLzCF7zr/KwFsAkWJiCpmAAnu5OwVZQVY4RGGohQEUBlAYQMFS/i3rJ3/eQEWmdKSMUZEpMkWm3BBlDuqncShFA6vaUBtqQ22oDbWhNpxGt0aHhtPwGM4R2YhsRDYSGAmMHvgD2Y4p3g==");

        Events.run(Trigger.update, () -> {
            if(active()){
                hexData.updateStats();

                for(Player player : Groups.player){
                    if(player.team() != Team.derelict && player.team().cores().isEmpty()){
                        player.clearUnit();
                        GameUtils.killTiles(player.team(), hexData);
                        Call.sendMessage("[yellow](!)[] [accent]" + player.name + "[lightgray] has been eliminated![yellow] (!)");
                        Call.infoMessage(player.con, "Your cores have been destroyed. You are defeated.");
                        player.team(Team.derelict);
                    }

                    if(player.team() == Team.derelict){
                        player.clearUnit();
                    }else if(hexData.getControlled(player).size == hexData.hexes().size){
                        endGame();
                        break;
                    }
                }

                if(interval.get(timerBoard, leaderboardTime)){
                    Call.infoToast(UI.getLeaderboard(hexData), 15f);
                }

                if(interval.get(timerUpdate, updateTime)){
                    hexData.updateControl();
                }

                if(interval.get(timerWinCheck, 60 * 2)){
                    Seq<Player> players = hexData.getLeaderboard();
                    if(!players.isEmpty() && hexData.getControlled(players.first()).size >= winCondition && players.size > 1 && hexData.getControlled(players.get(1)).size <= 1){
                        endGame();
                    }
                }

                counter += Time.delta;

                //kick everyone and restart w/ the script
                if(counter > roundTime && !restarting){
                    endGame();
                }
            }else{
                counter = 0;
            }
        });

        Events.on(BlockDestroyEvent.class, event -> {
            //reset last spawn times so this hex becomes vacant for a while.
            if(event.tile.block() instanceof CoreBlock){
                Hex hex = hexData.getHex(event.tile.pos());

                if(hex != null){
                    //update state
                    hex.spawnTime.reset();
                    hex.updateController();
                }
            }
        });

        Events.on(PlayerLeave.class, event -> {
//            if(active() && event.player.team() != Team.derelict){
//                killTiles(event.player.team());
//            }
        });

        Events.on(PlayerJoin.class, event -> {
            if(!active() || event.player.team() == Team.derelict) return;

            // If team is still alive
            if (event.player.team().active()) {

                // Let the player stay on the team
                return;
            }

            // Find a viable hex furthest from the center

            Seq<Hex> hexes = hexData.hexes().copy();
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

                Call.infoMessage(event.player.con, "There are currently no viable hexes available.\nAssigning into spectator mode.");
                event.player.unit().kill();
                event.player.team(Team.derelict);
                return;
            }

            loadout(event.player, bestHex.x, bestHex.y);
            Core.app.post(() -> hexData.data(event.player).chosen = false);
            bestHex.findController();

            hexData.data(event.player).lastMessage.reset();
        });

        Events.on(HexData.ProgressIncreaseEvent.class, event -> updateText(event.player));

        Events.on(HexData.HexCaptureEvent.class, event -> updateText(event.player));

        Events.on(HexData.HexMoveEvent.class, event -> updateText(event.player));

        TeamAssigner prev = netServer.assigner;
        netServer.assigner = (player, players) -> {
            Seq<Player> arr = Seq.with(players);

            if(active()){
                //pick first inactive team
                for(Team team : Team.all){
                    if(team.id > 5 && !team.active() && !arr.contains(p -> p.team() == team) && !hexData.data(team).dying && !hexData.data(team).chosen){
                        hexData.data(team).chosen = true;
                        return team;
                    }
                }
                Call.infoMessage(player.con, "There are currently no empty hex spaces available.\nAssigning into spectator mode.");
                return Team.derelict;
            }else{
                return prev.assign(player, players);
            }
        };
    }

    public void config() {
        info("config");

        serverName.set("\uF869 [red]\uE861 [purple]Assimilation [grey]> [red]Hex [white]PVP [grey]> [white]Serpulo");
        desc.set("Conquer every [red]hex [grey]on the map");
        motd.set("Welcome to [purple]Assimilation [red]Hex [white]PVP!\n[white]your goal is to convert all cores under your control. Good Luck.");
        allowCustomClients.set(true);
        antiSpam.set(true);

        // allows 1 interaction every 1 second

        interactRateWindow.set(1);
        interactRateLimit.set(1);

        messageRateLimit.set(1);
        enableVotekick.set(false);
        // startCommands.set("hexed");

    }

    void updateText(Player player){
        HexData.HexTeam team = hexData.data(player);

        StringBuilder message = new StringBuilder("[white]Hex #" + team.location.id + "\n");

        if(!team.lastMessage.get()) return;

        if(team.location.controller == null){
            if(team.progressPercent > 0){
                message.append("[lightgray]Capture progress: [accent]").append((int)(team.progressPercent)).append("%");
            }else{
                message.append("[lightgray][[Empty]");
            }
        }else if(team.location.controller == player.team()){
            message.append("[yellow][[Captured]");
        }else if(team.location != null && team.location.controller != null && hexData.getPlayer(team.location.controller) != null){
            message.append("[#").append(team.location.controller.color).append("]Captured by ").append(hexData.getPlayer(team.location.controller).name);
        }else{
            message.append("<Unknown>");
        }

        Call.setHudText(player.con, message.toString());
    }

    private void setRules() {

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
    public void registerServerCommands(CommandHandler handler){
        handler.register("hexed", "Begin hosting with the Hexed gamemode.", args -> {
            if(!state.is(State.menu)){
                Log.err("Stop the server first.");
                return;
            }

            hexData = new HexData();

            logic.reset();
            Log.info("Generating map...");
            Map generator = new Map();
            world.loadGenerator(Hex.size, Hex.size, generator);
            hexData.initHexes(generator.getHex());
            info("Map generated.");
//            state.rules = rules.copy();
            this.setRules();
            logic.play();
            netServer.openServer();
        });

        handler.register("countdown", "Get the hexed restart countdown.", args -> {
            Log.info("Time until round ends: &lc@ minutes", (int)(roundTime - counter) / 60 / 60);
        });

        handler.register("end", "End the game.", args -> endGame());

        handler.register("r", "Restart the server.", args -> System.exit(2));
    }

    @Override
    public void registerClientCommands(CommandHandler handler){

        if(registered) return;
        registered = true;

        PluginVars.clientCommands = handler;
        ClientCommands.init(hexData);
    }

    void endGame(){
        if(restarting) return;

        restarting = true;
        Seq<Player> players = hexData.getLeaderboard();
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < players.size && i < 3; i++){
            if(hexData.getControlled(players.get(i)).size > 1){
                builder.append("[yellow]").append(i + 1).append(".[accent] ").append(players.get(i).name)
                        .append("[lightgray] (x").append(hexData.getControlled(players.get(i)).size).append(")[]\n");
            }
        }

        if(!players.isEmpty()){
            boolean dominated = hexData.getControlled(players.first()).size == hexData.hexes().size;

            for(Player player : Groups.player){
                Call.infoMessage(player.con, "[accent]--ROUND OVER--\n\n[lightgray]"
                        + (player == players.first() ? "[accent]You[] were" : "[yellow]" + players.first().name + "[lightgray] was") +
                        " victorious, with [accent]" + hexData.getControlled(players.first()).size + "[lightgray] hexes conquered." + (dominated ? "" : "\n\nFinal scores:\n" + builder));
            }
        }

        Log.info("&ly--SERVER RESTARTING--");
        Time.runTask(60f * 10f, () -> {
            netServer.kickAll(KickReason.serverRestarting);
            Time.runTask(5f, () -> System.exit(2));
        });
    }

    void loadout(Player player, int x, int y){
        Stile coreTile = coreNucleus.tiles.find(s -> s.block instanceof CoreBlock);
        if(coreTile == null) throw new IllegalArgumentException("Schematic has no core tile. Exiting.");
        int ox = x - coreTile.x, oy = y - coreTile.y;
        coreNucleus.tiles.each(st -> {
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

    public boolean active(){
        return state.rules.tags.getBool("hexed") && !state.is(State.menu);
    }
    public static void info(String text, Object... values) {

        Log.infoTag("info", format(text, values));
    }
    public static void error(String text, Object... values) {

        Log.errTag("error", format(text, values));
    }
}