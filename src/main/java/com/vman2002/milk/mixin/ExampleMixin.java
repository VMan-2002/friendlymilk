package com.vman2002.milk.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ExampleMixin {
	@Inject(at = @At("HEAD"), method = "useOnEntity", cancellable = true)
	public void useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> info) {
		//NameTagItem.java
		/*
			(entity instanceof PlayerEntity)
		*/
		ItemStack itemStack = user.getStackInHand(hand);
		if (itemStack.isOf(Items.BUCKET)) {
			//Check if we can milk the target
			//This code supports milking non-players but we only want to milk players right now
			//Idk how to make a gamerule
			Boolean milkOwnerType = (entity instanceof PlayerEntity);
			if (milkOwnerType) {
				//Play sfx
				user.playSound(SoundEvents.ENTITY_COW_MILK, 1.0F, 1.0F);

				//Define milk bucket
				ItemStack newMilk = Items.MILK_BUCKET.getDefaultStack();
				NbtCompound nbtCompound = newMilk.getOrCreateSubNbt("display");
				String nameThing = milkOwnerType ? "{\"text\":\"" + ((PlayerEntity)entity).getName().getString() + "\"" : (entity.hasCustomName() ? "{\"text\":\"" + entity.getCustomName().getString() + "\"" : "{\"translate\":\"" + entity.getType().getTranslationKey() + "\"");
				nbtCompound.putString("Name", "[" + nameThing + ",\"italic\":\"false\",\"color\":\"yellow\"},{\"text\":\"'s Milk\"}]");
				NbtList lore = new NbtList();
				lore.addElement(0, NbtString.of("[{\"text\":\"Collected from \",\"italic\":false,\"color\":\"dark_aqua\"}," + nameThing + (!milkOwnerType && entity.hasCustomName() ? ",\"italic\":true}]" : "}]")));
				nbtCompound.putBoolean("MilkOwnerType", milkOwnerType);
				nbtCompound.putString("MilkOwner", milkOwnerType ? ((PlayerEntity)entity).getUuidAsString() : EntityType.getId(entity.getType()).toString());
				nbtCompound.put("Lore", lore);

				//Give milk bucket item
				//With weird 1-count bucket itemstack fix
				ItemStack itemStack2 = itemStack.getCount() == 1 ? newMilk : ItemUsage.exchangeStack(itemStack, user, newMilk);
				user.setStackInHand(hand, itemStack2);
				info.setReturnValue(ActionResult.success(true));
			}
		}
	}
}