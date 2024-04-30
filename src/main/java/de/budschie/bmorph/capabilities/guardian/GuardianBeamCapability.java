package de.budschie.bmorph.capabilities.guardian;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.entity.Entity;

public class GuardianBeamCapability implements IGuardianBeamCapability
{
	private Optional<UUID> attackedEntityServer = Optional.empty();
	private Optional<Integer> attackedEntity = Optional.empty();
	private int progression;
	private int maxAttackProgression;
	
	@Override
	public Optional<UUID> getAttackedEntityServer()
	{
		return attackedEntityServer;
	}

	@Override
	public int getAttackProgression()
	{
		return progression;
	}

	@Override
	public void setAttackedEntityServer(Optional<UUID> attackedEntity)
	{
		this.attackedEntityServer = attackedEntity;
	}

	@Override
	public void setAttackProgression(int progression)
	{
		this.progression = progression;
	}

	@Override
	public void attackServer(Optional<Entity> entity, int maxAttackDuration)
	{
		setAttackedEntityServer(entity.map(entityInstance -> entityInstance.getUniqueID()));
		attack(entity.map(entityInstance -> entityInstance.getEntityId()), maxAttackDuration);
	}

	@Override
	public Optional<Integer> getAttackedEntity()
	{
		return attackedEntity;
	}

	@Override
	public void setAttackedEntity(Optional<Integer> attackedEntity)
	{
		this.attackedEntity = attackedEntity;
	}

	@Override
	public void attack(Optional<Integer> entity, int maxAttackDuration)
	{
		setAttackedEntity(entity);
		setAttackProgression(0);
		setMaxAttackProgression(maxAttackDuration);
	}

	@Override
	public int getMaxAttackProgression()
	{
		return maxAttackProgression;
	}

	@Override
	public void setMaxAttackProgression(int maxAttackProgression)
	{
		this.maxAttackProgression = maxAttackProgression;
	}		
}
