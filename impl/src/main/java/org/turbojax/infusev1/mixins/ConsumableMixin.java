package org.turbojax.infusev1.mixins;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.turbojax.infusev1.items.CustomItem;

@Mixin(Consumable.class)
public abstract class ConsumableMixin {
    @Inject(method="onConsume", at= @At(value = "HEAD"))
    private void handleConsumeEvent(Level level, LivingEntity user, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (!(user instanceof ServerPlayer player)) return;

        CustomItem ci = CustomItem.fromItemStack(stack);
        if (ci == null) return;

        cir.setReturnValue(ci.consume(player, stack));
    }
}
