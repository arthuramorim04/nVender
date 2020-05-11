/**
 * Copyright NickUC
 * -
 * Esta class pertence ao projeto de NickUC
 * Discord: NickUltracraft#4550
 * Mais informações: https://nickuc.com
 * -
 * É expressamente proibido alterar o nome do proprietário do código, sem
 * expressar e deixar claramente o link para acesso da source original.
 * -
 * Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package com.nickuc.vender.manager;

import com.nickuc.ncore.api.plugin.bukkit.item.*;
import com.nickuc.vender.settings.*;
import lombok.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

public class VendaMenu {

	@AllArgsConstructor @Getter
	private enum Type {

		AUTO_VENDA("§7Auto-Venda"), VENDA_SHIFT("§7Venda Shift"), VENDA("§7Vender");

		private final String displayName;

		public boolean getStatus(Player player) {
			switch (this) {
				case AUTO_VENDA:
					return SettingsEnum.autoVenda.contains(player.getName());
				case VENDA_SHIFT:
					return SettingsEnum.vendaShift.contains(player.getName());
			}
			return false;
		}

	}

	private static ItemStack getItemStack(Player player, Type vendaType) {
		ItemBuilder itemBuilder = new ItemBuilder(vendaType == Type.AUTO_VENDA || (vendaType == Type.VENDA_SHIFT) ? Material.INK_SACK : Material.NAME_TAG)
				.displayName(vendaType.getDisplayName());

		if(vendaType != Type.VENDA) {
			itemBuilder.durability(vendaType.getStatus(player) ? 10 : 8);
		}
		return itemBuilder.createItem();
	}

	public static void openMenu(Player player) {
		Inventory inventory = Bukkit.createInventory(null, 27, "§8Opções de venda");
		inventory.setItem(11, getItemStack(player, Type.AUTO_VENDA));
		inventory.setItem(13, getItemStack(player, Type.VENDA));
		inventory.setItem(15, getItemStack(player, Type.VENDA_SHIFT));
		player.openInventory(inventory);
	}
}
