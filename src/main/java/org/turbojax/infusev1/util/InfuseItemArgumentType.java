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
import org.turbojax.infusev1.items.CustomItem;
import org.turbojax.infusev1.items.Enhancer;
import org.turbojax.infusev1.items.InfuseEffect;
import org.turbojax.infusev1.items.Reviver;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class InfuseItemArgumentType implements CustomArgumentType<CustomItem,String> {
    private static final List<String> EXAMPLES = List.of("enhancer", "infuse_effect", "reviver");
    private static final DynamicCommandExceptionType PARSE_ERROR = new DynamicCommandExceptionType(key -> new LiteralMessage("Invalid item key \"" + key + "\""));

    @Override
    public CustomItem parse(StringReader reader) throws CommandSyntaxException {
        String key = reader.readUnquotedString();

        if (key.equalsIgnoreCase("enhancer")) return new Enhancer();
        if (key.equalsIgnoreCase("infuse_effect")) return new InfuseEffect();
        if (key.equalsIgnoreCase("reviver")) return new Reviver();

        throw PARSE_ERROR.create(key);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        EXAMPLES.stream()
            .filter(s -> s.contains(builder.getRemaining().toLowerCase()))
            .sorted()
            .forEach(builder::suggest);

        return builder.buildFuture();
    }

    public List<String> getExamples() {
        return List.copyOf(EXAMPLES);
    }    
}
