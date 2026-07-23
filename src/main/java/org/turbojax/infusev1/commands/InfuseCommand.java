package org.turbojax.infusev1.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.MainConfig;
import org.turbojax.infusev1.items.CustomItem;
import org.turbojax.infusev1.util.BannedPlayerArgumentType;
import org.turbojax.infusev1.util.InfuseItemArgumentType;

import java.util.Date;
import java.util.List;

public class InfuseCommand {
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final MessageComponentSerializer msgSerializer = MessageComponentSerializer.message();
    public static final Component helpMsg = mm.deserialize("""
            <aqua>/infuse
            <aqua> |- help<white>: Shows the help message
            <aqua> |- reload<white>: Reloads the config
            <aqua> |- revive <gold>\\<player><white>: Revives a dead player
            <aqua> |- setscore <gold>\\<player> \\<score><white>: Sets a player's score.  Also rerolls their effects.
            <aqua> |- getscore <gold>\\<player><white>: Gets a player's score
            <aqua> \\- give <gold>\\<player> \\<item> [count]<white>: Gives a player an infuse item.
            """);

    public static LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("infuse")
            .then(Commands.literal("help")
                .requires(c -> c.getSender().hasPermission("infusev1.help"))
                .executes(InfuseCommand::help)
            )
            .then(Commands.literal("reload")
                .requires(c -> c.getSender().hasPermission("infusev1.reload"))
                .executes(InfuseCommand::reload)
            )
            .then(Commands.literal("revive")
                .requires(c -> c.getSender().hasPermission("infusev1.revive"))
                .then(Commands.argument("player", new BannedPlayerArgumentType())
                    .executes(c -> revive(c, c.getArgument("player", OfflinePlayer.class)))
                )
            )
            .then(Commands.literal("getscore")
                .requires(c -> c.getSender().hasPermission("infusev1.getscore"))
                .then(Commands.argument("player", ArgumentTypes.players())
                    .executes(c -> getScore(c, c.getArgument("player", PlayerSelectorArgumentResolver.class)))
                )
            )
            .then(Commands.literal("setscore")
                .requires(c -> c.getSender().hasPermission("infusev1.setscore"))
                .then(Commands.argument("player", ArgumentTypes.players())
                    .then(Commands.argument("score", IntegerArgumentType.integer(MainConfig.minScore(), MainConfig.maxScore()))
                        .executes(c -> setScore(c, c.getArgument("player", PlayerSelectorArgumentResolver.class), c.getArgument("score", Integer.class)))
                    )
                )
            )
            .then(Commands.literal("give")
                .requires(c -> c.getSender().hasPermission("infusev1.give"))
                .then(Commands.argument("player", ArgumentTypes.players())
                    .then(Commands.argument("item", new InfuseItemArgumentType())
                        .executes(c -> give(c, c.getArgument("player", PlayerSelectorArgumentResolver.class), c.getArgument("item", CustomItem.class), 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                            .executes(c -> give(c, c.getArgument("player", PlayerSelectorArgumentResolver.class), c.getArgument("item", CustomItem.class), c.getArgument("count", Integer.class)))
                        )
                    )
                )
            )
            .build();
    }

    public static int help(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        sender.sendMessage(helpMsg);

        return 1;
    }

    public static int reload(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        MainConfig.load();
        Infuse.registerRecipes();
        sender.sendMessage(Component.text("Reloaded the config.", NamedTextColor.GREEN));

        return 1;
    }

    public static int revive(CommandContext<CommandSourceStack> ctx, OfflinePlayer player) {
        CommandSender sender = ctx.getSource().getSender();

        if (!DataManager.getBanned().contains(player)) {
            sender.sendMessage(Component.text(player.getName() + " isn't banned.", NamedTextColor.RED));
        }

        DataManager.unban(player);
        sender.sendMessage(Component.text("Revived " + player.getName() + "!", NamedTextColor.GREEN));

        return 1;
    }

    public static int getScore(CommandContext<CommandSourceStack> ctx, PlayerSelectorArgumentResolver resolver) {
        CommandSender sender = ctx.getSource().getSender();

        List<Player> targets;

        try {
            targets = resolver.resolve(ctx.getSource());
        } catch (CommandSyntaxException e) {
            sender.sendMessage(msgSerializer.deserialize(e.getRawMessage()));
            return 1;
        }
        
        targets.forEach(p -> sender.sendMessage(p.getName() + "'s score is " + DataManager.getScore(p)));

        return 1;
    }

    public static int setScore(CommandContext<CommandSourceStack> ctx, PlayerSelectorArgumentResolver resolver, int score) {
        CommandSender sender = ctx.getSource().getSender();
        List<Player> targets;

        try {
            targets = resolver.resolve(ctx.getSource());
        } catch (CommandSyntaxException e) {
            sender.sendMessage(msgSerializer.deserialize(e.getRawMessage()));
            return 1;
        }

        targets.forEach(player -> {
            DataManager.setScore(player, score);
            DataManager.resetEffects(player);

            // Banning the player if necessary
            int banScore = MainConfig.banScore();
            if (banScore < 0 && score == banScore) {
                player.ban("Ran out of lives!", (Date) null, null);
                DataManager.ban(player);
            }
        });

        sender.sendMessage(Component.text("Set " + (targets.size() == 1 ? targets.getFirst().getName() + "'s score" : targets.size() + " player's scores") + " to " + score, NamedTextColor.GREEN));

        return 1;
    }

    public static int give(CommandContext<CommandSourceStack> ctx, PlayerSelectorArgumentResolver resolver, CustomItem item, int count) {
        CommandSender sender = ctx.getSource().getSender();

        try {
            List<Player> players = resolver.resolve(ctx.getSource());
            
            players.stream()
                .forEach(p -> {
                    ItemStack stack = item.createItem();
                    stack.setAmount(count);

                    p.give(stack);
                });

            sender.sendMessage("Gave " + (players.size() == 1 ? players.getFirst().getName() : players.size() + " players") + " " + (count == 1 ? "a" : count) + " " + item.getKey().getKey() + ( count == 1 ? "" : "s"));
        } catch (CommandSyntaxException e) {
            sender.sendMessage(msgSerializer.deserialize(e.getRawMessage()));
        }

        return 1;
    }
}