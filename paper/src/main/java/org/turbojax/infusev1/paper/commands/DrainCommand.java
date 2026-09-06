package org.turbojax.infusev1.paper.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.items.InfuseEffect;

public class DrainCommand {
    public static LiteralCommandNode<CommandSourceStack> build(String alias) {
        return Commands.literal(alias)
            .requires(src -> src.getSender().hasPermission("infusev1.drain"))
            .executes(ctx -> drain(ctx.getSource()))
            .build();
    }

    public static int drain(CommandSourceStack src) throws CommandSyntaxException {
        Infuse infuse = Infuse.getInstance();
        if (!(src.getSender() instanceof Player player)) {
            src.getSender().sendMessage(Component.text("You must be a player to use this command!", NamedTextColor.RED));
            return 1;
        }

        int slot = player.getInventory().firstEmpty();
        if (slot == -1) {
            src.getSender().sendMessage(Component.text("You don't have any space in your inventory!", NamedTextColor.RED));
            return 1;
        }

        int score = infuse.dataManager().getScore(player.getUniqueId());
        if (score <= 0) {
            src.getSender().sendMessage(Component.text("You don't have any positive effects to drain!", NamedTextColor.RED));
            return 1;
        }

        var effects = infuse.dataManager().getEffects(((CraftPlayer)player).getHandle().nameAndId());
        var effect = effects.get((int) (Math.random() * effects.size()));

        infuse.dataManager().setScore(player.getUniqueId(), score - 1);
        infuse.dataManager().removeEffect(((CraftPlayer)player).getHandle(), effect);
        src.getSender().sendMessage(Component.text("You drained your ", NamedTextColor.GREEN).append(Component.text(effect.key().identifier().toShortString().toUpperCase(), NamedTextColor.YELLOW)));
        player.give(CraftItemStack.asBukkitCopy(new InfuseEffect(effect, player.getName()).createItem()));

        return 1;
    }
}
