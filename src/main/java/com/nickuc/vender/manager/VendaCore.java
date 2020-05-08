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

import com.nickuc.ncore.api.cache.*;
import com.nickuc.ncore.api.logger.*;
import com.nickuc.ncore.api.plugin.shared.sender.*;
import com.nickuc.ncore.api.settings.*;
import com.nickuc.ncore.api.utils.val.*;
import com.nickuc.vender.*;
import com.nickuc.vender.objects.*;
import com.nickuc.vender.settings.*;
import lombok.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

import java.util.*;

public class VendaCore {

	public static void sell(nVender nvender, Player player, Type type) {
		Optional<SharedPlayer> sharedPlayerOpt = Cache.getSharedPlayer(player.getName());
		if (!sharedPlayerOpt.isPresent()) {
			player.sendMessage("§cOps, parece que você não está incluído no cache. Reporte isso para NickUC#4550.");
			return;
		}

		SharedPlayer sharedPlayer  = sharedPlayerOpt.get();
		if (sharedPlayer.cache().exists("sellLock")) return;

		sharedPlayer.cache().define("sellLock", "");
		nvender.runTask(Settings.getBoolean(SettingsEnum.RUN_SELL_ASYNC), () -> {
			try {
				performSell(nvender, player, type);
			} catch (Exception e) {
				e.printStackTrace();
				player.sendMessage("§cUm erro desconhecido ocorreu...");
			} finally {
				sharedPlayer.cache().remove("sellLock");
			}
		});
	}

	private static void performSell(nVender nvender, Player player, Type type) {
		if (!player.isOnline()) return;
		if (inventoryEmpty(player)) {
			if (type == Type.VENDA_NORMAL) player.sendMessage(Messages.getMessage(MessagesEnum.INVENTORY_EMPTY));
			return;
		}
		if (SettingsEnum.loadedItens == null || SettingsEnum.loadedItens.size() == 0) {
			player.sendMessage("§cAinda não existem itens configurados para serem vendidos.");
			return;
		}

		int itensSold = 0;
		double itensMoney = 0;
		for (ItemStack item : getItensInInventory(player)) {
			for (ItemStack itemStackConfig : SettingsEnum.loadedItens) {
				if (item.getType() == itemStackConfig.getType() && item.getDurability() == itemStackConfig.getDurability()) {
					ConsoleLogger.debug(" [Player Inventory] The player contains " + item.getType().name() + ".");
					SellItem nvenderitem = SellItem.valueOf(nvender, item);
					int amount = item.getAmount();
					double price = nvenderitem.getPrice();
					double giveMoney = price * amount;
					itensSold += amount;
					itensMoney += processMultiplicador(player, giveMoney/nvenderitem.getQuantidade());
					for (int i = 0; i < itensSold; i++) {
						player.getInventory().removeItem(item);
					}
				}
			}
		}
		player.updateInventory();

		ConsoleLogger.debug("[PlayerInventory] Giving money after sell...");
		nVender.economy.depositPlayer(player, itensMoney);

		if (itensSold == 0) {
			if (type == Type.VENDA_NORMAL) {
				player.sendMessage(Messages.getMessage(MessagesEnum.NO_ITENS));
			}
			return;
		}
		player.sendMessage(Messages.getMessage(MessagesEnum.VENDIDO).replace("%itens%", StringUtils.formatNumber(itensSold)).replace("%dinheiro%", StringUtils.formatNumber(itensMoney)));
	}

	private static boolean inventoryEmpty(Player player) {
		PlayerInventory inv = player.getInventory();
		for (ItemStack i : inv.getContents()) {
			if (i != null && i.getType() != Material.AIR) return false;
		}
		return true;
	}

	private static double processMultiplicador(Player player, double valorOriginal) {
		switch (SettingsEnum.getMultiplicadorType()) {

			case DETECCAO_GRUPO:
				for (String multiplicador : SettingsEnum.multiplicadores) {
					boolean inGroup = nVender.permission.playerInGroup(player, multiplicador.split("-")[0]);
					if (inGroup) {
						double calc = Double.parseDouble(multiplicador.split("-")[2])/100;
						ConsoleLogger.debug(" [Multiplicador] The price original is: " + valorOriginal + ". The final is: " + valorOriginal*calc + ". The multiplicator is " + multiplicador.split("-")[2] + "X for group " + multiplicador.split("-")[0]);
						return valorOriginal*calc;
					}
				}
				break;
			case DETECCAO_PERMISSAO:
				for (String multiplicador : SettingsEnum.multiplicadores) {
					boolean hasperm = player.hasPermission(multiplicador.split("-")[1]);
					if (hasperm) {
						double calc = Double.parseDouble(multiplicador.split("-")[2])/100;
						ConsoleLogger.debug(" [Multiplicador] The price original is: " + valorOriginal + ". The final is: " + valorOriginal*calc + ". The multiplicator is " + multiplicador.split("-")[2] + "X  for group " + multiplicador.split("-")[0]);
						return valorOriginal*calc;
					}
				}
				break;
		}
		return valorOriginal;
	}

	private static ArrayList<ItemStack> getItensInInventory(Player player) {
		ConsoleLogger.debug("Loading itens in " + player.getName() + " inventory...");
		ItemStack[] contents = player.getInventory().getContents();
		ArrayList<ItemStack> itens = new ArrayList<>();
		for (ItemStack item : contents) {
			if (item != null) {
				itens.add(item);
				ConsoleLogger.debug(" [Player Inventory] " + item.getType().name() + " X" + item.getAmount());
			}
		}
		return itens;
	}

	@AllArgsConstructor @Getter
	public enum Type {
		AUTO_VENDA, VENDA_SHIFT, VENDA_NORMAL
	}

	@AllArgsConstructor @Getter
	public enum MultiplicadorType {

		DETECCAO_GRUPO("Detecção por Grupo", "grupo"), DETECCAO_PERMISSAO("Detecção por Permissão", "permissao"), NENHUM("Nenhum", "nenhum");

		private final String nome;
		private final String configNome;

		public static MultiplicadorType getByConfig(String config) {
			if (config == null || nVender.permission == null) return DETECCAO_PERMISSAO;

			for (MultiplicadorType multiplicadorMethodType : MultiplicadorType.values()) {
				if (multiplicadorMethodType.configNome.equals(config.toLowerCase())) return multiplicadorMethodType;
			}
			return nVender.permission != null && nVender.permission.isEnabled() ?  MultiplicadorType.DETECCAO_GRUPO :  MultiplicadorType.DETECCAO_PERMISSAO;
		}

	}

}
