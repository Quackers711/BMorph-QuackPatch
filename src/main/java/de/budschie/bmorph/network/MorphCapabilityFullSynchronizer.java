package de.budschie.bmorph.network;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.FavouriteList;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphList;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.network.MorphCapabilityFullSynchronizer.MorphPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent;

public class MorphCapabilityFullSynchronizer implements ISimpleImplPacket<MorphPacket>
{
	@Override
	public void encode(MorphPacket packet, PacketBuffer buffer)
	{		
		buffer.writeUniqueId(packet.player);
		packet.morphList.serializePacket(buffer);
		packet.favouriteList.serializePacket(buffer);
		buffer.writeBoolean(packet.entityData.isPresent());
		buffer.writeBoolean(packet.entityIndex.isPresent());
		packet.getEntityData().ifPresent(data -> buffer.writeCompoundTag(data.serialize()));
		packet.getEntityIndex().ifPresent(data -> buffer.writeInt(data));
		
		buffer.writeInt(packet.getAbilities().size());
		
		for(String str : packet.getAbilities())
			buffer.writeString(str);
	}
	
	@Override
	public MorphPacket decode(PacketBuffer buffer)
	{
		UUID playerUUID = buffer.readUniqueId();
		
		// Hmmm yeah the floor is made out of floor
		MorphList morphList = new MorphList();
		morphList.deserializePacket(buffer);
		
		FavouriteList favouriteList = new FavouriteList(morphList);
		favouriteList.deserializePacket(buffer);
		
		Optional<MorphItem> toMorph = Optional.empty();
		Optional<Integer> entityIndex = Optional.empty();
		
		boolean hasMorph = buffer.readBoolean(), hasIndex = buffer.readBoolean();
		
		if(hasMorph)
			toMorph = Optional.of(MorphHandler.deserializeMorphItem(buffer.readCompoundTag()));
		
		if(hasIndex)
			entityIndex = Optional.of(buffer.readInt());
		
		int amountOfAbilities = buffer.readInt();
		
		ArrayList<String> abilities = new ArrayList<>(amountOfAbilities);
		
		for(int i = 0; i < amountOfAbilities; i++)
			abilities.add(buffer.readString());
		
		return new MorphPacket(toMorph, entityIndex, morphList, favouriteList, abilities, playerUUID);
	}
	
	@Override
	public void handle(MorphPacket packet, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			if(Minecraft.getInstance().world != null)
			{
				PlayerEntity player = Minecraft.getInstance().world.getPlayerByUuid(packet.getPlayer());
				
				if(player != null)
				{
					LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
					if(cap.isPresent())
					{
						IMorphCapability resolved = cap.resolve().get();
						resolved.setMorphList(packet.getMorphList());
						resolved.setFavouriteList(packet.getFavouriteList());
					}
					MorphUtil.morphToClient(packet.getEntityData(), packet.getEntityIndex(), packet.getAbilities(), player);	
				}
			}
		});
	}
	
	public static class MorphPacket
	{
		private Optional<MorphItem> entityData;
		private Optional<Integer> entityIndex;
		private MorphList morphList;
		private FavouriteList favouriteList;
		private ArrayList<String> abilities;
		private UUID player;
		
		public MorphPacket(Optional<MorphItem> entityData, Optional<Integer> entityIndex, MorphList morphList, FavouriteList favouriteList, ArrayList<String> abilities, UUID player)
		{
			this.entityData = entityData;
			this.player = player;
			this.morphList = morphList;
			this.favouriteList = favouriteList;
			this.entityIndex = entityIndex;
			this.abilities = abilities;
		}
		
		public ArrayList<String> getAbilities()
		{
			return abilities;
		}
		
		public Optional<MorphItem> getEntityData()
		{
			return entityData;
		}
		
		public UUID getPlayer()
		{
			return player;
		}
		
		public MorphList getMorphList()
		{
			return morphList;
		}
		
		public FavouriteList getFavouriteList()
		{
			return favouriteList;
		}
		
		public Optional<Integer> getEntityIndex()
		{
			return entityIndex;
		}
	}
}
