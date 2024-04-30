package de.budschie.bmorph.commands;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import de.budschie.bmorph.capabilities.blacklist.BlacklistData;
import de.budschie.bmorph.capabilities.blacklist.ConfigManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.ResourceLocation;

public class RemovableSuggestionProvider implements SuggestionProvider<CommandSource>
{
	public static final RemovableSuggestionProvider INSTANCE = new RemovableSuggestionProvider();
	
	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context,
			SuggestionsBuilder builder) throws CommandSyntaxException
	{
		HashSet<ResourceLocation> suggestions = ConfigManager.INSTANCE.get(BlacklistData.class).getBlacklist();
		CompletableFuture<Suggestions> completableSuggestions = ISuggestionProvider.suggest(suggestions.stream().map(rs -> rs.toString()).collect(Collectors.toList()), builder);
		return completableSuggestions;
	}
}
