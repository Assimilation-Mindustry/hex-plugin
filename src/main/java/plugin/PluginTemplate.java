package plugin;

import arc.ApplicationListener;
import arc.Core;
import arc.util.CommandHandler;
import mindustry.mod.Plugin;

public class PluginTemplate extends Plugin implements ApplicationListener {

    public PluginTemplate(){
        Core.app.addListener(this);
        //run code that needs to be run as soon as app starts and mod is loaded
    }

    @Override
    public void init(){
        //run code that is run when everything has been initialised
    }

    @Override
    public void update(){
        //run code that needs to be run every tick
    }

    @Override
    public void registerClientCommands(CommandHandler handler){
        //register client commands to NetServer.clientCommands
    }

    @Override
    public void registerServerCommands(CommandHandler handler){
        //register server commands
    }

    @Override
    public void dispose(){
        //run code that shuts the server down
    }
}