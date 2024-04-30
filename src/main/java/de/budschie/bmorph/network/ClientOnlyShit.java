package de.budschie.bmorph.network;

import java.util.UUID;

import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityAttacher;
import de.budschie.bmorph.capabilities.pufferfish.PufferfishCapabilityAttacher;
import de.budschie.bmorph.render_handler.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

public class ClientOnlyShit
{
	public static void handlePufferfishPacketClient(PufferfishPuff.PufferfishPuffPacket packet)
	{
		if(Minecraft.getInstance().world != null)
		{
			PlayerEntity player = Minecraft.getInstance().world.getPlayerByUuid(packet.getPlayer());
	
			if (player != null)
			{
				player.getCapability(PufferfishCapabilityAttacher.PUFFER_CAP).ifPresent(cap ->
				{
					cap.setOriginalPuffTime(packet.getOriginalDuration());
					cap.setPuffTime(packet.getDuration());
				});
			}
		}
	}
	
	public static void handleGuardianPacketClient(GuardianBeamAttack.GuardianBeamAttackPacket packet)
	{
		if(Minecraft.getInstance().world != null)
		{
			PlayerEntity player = Minecraft.getInstance().world.getPlayerByUuid(packet.getPlayer());
	
			if (player != null)
			{
				player.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).ifPresent(cap ->
				{
					cap.setAttackedEntity(packet.getEntity());
					cap.setAttackProgression(packet.getAttackProgression());
					cap.setMaxAttackProgression(packet.getMaxAttackProgression());
				});
			}
		}
	}
	
	public static void disposePlayerMorphData(UUID playerToDispose)
	{
		RenderHandler.disposePlayerMorphData(playerToDispose);
	}
}
