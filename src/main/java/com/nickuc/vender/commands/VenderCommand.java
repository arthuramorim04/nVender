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

package com.nickuc.vender.commands;

import com.nickuc.ncore.api.config.*;
import com.nickuc.ncore.api.plugin.shared.command.*;
import com.nickuc.ncore.api.plugin.shared.sender.*;
import com.nickuc.ncore.api.settings.*;
import com.nickuc.vender.manager.*;
import com.nickuc.vender.*;
import com.nickuc.vender.settings.*;
import org.bukkit.entity.*;

public class VenderCommand extends SharedCommand<nVender> {

	public VenderCommand() {
		super("vender");
		addPermission(Settings.getString(SettingsEnum.PERMISSION_VENDER)).setPermissionMessage(Messages.getMessage(MessagesEnum.NO_PERMISSION));
	}

	@Override
	public void execute(SharedSender sender, String lb, String[] args) throws Exception {
		if (!(sender instanceof SharedPlayer)) {
			reload(sender);
			return;
		}

		Player player = sender.getSender();
		if(args.length == 0) {
			VendaMenu.openMenu(player);
			return;
		}

		String subcmd = args[0];
		if(subcmd.equalsIgnoreCase("reload") || subcmd.equalsIgnoreCase("r")) {
			if(!sender.hasPermission("nvender.admin")) {
				sender.sendMessage(Messages.getMessage(MessagesEnum.NO_PERMISSION));
				return;
			}
			reload(sender);
			return;
		}
		sender.sendMessage("§cSub-comando inexistente.");
	}

	private void reload(SharedSender sender) throws Exception {
		nConfig config = new nConfig("config.yml");
		if (!config.exists()) {
			config.saveDefaultConfig("config.yml");
		}
		SettingsEnum.reload(config);
		MessagesEnum.reload(config);
		sender.sendMessage(Messages.getMessage(MessagesEnum.CONFIG_RELOADED));
	}
}
