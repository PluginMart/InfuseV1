package org.turbojax.infusev1.paper.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.PaperCommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jspecify.annotations.Nullable;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.MainConfig;
import org.turbojax.infusev1.commands.InfuseCommand;

import java.util.List;
import java.util.stream.Stream;

public class PaperInfuseCommand extends InfuseCommand {
    private final Infuse infuse = Infuse.getInstance();

    public static LiteralCommandNode<CommandSourceStack> buildPaper(String alias) {
        Infuse infuse = Infuse.getInstance();
        MainConfig config = infuse.config();
        PaperInfuseCommand cmd = new PaperInfuseCommand();

        return Commands.literal(alias)
                .then(Commands.literal("help")
                        .executes(cmd::help)
                )
                .then(Commands.literal("reload")
                        .executes(cmd::reload)
                )
                .then(Commands.literal("revive")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests((c, builder) -> {
                                    infuse.dataManager().getBanned()
                                            .stream()
                                            .map(NameAndId::name)
                                            .forEach(builder::suggest);

                                    return builder.buildFuture();
                                })
                                .executes(c -> cmd.revive(c, c.getArgument("player", String.class)))
                        )
                )
                .then(Commands.literal("getscore")
                        .executes(c -> cmd.getScore(c, null))
                        .then(Commands.argument("player", ArgumentTypes.players())
                                .executes(c -> cmd.getScore(c, c.getArgument("player", PlayerSelectorArgumentResolver.class)))
                        )
                )
                .then(Commands.literal("setscore")
                        .then(Commands.argument("player", EntityArgument.players())
                                .then(Commands.argument("score", IntegerArgumentType.integer(config.minScore(), config.maxScore()))
                                        .executes(c -> cmd.setScore(c, c.getArgument("player", PlayerSelectorArgumentResolver.class), c.getArgument("score", Integer.class)))
                                )
                        )
                )
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.players())
                                .then(Commands.argument("item", StringArgumentType.word())
                                        .suggests((c, builder) -> {
                                            Stream.of("enhancer", "infuse_effect", "reviver")
                                                    .filter(s -> s.contains(builder.getRemaining().toLowerCase()))
                                                    .sorted()
                                                    .forEach(builder::suggest);

                                            return builder.buildFuture();
                                        })
                                        .executes(c -> cmd.give(c, c.getArgument("player", PlayerSelectorArgumentResolver.class), c.getArgument("item", String.class), 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                .executes(c -> cmd.give(c, c.getArgument("player", PlayerSelectorArgumentResolver.class), c.getArgument("item", String.class), c.getArgument("count", Integer.class)))
                                        )
                                )
                        )
                )
                .build();
    }

    public int help(CommandContext<CommandSourceStack> ctx) {
        PaperCommandSourceStack source = (PaperCommandSourceStack) ctx.getSource();
        return super.help(source.getHandle());
    }

    public int reload(CommandContext<CommandSourceStack> ctx) {
        PaperCommandSourceStack source = (PaperCommandSourceStack) ctx.getSource();
        return super.reload(source.getHandle());
    }

    public int revive(CommandContext<CommandSourceStack> ctx, String name) {
        PaperCommandSourceStack source = (PaperCommandSourceStack) ctx.getSource();
        return super.revive(source.getHandle(), name);
    }

    public int getScore(CommandContext<CommandSourceStack> ctx, @Nullable PlayerSelectorArgumentResolver resolver) {
        PaperCommandSourceStack source = (PaperCommandSourceStack) ctx.getSource();

        if (resolver == null) return super.getScore(source.getHandle(), null);

        try {
            List<ServerPlayer> targets = resolver.resolve(ctx.getSource())
                    .stream()
                    .map(p -> (CraftPlayer) p)
                    .map(CraftPlayer::getHandle)
                    .toList();

            return super.getScore(source.getHandle(), targets);
        } catch (CommandSyntaxException e) {
            //noinspection ConstantConditions
            source.getSender().sendMessage(e.componentMessage());
        }

        return 1;
    }

    public int setScore(CommandContext<CommandSourceStack> ctx, PlayerSelectorArgumentResolver resolver, int score) {
        PaperCommandSourceStack source = (PaperCommandSourceStack) ctx.getSource();

        try {
            List<ServerPlayer> targets = resolver.resolve(ctx.getSource())
                    .stream()
                    .map(p -> (CraftPlayer) p)
                    .map(CraftPlayer::getHandle)
                    .toList();
            return super.setScore(source.getHandle(), targets, score);
        } catch (CommandSyntaxException e) {
            //noinspection ConstantConditions
            source.getSender().sendMessage(e.componentMessage());
            return 1;
        }
    }

    public int give(CommandContext<CommandSourceStack> ctx, PlayerSelectorArgumentResolver resolver, String itemKey, int count) {
        PaperCommandSourceStack source = (PaperCommandSourceStack) ctx.getSource();

        try {
            List<ServerPlayer> targets = resolver.resolve(ctx.getSource())
                    .stream()
                    .map(p -> (CraftPlayer) p)
                    .map(CraftPlayer::getHandle)
                    .toList();

            return super.give(source.getHandle(), targets, itemKey, count);
        } catch (CommandSyntaxException e) {
            //noinspection ConstantConditions
            source.getSender().sendMessage(e.componentMessage());
        }

        return 1;
    }
}
