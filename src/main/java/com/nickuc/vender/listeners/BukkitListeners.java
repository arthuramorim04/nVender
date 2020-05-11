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

package com.nickuc.vender.listeners;

import com.nickuc.ncore.api.plugin.bukkit.events.Listener;
import com.nickuc.ncore.api.settings.*;
import com.nickuc.vender.manager.*;
import com.nickuc.vender.*;
import com.nickuc.vender.settings.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

import java.util.*;
import java.util.concurrent.*;

public class BukkitListeners extends Listener<nVender> {
	
	private static final ArrayList<String> delay = new ArrayList<>();

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		String invName = e.getView().getTitle();
		if (e.getView() != null && invName != null && invName.equalsIgnoreCase("§8Opções de Venda")) {
			e.setCancelled(true);
			ItemStack item = e.getCurrentItem();
			if (item != null && (item.hasItemMeta() && (item.getItemMeta().hasDisplayName()))) {
				Player p = (Player)e.getWhoClicked();
				String display = item.getItemMeta().getDisplayName();
				switch (display) {
					case "§7Auto-Venda":
						if (!p.hasPermission(Settings.getString(SettingsEnum.PERMISSION_AUTOMATICO))) {
							p.sendMessage("§cVocê não tem permissão para usar a venda automática.");
							return;
						}
						if (SettingsEnum.autoVenda.contains(p.getName())) {
							p.sendMessage("§cVocê desativou o modo de venda automática.");
							SettingsEnum.autoVenda.remove(p.getName());
						} else {
							p.sendMessage("§aVocê ativou o modo de venda automática.");
							SettingsEnum.autoVenda.add(p.getName());
							double delayDouble = plugin.getConfig().getDouble("Config.DelayAutoVenda");
							if (delayDouble < 0.5) {
								delayDouble = 2.5;
							}
							long timeLong = (long) (1000 * delayDouble);

							Timer timer = new Timer(true);
							timer.scheduleAtFixedRate(new TimerTask() {
								@Override
								public void run() {
									if (SettingsEnum.autoVenda.contains(p.getName())) {
										VendaCore.sell(plugin, p, VendaCore.Type.AUTO_VENDA);
									} else {
										cancel();
									}
								}
							}, timeLong, timeLong);
						}
						p.closeInventory();
						VendaMenu.openMenu(p);
						return;
					case "§7Vender":
						VendaCore.sell(plugin, p, VendaCore.Type.VENDA_NORMAL);
						return;
					case "§7Venda Shift":
						if (!p.hasPermission(Settings.getString(SettingsEnum.PERMISSION_SHIFT))) {
							p.sendMessage("§cVocê não tem permissão para usar a venda por shift.");
							return;
						}
						if (SettingsEnum.vendaShift.contains(p.getName())) {
							p.sendMessage("§cVocê desativou o modo de venda por shift.");
							SettingsEnum.vendaShift.remove(p.getName());
						} else {
							p.sendMessage("§aVocê ativou o modo de venda por shift.");
							SettingsEnum.vendaShift.add(p.getName());
						}
						p.closeInventory();
						VendaMenu.openMenu(p);
				}
			}
		}
	}
	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
		if (SettingsEnum.vendaShift.contains(e.getPlayer().getName())) {
			double delayDouble = plugin.getConfig().getDouble("Config.DelayShift");
			if (delayDouble <= 0) {
				VendaCore.sell(plugin, e.getPlayer(), VendaCore.Type.VENDA_SHIFT);
			} else {
				long timeLong = (long) delayDouble;
				if (!delay.contains(e.getPlayer().getName())) {
					VendaCore.sell(plugin, e.getPlayer(), VendaCore.Type.VENDA_SHIFT);
					delay.add(e.getPlayer().getName());
					plugin.runTaskLater(false, () -> delay.remove(e.getPlayer().getName()), timeLong, TimeUnit.SECONDS);
				} 
			}
		}
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		SettingsEnum.vendaShift.remove(e.getPlayer().getName());
		SettingsEnum.autoVenda.remove(e.getPlayer().getName());
	}
}
