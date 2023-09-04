package assimilation;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import assimilation.hex.Hex;
import mindustry.content.Items;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.type.ItemStack;

import static mindustry.Vars.state;

public class AssimilationEvents {
    public static void init() {

        Events.on(EventType.PlayerLeave.class, event -> {
            AssimilationPlugin.info("THIS PLAYER HAS LEFT: @", event.player.name());
        });
    }
}