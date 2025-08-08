package com.brunoruan.Brskills.comandos;

import com.brunoruan.Brskills.SkiHabilidades;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ComandoHabilidades implements CommandExecutor {
    private final SkiHabilidades plugin;
    public ComandoHabilidades(SkiHabilidades plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando so pode ser executado por jogadores.");
            return true;
        }
        Player player = (Player) sender;
        plugin.getGuiManager().abrirMenuSelecaoModo(player);
        return true;
    }
}