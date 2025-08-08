package com.brunoruan.Brskills.listeners;

import com.brunoruan.Brskills.SkiHabilidades;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {
    private final SkiHabilidades plugin;

    public PlayerConnectionListener(SkiHabilidades plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Carrega os dados do jogador do arquivo para a memória quando ele entra
        plugin.getPlayerSkillManager().carregarDados(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Salva os dados do jogador da memória para o arquivo quando ele sai
        plugin.getPlayerSkillManager().descarregarDados(event.getPlayer());
    }
}