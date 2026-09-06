package org.turbojax.infusev1.fabric.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.items.InfuseEffect;

public class DrainCommand {
    public static LiteralCommandNode<CommandSourceStack> build(String alias) {
        return Commands.literal(alias)
            .requires(src -> InfuseCommand.hasPermission(src, "infusev1.drain"))
            .executes(ctx -> drain(ctx.getSource()))
            .build();
    }

    public static int drain(CommandSourceStack src) throws CommandSyntaxException {
        Infuse infuse = Infuse.getInstance();
        ServerPlayer player = src.getPlayerOrException();

        int slot = player.getInventory().getFreeSlot();
        if (slot == -1) {
            src.sendSystemMessage(Component.literal("You don't have any space in your inventory!").withColor(ChatFormatting.RED.getColor()));
            return 1;
        }

        int score = infuse.dataManager().getScore(player);
        if (score <= 0) {
            src.sendSystemMessage(Component.literal("You don't have any positive effects to drain!").withColor(ChatFormatting.RED.getColor()));
            return 1;
        }

        var effects = infuse.dataManager().getEffects(player);
        var effect = effects.get((int) (Math.random() * effects.size()));

        infuse.dataManager().setScore(player, score - 1);
        infuse.dataManager().removeEffect(player, effect);
        src.sendSystemMessage(Component.literal("You drained your ").withColor(ChatFormatting.GREEN.getColor()).append(Infuse.getEffectName(effect)));
        player.addItem(new InfuseEffect(effect, player.getPlainTextName()).createItem());

        return 1;
    }
}
