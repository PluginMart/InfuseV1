package org.turbojax.infusev1.paper.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.MainConfig;
import org.turbojax.infusev1.items.CustomItem;

import java.util.List;
import java.util.stream.Stream;

public class PaperInfuseCommand {
    private final Infuse infuse = Infuse.getInstance();

    public static LiteralCommandNode<CommandSourceStack> build(String alias) {
        MainConfig config = Infuse.getInstance().config();
        PaperInfuseCommand cmd = new PaperInfuseCommand();

        return Commands.literal(alias)
                .then(Commands.literal("help")
                        .executes(ctx -> cmd.help(ctx.getSource()))
                )
                .then(Commands.literal("reload")
                        .executes(ctx -> cmd.reload(ctx.getSource()))
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
                                .executes(c -> cmd.revive(c.getSource(), c.getArgument("player", String.class)))
                        )
                )
                .then(Commands.literal("getscore")
                        .executes(c -> cmd.getScore(c.getSource(), null))
                        .then(Commands.argument("player", ArgumentTypes.players())
                                .executes(c -> cmd.getScore(c.getSource(), c.getArgument("player", PlayerSelectorArgumentResolver.class)))
                        )
                )
                .then(Commands.literal("setscore")
                        .then(Commands.argument("player", ArgumentTypes.players())
                                .then(Commands.argument("score", IntegerArgumentType.integer(config.minScore(), config.maxScore()))
                                        .executes(c -> cmd.setScore(c.getSource(), c.getArgument("player", PlayerSelectorArgumentResolver.class), c.getArgument("score", Integer.class)))
                                )
                        )
                )
                .then(Commands.literal("give")
                        .then(Commands.argument("player", ArgumentTypes.players())
                                .then(Commands.argument("item", StringArgumentType.word())
                                        .suggests((c, builder) -> {
                                            Stream.of("enhancer", "infuse_effect", "reviver")
                                                    .filter(s -> s.contains(builder.getRemaining().toLowerCase()))
                                                    .sorted()
                                                    .forEach(builder::suggest);

                                            return builder.buildFuture();
                                        })
                                        .executes(c -> cmd.give(c.getSource(), c.getArgument("player", PlayerSelectorArgumentResolver.class), c.getArgument("item", String.class), 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                .executes(c -> cmd.give(c.getSource(), c.getArgument("player", PlayerSelectorArgumentResolver.class), c.getArgument("item", String.class), c.getArgument("count", Integer.class)))
                                        )
                                )
                        )
                )
                .build();
    }

    public int help(CommandSourceStack ctx) {
        if (!ctx.getSender().hasPermission("infusev1.help")) return 1;

        ctx.getSender().sendMessage(Component.text("/infuse", NamedTextColor.AQUA));
        ctx.getSender().sendMessage(Component.text(" |- help", NamedTextColor.AQUA).append(Component.text(": Shows the help message", NamedTextColor.WHITE)));
        ctx.getSender().sendMessage(Component.text(" |- reload", NamedTextColor.AQUA).append(Component.text(": Reloads the config", NamedTextColor.WHITE)));
        ctx.getSender().sendMessage(Component.text(" |- revive ", NamedTextColor.AQUA).append(Component.text("<player>", NamedTextColor.GOLD)).append(Component.text(": Revives a dead player", NamedTextColor.WHITE)));
        ctx.getSender().sendMessage(Component.text(" |- setscore ", NamedTextColor.AQUA).append(Component.text("<player> <score>", NamedTextColor.GOLD)).append(Component.text(": Sets a player's score.  Also rerolls their effects.", NamedTextColor.WHITE)));
        ctx.getSender().sendMessage(Component.text(" |- getscore ", NamedTextColor.AQUA).append(Component.text("<player>", NamedTextColor.GOLD)).append(Component.text(": Gets a player's score", NamedTextColor.WHITE)));
        ctx.getSender().sendMessage(Component.text(" \\- give ", NamedTextColor.AQUA).append(Component.text("<player> <item> [count]", NamedTextColor.GOLD)).append(Component.text(": Gives a player an infuse item.", NamedTextColor.WHITE)));

        return 1;
    }

    public int reload(CommandSourceStack ctx) {
        if (!ctx.getSender().hasPermission("infusev1.reload")) return 1;

        Infuse.getInstance().config().load();
        Infuse.getInstance().reloadRecipes();

        ctx.getSender().sendMessage(Component.text("Reloaded the config.", NamedTextColor.GREEN));

        return 1;
    }

    public int revive(CommandSourceStack ctx, String name) {
        if (!ctx.getSender().hasPermission("infusev1.revive")) return 1;

        NameAndId player = infuse.getPlayer(name);
        if (player == null || !infuse.dataManager().getBanned().contains(player)) {
            ctx.getSender().sendMessage(Component.text("Player \"" + name + "\" is not banned", NamedTextColor.RED));
            return 1;
        }

        infuse.dataManager().unban(player);
        ctx.getSender().sendMessage(Component.text("Revived " + name + "!", NamedTextColor.GREEN));

        return 1;
    }

    public int getScore(CommandSourceStack ctx, @Nullable PlayerSelectorArgumentResolver resolver) {
        List<Player> targets = null;
        if (resolver == null) {
            if (!ctx.getSender().hasPermission("infusev1.getscore")) return 1;

            if (ctx instanceof Player p) {
                targets = List.of(p);
            } else {
                ctx.getSender().sendMessage(Component.text("You must specify a target.", NamedTextColor.RED));
                return 1;
            }
        } else {
            if (!ctx.getSender().hasPermission("infusev1.getscore.other")) return 1;

            try {
                targets = resolver.resolve(ctx);
            } catch (CommandSyntaxException e) {
                ctx.getSender().sendMessage(e.componentMessage());
                return 1;
            }
        }


        targets.forEach(p -> ctx.getSender().sendMessage(Component.text(p.getName() + "'s score is " + infuse.dataManager().getScore(p.getUniqueId()))));

        return 1;
    }

    @SuppressWarnings("DataFlowIssue")
    public int setScore(CommandSourceStack ctx, PlayerSelectorArgumentResolver resolver, int score) {
        if (!ctx.getSender().hasPermission("infusev1.setscore")) return 1;

        List<Player> targets;
        try {
            targets = resolver.resolve(ctx);
        } catch (CommandSyntaxException e) {
            ctx.getSender().sendMessage(e.componentMessage());
            MessageComponentSerializer.message().deserialize(e.getRawMessage());
            return 1;
        }

        for (Player player : targets) {
            if (!(player instanceof CraftPlayer cp)) continue;
            infuse.dataManager().setScore(cp.getHandle(), score);
            infuse.dataManager().resetEffects(cp.getHandle());

            // Banning the player if necessary
            int banScore = infuse.config().banScore();
            if (banScore < 0 && score == banScore) {
                infuse.dataManager().ban(cp.getHandle().nameAndId());
            }
        }

        ctx.getSender().sendMessage(Component.text("Set %s to %d".formatted(targets.size() == 1 ? "%s's score".formatted(targets.iterator().next().getName()) : "%d player's scores".formatted(targets.size()), score), NamedTextColor.GREEN));

        return 1;
    }

    public int give(CommandSourceStack ctx, PlayerSelectorArgumentResolver resolver, String itemKey, int count) {
        if (!ctx.getSender().hasPermission("infusev1.give")) return 1;

        List<Player> targets;
        try {
            targets = resolver.resolve(ctx);
        } catch (CommandSyntaxException e) {
            ctx.getSender().sendMessage(e.componentMessage());
            return 1;
        }

        for (Player player : targets) {
            CustomItem item = CustomItem.fromKey(itemKey);
            if (item == null) {
                ctx.getSender().sendMessage(Component.text("Invalid item key \"" + itemKey + "\""));
                return 1;
            }

            ItemStack stack = item.createItem(count);

            player.give(stack.asBukkitCopy());
        }

        ctx.getSender().sendMessage(Component.text("Gave %s %s %s%s".formatted((targets.size() == 1 ? targets.iterator().next().getName() : targets.size() + " players"), (count == 1 ? "a" : count), itemKey, ( count == 1 ? "" : "s"))));

        return 1;
    }
}