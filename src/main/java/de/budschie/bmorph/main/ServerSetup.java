package de.budschie.bmorph.main;

import de.budschie.bmorph.capabilities.blacklist.ConfigManager;
import de.budschie.bmorph.commands.BlacklistCommand;
import de.budschie.bmorph.commands.MorphCommand;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

@EventBusSubscriber(bus = Bus.FORGE)
public class ServerSetup
{
	public static MinecraftServer server;
		
	@SubscribeEvent
	public static void onServerStarting(final FMLServerStartingEvent event)
	{		
		server = event.getServer();
		
		ConfigManager.INSTANCE.loadAll();
	}
	
	@SubscribeEvent
	public static void onServerReloaded(RegisterCommandsEvent event)
	{
		MorphCommand.registerCommands(event.getDispatcher());
		BlacklistCommand.registerCommand(event.getDispatcher());
		
		System.out.println("Registered commands.");
	}
	
	@SubscribeEvent
	public static void onServerStopping(final FMLServerStoppingEvent event)
	{
		ConfigManager.INSTANCE.serverShutdown();
		BMorphMod.DYNAMIC_ABILITY_REGISTRY.unregisterAll();
		server = null;
	}
}
