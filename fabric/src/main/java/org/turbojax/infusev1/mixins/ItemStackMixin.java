package org.turbojax.infusev1.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.turbojax.infusev1.items.CustomItem;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @WrapOperation(method="use", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult infusev1_interceptItemUse(Item instance, Level level, Player player, InteractionHand hand, Operation<InteractionResult> original) {
        ItemStack item = player.getItemBySlot(hand.asEquipmentSlot());

        CustomItem ci = CustomItem.fromItemStack(item);
        if (ci == null) return original.call(instance, level, player, hand);

        return ci.interact(player, item);
    }
}
