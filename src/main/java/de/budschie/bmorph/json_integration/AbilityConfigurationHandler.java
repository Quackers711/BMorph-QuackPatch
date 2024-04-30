package de.budschie.bmorph.json_integration;

import java.util.Map;
import java.util.Optional;

import de.budschie.bmorph.main.BMorphMod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.AbilityRegistry;
import de.budschie.bmorph.morph.functionality.configurable.ConfigurableAbility;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class AbilityConfigurationHandler extends JsonReloadListener
{
	private static final Gson GSON = (new GsonBuilder()).create();
	private static final Logger LOGGER = LogManager.getLogger();
	
	public AbilityConfigurationHandler()
	{
		super(GSON, "configured_abilities");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn)
	{
		BMorphMod.DYNAMIC_ABILITY_REGISTRY.unregisterAll();
		
		objectIn.forEach((resourceLocation, jsonElement) ->
		{
			ResourceLocation baseAbility = new ResourceLocation(jsonElement.getAsJsonObject().get("ability").getAsString());
			ConfigurableAbility<? extends Ability> configurableAbility = AbilityRegistry.REGISTRY.get().getValue(baseAbility);
			
			if(configurableAbility == null)
				LOGGER.warn(String.format("Configurable ability \"%s\" doesn't exist. Can't parse the configured ability %s.", baseAbility, resourceLocation));
			else
			{
				JsonElement configElement = jsonElement.getAsJsonObject().get("config");
				
				if(configElement == null)
					configElement = new JsonObject();
				
				Optional<? extends Ability> ability = configurableAbility.deserialize(configElement.getAsJsonObject());
				
				if(ability.isPresent())
				{
					ability.get().setResourceLocation(resourceLocation);
					BMorphMod.DYNAMIC_ABILITY_REGISTRY.registerAbility(ability.get());
				}
			}
			
			// BMorphMod.DYNAMIC_ABILITY_REGISTRY.registerAbility(resourceLocation, );
		});
	}
}
