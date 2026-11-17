package enchantxp.mixin;

import enchantxp.EnchantXp;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ваниль (26.2, байткод {@code Player.onEnchantmentPerformed}):
 * <pre>
 *   this.experienceLevel -= levels;            // 1/2/3 уровня, независимо от текущего уровня
 *   if (this.experienceLevel &lt; 0) { level = 0; progress = 0; total = 0; }
 *   this.enchantmentSeed = this.random.nextInt();
 * </pre>
 * Заменяем списание на фиксированное число очков опыта: запоминаем totalExperience на входе,
 * после ванили пересобираем состояние из {@code before - цена}. Штатный
 * {@code giveExperiencePoints} корректно пересчитывает уровень и прогресс из общего опыта,
 * поэтому «долг» из-за ванильного вычитания уровней без totalExperience не остаётся.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {
	@Unique
	private int enchantxp$xpBefore;

	/** Метка последнего «нового уровня» — ваниль играет звук, когда она старше 100 тиков. */
	@Shadow
	private int lastLevelUpTime;

	@Inject(method = "onEnchantmentPerformed", at = @At("HEAD"))
	private void enchantxp$captureXp(ItemStack stack, int levels, CallbackInfo ci) {
		this.enchantxp$xpBefore = ((Player) (Object) this).totalExperience;
	}

	@Inject(method = "onEnchantmentPerformed", at = @At("TAIL"))
	private void enchantxp$chargeFixedXp(ItemStack stack, int levels, CallbackInfo ci) {
		Player self = (Player) (Object) this;
		int target = Math.max(0, this.enchantxp$xpBefore - EnchantXp.config().priceFor(levels));

		// giveExperiencePoints набирает уровень по одному и на каждом кратном пяти играет
		// звук нового уровня. Пересборка с нуля иначе озвучивала бы «level up» на каждое
		// зачарование, поэтому сдвигаем метку: 100 тиков тишины после пересчёта.
		this.lastLevelUpTime = self.tickCount;

		self.totalExperience = 0;
		self.experienceLevel = 0;
		self.experienceProgress = 0.0F;
		self.giveExperiencePoints(target);
	}
}
