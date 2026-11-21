package enchantxp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Настройки в <code>config/enchantxp.json</code>. Читаются один раз при старте сервера.
 */
public class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("enchantxp.json");

	/**
	 * Цена в очках опыта для 1-й, 2-й и 3-й кнопки при {@link #full_bookshelves} полках и более.
	 * По умолчанию 100/200/300.
	 */
	public int[] price_points = {100, 200, 300};

	/**
	 * Цена в очках опыта для 1-й, 2-й и 3-й кнопки у стола без полок; дальше растёт линейно до
	 * {@link #price_points}. По умолчанию 10/20/30.
	 */
	public int[] price_points_weak = {10, 20, 30};

	/** Сколько действующих полок считается полным столом. В ванили сила стола упирается в 15 полок. */
	public int full_bookshelves = 15;

	/**
	 * levels — то, что ваниль вычитала бы из experienceLevel (1..3), bookshelves — действующих полок.
	 * Цена линейно идёт от слабой таблицы (0 полок) к полной ({@code full_bookshelves} и больше).
	 */
	public int priceFor(int levels, int bookshelves) {
		int index = Math.max(0, levels - 1);
		long weak = valueAt(price_points_weak, index);
		long full = valueAt(price_points, index);
		if (full_bookshelves <= 0) {
			return (int) Math.min(full, Integer.MAX_VALUE);
		}
		long shelves = Math.min(Math.max(0, bookshelves), full_bookshelves);
		// округление к ближайшему (для нечётного full_bookshelves формула «+ half» смещала бы вверх):
		// при 0 полок выходит ровно слабая цена, при full_bookshelves — ровно полная
		long scaled = weak * (full_bookshelves - shelves) + full * shelves;
		long rounded = (2 * scaled + full_bookshelves) / (2L * full_bookshelves);
		return (int) Math.max(0, Math.min(rounded, Integer.MAX_VALUE));
	}

	private static int valueAt(int[] table, int index) {
		if (table == null || table.length == 0) {
			return 0;
		}
		return Math.max(0, table[Math.min(index, table.length - 1)]);
	}

	public static ModConfig load() {
		ModConfig config = new ModConfig();
		if (Files.exists(PATH)) {
			try {
				ModConfig loaded = GSON.fromJson(Files.readString(PATH, StandardCharsets.UTF_8), ModConfig.class);
				if (loaded != null) {
					config = loaded;
					if (config.price_points == null || config.price_points.length == 0) {
						config.price_points = new int[]{100, 200, 300};
					}
					if (config.price_points_weak == null || config.price_points_weak.length == 0) {
						config.price_points_weak = new int[]{10, 20, 30};
					}
					if (config.full_bookshelves <= 0 || config.full_bookshelves > 32) {
						EnchantXp.LOGGER.warn("EnchantXP: full_bookshelves={} вне диапазона 1..32, беру 15",
								config.full_bookshelves);
						config.full_bookshelves = 15;
					}
					// цена не должна падать с числом полок: слабая цена не больше полной
					for (int i = 0; i < config.price_points_weak.length; i++) {
						int weak = Math.max(0, config.price_points_weak[i]);
						int full = valueAt(config.price_points, i);
						if (weak > full) {
							EnchantXp.LOGGER.warn("EnchantXP: price_points_weak[{}]={} больше полной цены {}, беру {}",
									i, weak, full, full);
							config.price_points_weak[i] = full;
						}
					}
				}
			} catch (Exception e) {
				EnchantXp.LOGGER.error("Не смог прочитать {}, беру значения по умолчанию", PATH, e);
			}
		}
		config.save();
		return config;
	}

	public void save() {
		try {
			Files.createDirectories(PATH.getParent());
			Files.writeString(PATH, GSON.toJson(this), StandardCharsets.UTF_8);
		} catch (IOException e) {
			EnchantXp.LOGGER.error("Не смог записать {}", PATH, e);
		}
	}
}
