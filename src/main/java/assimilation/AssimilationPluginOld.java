/*package assimilation;

// internal

import arc.ApplicationListener;
import arc.Core;
import arc.util.CommandHandler;
import arc.util.Log;
import assimilation.commands.*;
import mindustry.mod.Plugin;

import static arc.util.Strings.format;
import static assimilation.PluginVars.clientCommands;
import static assimilation.PluginVars.serverCommands;
import static mindustry.Vars.logic;
import static mindustry.net.Administration.Config.*;

@SuppressWarnings("unused")
public class AssimilationPluginOld extends Plugin implements ApplicationListener {

    public static final int messageTime = 1;

    //run code that needs to be run as soon as app starts and mod is loaded
    public void PluginTemplate(){
        Core.app.addListener(this);

    }

    @Override
    //run code that is run when everything has been initialised
    public void init(){
        info("start init");

        AssimilationEvents.init();
        this.config();

        info("end init");
    }

    public void config() {
        info("config");

        serverName.set("[red]TESTINGDUSTRY");
        desc.set("TESTING TESTING");

        allowCustomClients.set(true);
        interactRateLimit.set(100);
        messageRateLimit.set(50);
        antiSpam.set(true);

    }

    @Override
    //run code that needs to be run every tick
    public void update(){

        info("start tick");
        info("end tick");
    }

    @Override
    //register client commands to NetServer.clientCommands
    public void registerClientCommands(CommandHandler handler){

        clientCommands = handler;
        ClientCommands.init();
    }

    @Override
    //register server commands
    public void registerServerCommands(CommandHandler handler){
        info("start server commands");

        serverCommands = handler;
        ServerCommands.init();

        //

        logic.reset();
        logic.play();

        info("end server commands");
    }

    @Override
    //run code that shuts the server down
    public void dispose(){

    }

    public static void info(String text, Object... values) {

        Log.infoTag("info", format(text, values));
    }
    public static void error(String text, Object... values) {

        Log.errTag("error", format(text, values));
    }
}*/