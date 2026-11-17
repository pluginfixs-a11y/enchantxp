package enchantxp;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Стол зачарований платит фиксированным числом очков опыта вместо уровней.
 * Ваниль в {@code Player.onEnchantmentPerformed} вычитает 1/2/3 уровня из {@code experienceLevel},
 * то есть на 100-м уровне третья кнопка стоит ~1500 очков, а на 30-м — 306. Здесь цена постоянная.
 */
public class EnchantXp implements ModInitializer {
	public static final String MOD_ID = "enchantxp";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static ModConfig config;

	public static ModConfig config() {
		if (config == null) {
			config = ModConfig.load();
		}
		return config;
	}

	@Override
	public void onInitialize() {
		ModConfig c = config();
		LOGGER.info("EnchantXP: цена в очках опыта {} (кнопки 1/2/3)", Arrays.toString(c.price_points));
	}
}
