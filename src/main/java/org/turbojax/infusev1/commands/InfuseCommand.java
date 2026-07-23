package org.turbojax.infusev1.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.MainConfig;
import org.turbojax.infusev1.items.CustomItem;
import org.turbojax.infusev1.util.BannedPlayerArgumentType;
import org.turbojax.infusev1.util.InfuseItemArgumentType;

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
            <aqua> \\- give <gold>\\<player> \\<item><white>: Gives a player an infuse item.
            """);

    public static LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("infuse")
            .then(Commands.literal("help")
                .executes(InfuseCommand::help)
            )
            .then(Commands.literal("reload")
                .executes(InfuseCommand::reload)
            )
            .then(Commands.literal("revive")
                .then(Commands.argument("player", new BannedPlayerArgumentType())
                    .executes(c -> revive(c, c.getArgument("player", OfflinePlayer.class)))
                )
            )
            .then(Commands.literal("getscore")
                .then(Commands.argument("player", StringArgumentType.word())
                    .executes(c -> getScore(c, c.getArgument("player", String.class)))
                )
            )
            .then(Commands.literal("setscore")
                .then(Commands.argument("player", ArgumentTypes.player())
                    .then(Commands.argument("score", IntegerArgumentType.integer(MainConfig.minScore(), MainConfig.maxScore()))
                        .executes(c -> setScore(c, c.getArgument("player", PlayerSelectorArgumentResolver.class), c.getArgument("score", Integer.class)))
                    )
                )
            )
            .then(Commands.literal("give")
                .then(Commands.argument("player", ArgumentTypes.player())
                    .then(Commands.argument("item", new InfuseItemArgumentType())
                        .executes(c -> give(c, c.getArgument("player", PlayerSelectorArgumentResolver.class), c.getArgument("item", CustomItem.class)))
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

    public static int getScore(CommandContext<CommandSourceStack> ctx, String target) {
        CommandSender sender = ctx.getSource().getSender();
        OfflinePlayer player = Bukkit.getOfflinePlayer(target);

        sender.sendMessage(player.getName() + "'s score is " + DataManager.getScore(player));
        return 1;
    }

    public static int setScore(CommandContext<CommandSourceStack> ctx, PlayerSelectorArgumentResolver resolver, int score) {
        CommandSender sender = ctx.getSource().getSender();
        Player player;

        try {
            player = resolver.resolve(ctx.getSource()).getFirst();
        } catch (CommandSyntaxException e) {
            sender.sendMessage(msgSerializer.deserialize(e.getRawMessage()));
            return 1;
        }

        DataManager.setScore(player, score);

        List<PotionEffectType> newEffects = new ArrayList<>();
        List<PotionEffectType> possibleEffects = new ArrayList<>((score > 0) ? MainConfig.positiveEffects() : MainConfig.negativeEffects());

        for (int i = 0; i < Math.abs(score); i++) {
            newEffects.add(possibleEffects.remove((int) (Math.random() * possibleEffects.size())));
        }

        // Removing all infinite effects
        player.getActivePotionEffects().stream()
            .filter(e -> e.getDuration() == -1)
            .forEach(e -> player.removePotionEffect(e.getType()));

        // Equipping the new effects
        newEffects.forEach(e -> player.addPotionEffect(new PotionEffect(e, -1, MainConfig.getEffectiveLevel(e) - 1)));

        DataManager.setEffects(player, newEffects);

        sender.sendMessage(Component.text("Set " + player.getName() + "'s score to " + score, NamedTextColor.GREEN));

        return 1;
    }

    public static int give(CommandContext<CommandSourceStack> ctx, PlayerSelectorArgumentResolver resolver, CustomItem item) {
        CommandSender sender = ctx.getSource().getSender();

        try {
            Player p = resolver.resolve(ctx.getSource()).getFirst();
            p.give(item.createItem());

            sender.sendMessage("Gave " + p.getName() + " a " + item.getKey().getKey());
        } catch (CommandSyntaxException e) {
            sender.sendMessage(msgSerializer.deserialize(e.getRawMessage()));
        }

        return 1;
    }
}