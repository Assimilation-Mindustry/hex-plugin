package assimilation;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import assimilation.AssimilationPlugin;
import assimilation.hex.Hex;
import mindustry.content.Items;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.type.ItemStack;

import static mindustry.Vars.state;

public class AssimilationEvents {
    public static void init() {

        Events.on(EventType.PlayerLeave.class, event -> {
            AssimilationPlugin.info("THIS PLAYER HAS LEFT: @", event.player);
        });

        rules();
    }

    public static void rules() {

        Events.on(EventType.PlayEvent.class, event -> {
            AssimilationPlugin.info("start set rules");

            state.rules.pvp = true;
            state.rules.tags.put("hexed", "true");
            //state.rules.canGameOver = false;
            state.rules.polygonCoreProtection = true;
            state.rules.loadout = ItemStack.list(Items.copper, 1000, Items.lead, 1000, Items.graphite, 150, Items.metaglass, 150, Items.silicon, 150, Items.plastanium, 0);
            state.rules.buildSpeedMultiplier = 1f / 2f;
            state.rules.blockHealthMultiplier = .5f;
            state.rules.unitBuildSpeedMultiplier = 1f;
            state.rules.unitDamageMultiplier = .5f;
            state.rules.blockDamageMultiplier = .5f;
            state.rules.coreIncinerates = true;
            state.rules.damageExplosions = false;
            state.rules.disableOutsideArea = false;
            state.rules.dragMultiplier = .0f;
            state.rules.enemyCoreBuildRadius = .100f;
            state.rules.ghostBlocks = true;
            state.rules.hideBannedBlocks = true;
            state.rules.logicUnitBuild = false;
            state.rules.mission = "HELLO MISSION";
            state.rules.modeName = "THE CURRENT MODE";
            // state.rules.onlyDepositCore = true;
            state.rules.reactorExplosions = false;
            state.rules.unitCap = 24;
            state.rules.unitCrashDamageMultiplier = .0f;
            state.rules.unitCapVariable = true;
            // state.rules.bannedBlocks = new ObjectSet<>("");

            AssimilationPlugin.info("end set rules");
        });

        Events.on(EventType.PlayerJoin.class, event -> {
            if(event.player.team() == Team.derelict) return;

            Seq<Hex> copy = data.hexes().copy();
            copy.shuffle();
            Hex hex = copy.find(h -> h.controller == null && h.spawnTime.get());

            if(hex != null){
                loadout(event.player, hex.x, hex.y);
                Core.app.post(() -> data.data(event.player).chosen = false);
                hex.findController();
            }else{
                Call.infoMessage(event.player.con, "There are currently no empty hex spaces available.\nAssigning into spectator mode.");
                event.player.unit().kill();
                event.player.team(Team.derelict);
            }

            data.data(event.player).lastMessage.reset();
        });
    }
}