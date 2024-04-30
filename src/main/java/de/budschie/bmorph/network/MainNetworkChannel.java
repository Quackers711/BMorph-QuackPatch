package de.budschie.bmorph.network;

import de.budschie.bmorph.main.References;
import de.budschie.bmorph.network.ConfiguredAbilitySynchronizer.ConfiguredAbilityPacket;
import de.budschie.bmorph.network.MorphCapabilityFullSynchronizer.MorphPacket;
import de.budschie.bmorph.network.MorphRemovedSynchronizer.MorphRemovedPacket;
import de.budschie.bmorph.network.MorphRequestAbilityUsage.MorphRequestAbilityUsagePacket;
import de.budschie.bmorph.network.MorphRequestFavouriteChange.MorphRequestFavouriteChangePacket;
import de.budschie.bmorph.network.MorphRequestMorphIndexChange.RequestMorphIndexChangePacket;
import de.budschie.bmorph.network.SquidBoost.SquidBoostPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class MainNetworkChannel
{
	public static final String PROTOCOL_VERSION = "3";
	
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(References.MODID, "main"), 
			() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	
	private static int id = 0;
		
	public static void registerMainNetworkChannels()
	{
//		INSTANCE.registerMessage(id++, MorphCapabilityFullSynchronizer.MorphPacket.class, MorphCapabilityFullSynchronizer::encode,
//				MorphCapabilityFullSynchronizer::decode, MorphCapabilityFullSynchronizer::handle);
		
		registerSimpleImplPacket(MorphPacket.class, new MorphCapabilityFullSynchronizer());
		registerSimpleImplPacket(MorphAddedSynchronizer.MorphAddedPacket.class, new MorphAddedSynchronizer());
		registerSimpleImplPacket(MorphRemovedPacket.class, new MorphRemovedSynchronizer());
		registerSimpleImplPacket(MorphChangedSynchronizer.MorphChangedPacket.class, new MorphChangedSynchronizer());
		registerSimpleImplPacket(RequestMorphIndexChangePacket.class, new MorphRequestMorphIndexChange());
		registerSimpleImplPacket(MorphRequestAbilityUsagePacket.class, new MorphRequestAbilityUsage());
		registerSimpleImplPacket(MorphRequestFavouriteChangePacket.class, new MorphRequestFavouriteChange());
		registerSimpleImplPacket(SquidBoostPacket.class, new SquidBoost());
		registerSimpleImplPacket(ConfiguredAbilityPacket.class, new ConfiguredAbilitySynchronizer());
		registerSimpleImplPacket(PufferfishPuff.PufferfishPuffPacket.class, new PufferfishPuff());
		registerSimpleImplPacket(GuardianBeamAttack.GuardianBeamAttackPacket.class, new GuardianBeamAttack());
		registerSimpleImplPacket(DisposePlayerMorphData.DisposePlayerMorphDataPacket.class, new DisposePlayerMorphData());
	}
	
	public static <T> void registerSimpleImplPacket(Class<T> packetClass, ISimpleImplPacket<T> packet)
	{
		INSTANCE.registerMessage(id++, packetClass, packet::encode, packet::decode, packet::handle);
	}
}
