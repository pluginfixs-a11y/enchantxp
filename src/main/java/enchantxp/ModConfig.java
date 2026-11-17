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
	 * Сколько очков опыта стоит нажатие 1-й, 2-й и 3-й кнопки стола.
	 * По умолчанию 100/200/300 — столько стоили 1/2/3 уровня на 30-м уровне в ванили.
	 */
	public int[] price_points = {100, 200, 300};

	/** levels — то, что ваниль вычитала бы из experienceLevel (1..3). */
	public int priceFor(int levels) {
		if (price_points == null || price_points.length == 0) {
			return 0;
		}
		int index = Math.max(0, Math.min(levels - 1, price_points.length - 1));
		return Math.max(0, price_points[index]);
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
