package org.turbojax.infusev1.fabric.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;

import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.MainConfig;
import org.turbojax.infusev1.items.CustomItem;

import java.util.Collection;
import java.util.List;

public class InfuseCommand {
    private final Infuse infuse = Infuse.getInstance();
    
    public static LiteralCommandNode<CommandSourceStack> build(String alias) {
        MainConfig config = Infuse.getInstance().config();
        InfuseCommand cmd = new InfuseCommand();

        return Commands.literal(alias)
            .then(Commands.literal("help")
                .requires(src -> hasPermission(src, "infusev1.help"))
                .executes(ctx -> cmd.help(ctx.getSource()))
            )
            .then(DrainCommand.build("drain"))
            .then(Commands.literal("reload")
                .requires(src -> hasPermission(src, "infusev1.reload", Permissions.COMMANDS_ADMIN))
                .executes(ctx -> cmd.reload(ctx.getSource()))
            )
            .then(Commands.literal("getscore")
                .requires(src -> hasPermission(src, "infusev1.getscore"))
                .executes(c -> cmd.getScore(c.getSource(), null))
                .then(Commands.argument("player", EntityArgument.players())
                    .requires(src -> hasPermission(src, "infusev1.getscore.other", Permissions.COMMANDS_ADMIN))
                    .executes(c -> cmd.getScore(c.getSource(), EntityArgument.getPlayers(c, "player")))
                )
            )
            .then(Commands.literal("setscore")
                .requires(src -> hasPermission(src, "infusev1.setscore", Permissions.COMMANDS_ADMIN))
                .then(Commands.argument("player", EntityArgument.players())
                    .then(Commands.argument("score", IntegerArgumentType.integer(config.minScore(), config.maxScore()))
                        .executes(c -> cmd.setScore(c.getSource(), EntityArgument.getPlayers(c, "player"), c.getArgument("score", Integer.class)))
                    )
                )
            )
            .then(Commands.literal("give")
                .requires(src -> hasPermission(src, "infusev1.give", Permissions.COMMANDS_ADMIN))
                .then(Commands.argument("player", EntityArgument.players())
                    .then(Commands.argument("item", StringArgumentType.word())
                        .suggests((_, builder) -> {
                            CustomItem.getRegisteredItems().keySet().stream()
                                    .filter(s -> s.contains(builder.getRemaining().toLowerCase()))
                                    .sorted()
                                    .forEach(builder::suggest);

                            return builder.buildFuture();
                        })
                        .executes(c -> cmd.give(c.getSource(), EntityArgument.getPlayers(c, "player"), c.getArgument("item", String.class), 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                            .executes(c -> cmd.give(c.getSource(), EntityArgument.getPlayers(c, "player"), c.getArgument("item", String.class), c.getArgument("count", Integer.class)))
                        )
                    )
                )
            )
            .build();
    }

    public int help(CommandSourceStack ctx) {
        ctx.sendSystemMessage(Component.literal("/infuse").withColor(TextColor.AQUA));
        ctx.sendSystemMessage(Component.literal(" |- help").withColor(TextColor.AQUA).append(Component.literal(": Shows the help message").withColor(TextColor.WHITE)));
        ctx.sendSystemMessage(Component.literal(" |- drain").withColor(TextColor.AQUA).append(Component.literal(": Drains a positive effect").withColor(TextColor.WHITE)));
        ctx.sendSystemMessage(Component.literal(" |- reload").withColor(TextColor.AQUA).append(Component.literal(": Reloads the config").withColor(TextColor.WHITE)));
        ctx.sendSystemMessage(Component.literal(" |- setscore ").withColor(TextColor.AQUA).append(Component.literal("<targets> <score>").withColor(TextColor.GOLD)).append(Component.literal(": Sets a player's score.  Also rerolls their effects.").withColor(TextColor.WHITE)));
        ctx.sendSystemMessage(Component.literal(" |- getscore ").withColor(TextColor.AQUA).append(Component.literal("[targets]").withColor(TextColor.GOLD)).append(Component.literal(": Gets a player's score").withColor(TextColor.WHITE)));
        ctx.sendSystemMessage(Component.literal(" \\- give ").withColor(TextColor.AQUA).append(Component.literal("<targets> <item> [count]").withColor(TextColor.GOLD)).append(Component.literal(": Gives a player an infuse item.").withColor(TextColor.WHITE)));

        return 1;
    }

    public int reload(CommandSourceStack ctx) {
        Infuse.getInstance().config().load();
        // TODO: Reload recipes
        ctx.sendSystemMessage(Component.literal("Reloaded the config.").withColor(TextColor.GREEN));

        return 1;
    }

    public int getScore(CommandSourceStack ctx, @Nullable Collection<ServerPlayer> targets) {
        if (targets == null) {
            ServerPlayer target = ctx.getPlayer();

            if (target != null) {
                targets = List.of(target);
            } else {
                ctx.sendSystemMessage(Component.literal("You must specify a target.").withColor(TextColor.RED));
                return 1;
            }
        }

        targets.forEach(p -> ctx.sendSystemMessage(Component.literal(p.getPlainTextName() + "'s score is " + infuse.dataManager().getScore(p))));

        return 1;
    }
    
    public int setScore(CommandSourceStack ctx, Collection<ServerPlayer> targets, int score) {
        for (ServerPlayer player : targets) {
            infuse.dataManager().setScore(player, score);
            infuse.dataManager().resetEffects(player);
        }

        ctx.sendSystemMessage(Component.literal("Set %s to %d".formatted(targets.size() == 1 ? "%s's score".formatted(targets.iterator().next().getPlainTextName()) : "%d player's scores".formatted(targets.size()), score)).withColor(TextColor.GREEN));

        return 1;
    }

    public int give(CommandSourceStack ctx, Collection<ServerPlayer> targets, String itemKey, int count) {
        for (ServerPlayer player : targets) {
            CustomItem item = CustomItem.fromKey(itemKey);
            if (item == null) {
                ctx.sendSystemMessage(Component.literal("Invalid item key \"" + itemKey + "\""));
                return 1;
            }

            ItemStack stack = item.createItem(count);

            player.addItem(stack);
        }

        ctx.sendSystemMessage(Component.literal("Gave %s %s %s%s".formatted((targets.size() == 1 ? targets.iterator().next().getPlainTextName() : targets.size() + " players"), (count == 1 ? "a" : count), itemKey, ( count == 1 ? "" : "s"))));

        return 1;
    }

    public static boolean hasPermission(CommandSourceStack source, String permission) {
        return hasPermission(source, permission, null);
    }

    public static boolean hasPermission(CommandSourceStack source, String permission, @Nullable Permission fallbackPermission) {
        // TODO: Check luckperms

        if (fallbackPermission == null) return true;

        return source.permissions().hasPermission(fallbackPermission);
    }
}