package org.turbojax.infusev1.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.MainConfig;
import org.turbojax.infusev1.items.CustomItem;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class InfuseCommand {
    private final Infuse infuse = Infuse.getInstance();
    
    public static LiteralCommandNode<CommandSourceStack> build(String alias) {
        MainConfig config = Infuse.getInstance().config();
        InfuseCommand cmd = new InfuseCommand();

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
                            cmd.infuse.dataManager().getBanned()
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
                .then(Commands.argument("player", EntityArgument.players())
                        .executes(c -> cmd.getScore(c, EntityArgument.getPlayers(c, "player")))
                )
            )
            .then(Commands.literal("setscore")
                .then(Commands.argument("player", EntityArgument.players())
                    .then(Commands.argument("score", IntegerArgumentType.integer(config.minScore(), config.maxScore()))
                        .executes(c -> cmd.setScore(c, EntityArgument.getPlayers(c, "player"), c.getArgument("score", Integer.class)))
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
                        .executes(c -> cmd.give(c, EntityArgument.getPlayers(c, "player"), c.getArgument("item", String.class), 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                            .executes(c -> cmd.give(c, EntityArgument.getPlayers(c, "player"), c.getArgument("item", String.class), c.getArgument("count", Integer.class)))
                        )
                    )
                )
            )
            .build();
    }

    public int help(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSystemMessage(Component.literal("/infuse").withColor(TextColor.AQUA));
        ctx.getSource().sendSystemMessage(Component.literal(" |- help").withColor(TextColor.AQUA).append(Component.literal(": Shows the help message").withColor(TextColor.WHITE)));
        ctx.getSource().sendSystemMessage(Component.literal(" |- reload").withColor(TextColor.AQUA).append(Component.literal(": Reloads the config").withColor(TextColor.WHITE)));
        ctx.getSource().sendSystemMessage(Component.literal(" |- revive ").withColor(TextColor.AQUA).append(Component.literal("<player>").withColor(TextColor.GOLD)).append(Component.literal(": Revives a dead player").withColor(TextColor.WHITE)));
        ctx.getSource().sendSystemMessage(Component.literal(" |- setscore ").withColor(TextColor.AQUA).append(Component.literal("<player> <score>").withColor(TextColor.GOLD)).append(Component.literal(": Sets a player's score.  Also rerolls their effects.").withColor(TextColor.WHITE)));
        ctx.getSource().sendSystemMessage(Component.literal(" |- getscore ").withColor(TextColor.AQUA).append(Component.literal("<player>").withColor(TextColor.GOLD)).append(Component.literal(": Gets a player's score").withColor(TextColor.WHITE)));
        ctx.getSource().sendSystemMessage(Component.literal(" \\- give ").withColor(TextColor.AQUA).append(Component.literal("<player> <item> [count]").withColor(TextColor.GOLD)).append(Component.literal(": Gives a player an infuse item.").withColor(TextColor.WHITE)));

        return 1;
    }

    public int reload(CommandContext<CommandSourceStack> ctx) {
        InfuseProvider.get().config().load();
        // TODO: Reload recipes
        ctx.getSource().sendSystemMessage(Component.literal("Reloaded the config.").withColor(TextColor.GREEN));

        return 1;
    }

    public int revive(CommandContext<CommandSourceStack> ctx, String name) {
        Optional<NameAndId> result = infuse.server().services().nameToIdCache().get(name);
        if (result.isEmpty()) {
            ctx.getSource().sendSystemMessage(Component.literal("Player \"" + name + "\" is not banned"));
            return 1;
        }

        NameAndId player = result.get();

        if (!infuse.dataManager().getBanned().contains(player)) {
            ctx.getSource().sendSystemMessage(Component.literal(player.name() + " isn't banned.").withColor(TextColor.RED));
        }

        infuse.dataManager().unban(player);
        ctx.getSource().sendSystemMessage(Component.literal("Revived " + player.name() + "!").withColor(TextColor.GREEN));

        return 1;
    }
    
    public int getScore(CommandContext<CommandSourceStack> ctx, @Nullable Collection<ServerPlayer> targets) {
        if (targets == null) {
            if (ctx.getSource().isPlayer()) {
                targets = List.of(ctx.getSource().getPlayer());
            } else {
                ctx.getSource().sendSystemMessage(Component.literal("You must specify a target.").withColor(TextColor.RED));
                return 1;
            }
        }

        targets.forEach(p -> ctx.getSource().sendSystemMessage(Component.literal(p.getPlainTextName() + "'s score is " + infuse.dataManager().getScore(p))));

        return 1;
    }
    
    public int setScore(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets, int score) {
        for (ServerPlayer player : targets) {
            infuse.dataManager().setScore(player, score);
            infuse.dataManager().resetEffects(player);

            // Banning the player if necessary
            int banScore = infuse.config().banScore();
            if (banScore < 0 && score == banScore) {
                infuse.dataManager().ban(player.nameAndId());

                player.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
            }
        }

        ctx.getSource().sendSystemMessage(Component.literal("Set %s to %d".formatted(targets.size() == 1 ? "%s's score".formatted(targets.iterator().next().getPlainTextName()) : "%d player's scores".formatted(targets.size()), score)).withColor(TextColor.GREEN));

        return 1;
    }

    public int give(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets, String itemKey, int count) {
        for (ServerPlayer player : targets) {
            CustomItem item = CustomItem.fromKey(itemKey);
            if (item == null) {
                ctx.getSource().sendSystemMessage(Component.literal("Invalid item key \"" + itemKey + "\""));
                return 1;
            }

            ItemStack stack = item.createItem();
            stack.setCount(count);

            player.addItem(stack);
        }

        ctx.getSource().sendSystemMessage(Component.literal("Gave %s %s %s%s".formatted((targets.size() == 1 ? targets.iterator().next().getPlainTextName() : targets.size() + " players"), (count == 1 ? "a" : count), itemKey, ( count == 1 ? "" : "s"))));

        return 1;
    }
}