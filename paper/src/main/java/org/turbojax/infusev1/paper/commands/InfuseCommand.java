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
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.MainConfig;
import org.turbojax.infusev1.items.CustomItem;

import java.util.List;

public class InfuseCommand {
    private final Infuse infuse = Infuse.getInstance();

    public static LiteralCommandNode<CommandSourceStack> build(String alias) {
        MainConfig config = Infuse.getInstance().config();
        InfuseCommand cmd = new InfuseCommand();

        return Commands.literal(alias)
            .then(Commands.literal("help")
                .requires(src -> src.getSender().hasPermission("infusev1.help"))
                .executes(ctx -> cmd.help(ctx.getSource()))
            )
            .then(DrainCommand.build("drain"))
            .then(Commands.literal("reload")
                .requires(src -> src.getSender().hasPermission("infusev1.reload"))
                .executes(ctx -> cmd.reload(ctx.getSource()))
            )
            .then(Commands.literal("getscore")
                .requires(src -> src.getSender().hasPermission("infusev1.getscore"))
                .executes(c -> cmd.getScore(c.getSource(), null))
                .then(Commands.argument("player", ArgumentTypes.players())
                    .requires(src -> src.getSender().hasPermission("infusev1.getscore.other"))
                    .executes(c -> cmd.getScore(c.getSource(), c.getArgument("player", PlayerSelectorArgumentResolver.class)))
                )
            )
            .then(Commands.literal("setscore")
                .requires(src -> src.getSender().hasPermission("infusev1.setscore"))
                .then(Commands.argument("player", ArgumentTypes.players())
                    .then(Commands.argument("score", IntegerArgumentType.integer(config.minScore(), config.maxScore()))
                        .executes(c -> cmd.setScore(c.getSource(), c.getArgument("player", PlayerSelectorArgumentResolver.class), c.getArgument("score", Integer.class)))
                    )
                )
            )
            .then(Commands.literal("give")
                .requires(src -> src.getSender().hasPermission("infusev1.give"))
                .then(Commands.argument("player", ArgumentTypes.players())
                        .then(Commands.argument("item", StringArgumentType.word())
                            .suggests((c, builder) -> {
                                    CustomItem.getRegisteredItems().keySet().stream()
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
        ctx.getSender().sendMessage(Component.text("/infuse", NamedTextColor.AQUA));
        ctx.getSender().sendMessage(Component.text(" |- help", NamedTextColor.AQUA).append(Component.text(": Shows the help message", NamedTextColor.WHITE)));
        ctx.getSender().sendMessage(Component.text(" |- reload", NamedTextColor.AQUA).append(Component.text(": Reloads the config", NamedTextColor.WHITE)));
        ctx.getSender().sendMessage(Component.text(" |- setscore ", NamedTextColor.AQUA).append(Component.text("<player> <score>", NamedTextColor.GOLD)).append(Component.text(": Sets a player's score.  Also rerolls their effects.", NamedTextColor.WHITE)));
        ctx.getSender().sendMessage(Component.text(" |- getscore ", NamedTextColor.AQUA).append(Component.text("<player>", NamedTextColor.GOLD)).append(Component.text(": Gets a player's score", NamedTextColor.WHITE)));
        ctx.getSender().sendMessage(Component.text(" \\- give ", NamedTextColor.AQUA).append(Component.text("<player> <item> [count]", NamedTextColor.GOLD)).append(Component.text(": Gives a player an infuse item.", NamedTextColor.WHITE)));

        return 1;
    }

    public int reload(CommandSourceStack ctx) {
        Infuse.getInstance().config().load();
        Infuse.getInstance().reloadRecipes();

        ctx.getSender().sendMessage(Component.text("Reloaded the config.", NamedTextColor.GREEN));

        return 1;
    }

    public int getScore(CommandSourceStack ctx, @Nullable PlayerSelectorArgumentResolver resolver) {
        List<Player> targets;
        if (resolver == null) {
            if (ctx.getSender() instanceof Player p) {
                targets = List.of(p);
            } else {
                ctx.getSender().sendMessage(Component.text("You must specify a target.", NamedTextColor.RED));
                return 1;
            }
        } else {
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
        }

        ctx.getSender().sendMessage(Component.text("Set %s to %d".formatted(targets.size() == 1 ? "%s's score".formatted(targets.iterator().next().getName()) : "%d player's scores".formatted(targets.size()), score), NamedTextColor.GREEN));

        return 1;
    }

    public int give(CommandSourceStack ctx, PlayerSelectorArgumentResolver resolver, String itemKey, int count) {
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