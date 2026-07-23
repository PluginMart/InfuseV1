package org.turbojax.infusev1.util;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.turbojax.infusev1.DataManager;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BannedPlayerArgumentType implements CustomArgumentType<OfflinePlayer,String> {
    private static final List<String> EXAMPLES = List.of("TurboJax07", "CatAdmirer");
    private static final DynamicCommandExceptionType PARSE_ERROR = new DynamicCommandExceptionType(key -> new LiteralMessage("Player \"" + key + "\" is not banned"));

    @Override
    public OfflinePlayer parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);

        if (DataManager.getBanned().contains(player)) return player;

        throw PARSE_ERROR.create(name);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        DataManager.getBanned()
            .stream()
            .map(OfflinePlayer::getName)
            .forEach(builder::suggest);

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
