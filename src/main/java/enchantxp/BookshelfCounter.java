package enchantxp;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.EnchantingTableBlock;

/**
 * Считает полки вокруг стола ровно так же, как ваниль в {@code EnchantmentMenu.slotsChanged}:
 * те же смещения и та же проверка {@link EnchantingTableBlock#isValidBookShelf}.
 */
public final class BookshelfCounter {
	private BookshelfCounter() {
	}

	/**
	 * @return число действующих полок; 0, если позиция стола недоступна (клиентское меню).
	 */
	public static int count(ContainerLevelAccess access) {
		return access.evaluate((level, pos) -> {
			int bookshelves = 0;
			for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
				if (EnchantingTableBlock.isValidBookShelf(level, pos, offset)) {
					bookshelves++;
				}
			}
			return bookshelves;
		}, 0);
	}
}
