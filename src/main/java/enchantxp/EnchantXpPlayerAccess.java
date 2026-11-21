package enchantxp;

/**
 * Мост между {@code EnchantmentMenu} и {@code Player}: меню знает число полок у стола
 * и кладёт его игроку, а миксин {@code Player.onEnchantmentPerformed} по нему выбирает цену.
 */
public interface EnchantXpPlayerAccess {
	void enchantxp$setBookshelves(int bookshelves);
}
