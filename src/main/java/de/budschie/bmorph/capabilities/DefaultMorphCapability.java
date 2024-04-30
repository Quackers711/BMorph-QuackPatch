package de.budschie.bmorph.capabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import de.budschie.bmorph.morph.FavouriteList;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphList;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.MorphAddedSynchronizer;
import de.budschie.bmorph.network.MorphCapabilityFullSynchronizer;
import de.budschie.bmorph.network.MorphChangedSynchronizer;
import de.budschie.bmorph.network.MorphRemovedSynchronizer.MorphRemovedPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.network.PacketDistributor;

public class DefaultMorphCapability implements IMorphCapability
{
	boolean mobAttack = false;
	
	int aggroTimestamp = 0;
	int aggroDuration = 0;
	
	Optional<MorphItem> morph = Optional.empty();
	Optional<Integer> currentMorphIndex = Optional.empty();
	
	MorphList morphList = new MorphList();
	FavouriteList favouriteList = new FavouriteList(morphList);
	
	List<Ability> currentAbilities = new ArrayList<>();

	// CUSTOM QUACK711
	float originMaxHP = 20.0F;
	boolean wasPlayer;

	@Override
	public void syncWithClients(PlayerEntity player)
	{
		if(player.world.isRemote)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new MorphCapabilityFullSynchronizer.MorphPacket(morph, currentMorphIndex, morphList, favouriteList, serializeAbilities(), player.getUniqueID()));
		}
	}
	
	@Override
	public void syncWithClient(PlayerEntity player, ServerPlayerEntity syncTo)
	{
		if(player.world.isRemote)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> syncTo), new MorphCapabilityFullSynchronizer.MorphPacket(morph, currentMorphIndex, morphList, favouriteList, serializeAbilities(), player.getUniqueID()));
		}
	}
	
	@Override
	public void syncWithConnection(PlayerEntity player, NetworkManager connection)
	{
		if(player.world.isRemote)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.NMLIST.with(() -> Lists.newArrayList(connection)), new MorphCapabilityFullSynchronizer.MorphPacket(morph, currentMorphIndex, morphList, favouriteList, serializeAbilities(), player.getUniqueID()));
		}
	}
	
	@Override
	public void syncMorphChange(PlayerEntity player)
	{
		if(player.world.isRemote)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new MorphChangedSynchronizer.MorphChangedPacket(player.getUniqueID(), currentMorphIndex, morph, serializeAbilities()));
	}

	@Override
	public void syncMorphAcquisition(PlayerEntity player, MorphItem item)
	{
		if(player.world.isRemote)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new MorphAddedSynchronizer.MorphAddedPacket(player.getUniqueID(), item));
	}

	@Override
	public void syncMorphRemoval(PlayerEntity player, int index)
	{
		if(player.world.isRemote)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new MorphRemovedPacket(player.getUniqueID(), index));
	}
	
	private ArrayList<String> serializeAbilities()
	{
		if(getCurrentAbilities() == null || getCurrentAbilities().size() == 0)
			return new ArrayList<>();
		else
		{
			ArrayList<String> toString = new ArrayList<>();
			
			for (Ability ability : getCurrentAbilities())
			{
				toString.add(ability.getResourceLocation().toString());
			}
			
			return toString;
		}
	}
	
	@Override
	public void addToMorphList(MorphItem morphItem)
	{
		morphList.addToMorphList(morphItem);
	}
	@Override
	public void removeFromMorphList(int index)
	{
		morphList.removeFromMorphList(index);
	}
	
	@Override
	public void setMorphList(MorphList list)
	{
		this.morphList = list;
		
		// Setting morph list not fully handled, but this is an edge case that never happens lulw
		this.favouriteList.setMorphList(morphList);
	}

	@Override
	/** There shall only be read access to this list, as else, changed content won't be sent to the clients. **/
	public MorphList getMorphList()
	{
		return morphList;
	}

	// CUSTOM QUACK711
	@Override
	public void setOriginMaxHP(float hp) {
		this.originMaxHP = hp;
	}

	// CUSTOM QUACK711
	@Override
	public float getOriginMaxHP() {
		return this.originMaxHP;
	}

	@Override
	public void applyHealthOnPlayer(PlayerEntity player)
	{
		// CUSTOM QUACK711 PATCH - Bug Fix - Possible to die when switching morphs as well as keep max HP on switch

		float playerHealthPercentage = player.getHealth() / player.getMaxHealth();

		// If human/player
		if(!getCurrentMorph().isPresent())
		{
			float playerMaxHP = getOriginMaxHP();
			player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(playerMaxHP);
			player.setHealth(playerMaxHP * playerHealthPercentage);

			this.wasPlayer = true;
		}
		// If going from human/player to mob
		else
		{
			// If switching from a player
			if (this.wasPlayer) {
				setOriginMaxHP(player.getMaxHealth());
			}

			this.wasPlayer = false;

			Entity morphEntity = getCurrentMorph().get().createEntity(player.world);

			if(morphEntity instanceof LivingEntity)
			{
				float maxHealthOfEntity = ((LivingEntity)morphEntity).getMaxHealth();
				player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealthOfEntity);
				player.setHealth(maxHealthOfEntity * playerHealthPercentage);
			}
			else
			{
				float playerMaxHP = getOriginMaxHP();
				player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(playerMaxHP);
				player.setHealth(playerMaxHP * playerHealthPercentage);
			}
		}
		
//		
	}
	
	@Override
	public Optional<Integer> getCurrentMorphIndex()
	{
		return currentMorphIndex;
	}

	@Override
	public Optional<MorphItem> getCurrentMorphItem()
	{
		return morph;
	}

	@Override
	public Optional<MorphItem> getCurrentMorph()
	{
		if(currentMorphIndex.isPresent())
			return Optional.of(getMorphList().getMorphArrayList().get(currentMorphIndex.get()));
		else if(morph.isPresent())
			return morph;
		else
			return Optional.empty();
	}

	@Override
	public void setMorph(int index)
	{
		this.morph = Optional.empty();
		this.currentMorphIndex = Optional.of(index);
//		dirty = true;
	}

	@Override
	public void setMorph(MorphItem morph)
	{
		this.morph = Optional.of(morph);
		this.currentMorphIndex = Optional.empty();
//		dirty = true;
	}

	@Override
	public void demorph()
	{
		this.morph = Optional.empty();
		this.currentMorphIndex = Optional.empty();
//		dirty = true;
	}

	@Override
	public List<Ability> getCurrentAbilities()
	{
		return currentAbilities;
	}

	@Override
	public void setCurrentAbilities(List<Ability> abilities)
	{
		this.currentAbilities = abilities;
	}

	@Override
	public void applyAbilities(PlayerEntity player)
	{
		if(getCurrentAbilities() != null && getCurrentMorph().isPresent())
			getCurrentAbilities().forEach(ability -> ability.enableAbility(player, getCurrentMorph().get()));
	}

	@Override
	public void deapplyAbilities(PlayerEntity player)
	{
		if(getCurrentAbilities() != null)
			getCurrentAbilities().forEach(ability -> ability.disableAbility(player, getCurrentMorph().get()));
	}

	@Override
	public void useAbility(PlayerEntity player)
	{
		if(getCurrentAbilities() != null)
			getCurrentAbilities().forEach(ability -> ability.onUsedAbility(player, getCurrentMorph().get()));
	}

	@Override
	public int getLastAggroTimestamp()
	{
		return aggroTimestamp;
	}

	@Override
	public void setLastAggroTimestamp(int timestamp)
	{
		this.aggroTimestamp = timestamp;
	}

	@Override
	public int getLastAggroDuration()
	{
		return aggroDuration;
	}

	@Override
	public void setLastAggroDuration(int aggroDuration)
	{
		this.aggroDuration = aggroDuration;
	}
	
	@Override
	public FavouriteList getFavouriteList()
	{
		return favouriteList;
	}

	@Override
	public void setFavouriteList(FavouriteList favouriteList)
	{
		this.favouriteList = favouriteList;
	}

	@Override
	public boolean shouldMobsAttack()
	{
		return mobAttack;
	}

	@Override
	public void setMobAttack(boolean value)
	{
		this.mobAttack = value;
	}
}
