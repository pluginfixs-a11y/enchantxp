package enchantxp.mixin;

import enchantxp.BookshelfCounter;
import enchantxp.EnchantXp;
import enchantxp.EnchantXpPlayerAccess;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ваниль считает полки только в {@code slotsChanged}, когда собирает офферы; по этой же причине
 * число запоминаем там — цена должна соответствовать тому зачарованию, которое игрок видит.
 * В {@code clickMenuButton} отдаём число игроку и не даём купить, если очков опыта меньше цены
 * (иначе бедный игрок платил бы «сколько есть» и получал зачарование почти бесплатно).
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {
	@Shadow
	@Final
	private ContainerLevelAccess access;

	/** Действующих полок на момент расчёта офферов. */
	@Unique
	private int enchantxp$bookshelves;

	@Inject(method = "slotsChanged", at = @At("HEAD"))
	private void enchantxp$countBookshelves(Container container, CallbackInfo ci) {
		this.enchantxp$bookshelves = BookshelfCounter.count(this.access);
	}

	@Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
	private void enchantxp$rememberBookshelves(Player player, int buttonId, CallbackInfoReturnable<Boolean> cir) {
		EnchantXpPlayerAccess access = (EnchantXpPlayerAccess) player;
		// прошлое значение не должно пережить неудачный клик: у кнопок вне 0..2 ваниль сама
		// ругается в лог, поэтому такие клики не трогаем вовсе
		access.enchantxp$setBookshelves(0);
		if (buttonId < 0 || buttonId > 2) {
			return;
		}
		if (!player.hasInfiniteMaterials()
				&& player.totalExperience < EnchantXp.config().priceFor(buttonId + 1, this.enchantxp$bookshelves)) {
			cir.setReturnValue(false);
			return;
		}
		access.enchantxp$setBookshelves(this.enchantxp$bookshelves);
	}

	/**
	 * Страховка на путь, где ваниль не дошла до {@code onEnchantmentPerformed} (пустой список
	 * зачарований): цена последнего стола не должна пережить клик. Отказ по балансу сюда не
	 * попадает — при нём {@code setReturnValue} выходит из метода inline, но там поле уже сброшено.
	 */
	@Inject(method = "clickMenuButton", at = @At("RETURN"))
	private void enchantxp$clearBookshelves(Player player, int buttonId, CallbackInfoReturnable<Boolean> cir) {
		((EnchantXpPlayerAccess) player).enchantxp$setBookshelves(0);
	}
}
