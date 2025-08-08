package com.brunoruan.Brskills.habilidades;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public interface Habilidade extends Listener {

    enum TipoDeHabilidade {
        INDIVIDUAL,
        TIME
    }

    String getNome();
    String[] getDescricao();
    ItemStack getItemIcone(Player player, String grupo);
    String getPermissao();
    int getCustoDePontos();
    double getCustoDeCoins(int paraNivel);
    int getMaxLevel();
    TipoDeHabilidade getTipo();
}