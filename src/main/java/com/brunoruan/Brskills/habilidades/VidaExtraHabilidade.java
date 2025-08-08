package com.brunoruan.Brskills.habilidades;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import com.brunoruan.Brskills.SkiHabilidades;
import com.brunoruan.Brskills.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VidaExtraHabilidade implements Habilidade {
    private final SkiHabilidades plugin;
    private final BedWars bedwarsAPI;

    public VidaExtraHabilidade(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.bedwarsAPI = SkiHabilidades.getBedwarsAPI();
    }

    @Override
    public String getNome() { return "Vida Extra"; }
    @Override
    public int getMaxLevel() { return 3; }
    @Override
    public TipoDeHabilidade getTipo() { return TipoDeHabilidade.INDIVIDUAL; }

    @Override
    public String[] getDescricao() {
        return new String[]{
                ChatColor.GRAY + "Mais resistente que o normal.",
                ChatColor.GRAY + "Comece e renasça sempre com",
                ChatColor.GRAY + "corações extras para sobreviver."
        };
    }

    @Override
    public ItemStack getItemIcone(Player player, String grupo) {
        ItemStack item = new ItemStack(Material.GHAST_TEAR);
        ItemMeta meta = item.getItemMeta();
        PlayerData dados = plugin.getPlayerSkillManager().carregarDados(player);
        int nivelAtual = dados.getNivelHabilidade(grupo, getNome());

        ChatColor corDoNome = (nivelAtual > 0) ? ChatColor.GREEN : ChatColor.YELLOW;
        meta.setDisplayName(corDoNome + getNome());

        List<String> lore = new ArrayList<>(Arrays.asList(getDescricao()));
        lore.add("");

        if (nivelAtual > 0) {
            if (nivelAtual < getMaxLevel()) {
                lore.add(ChatColor.GREEN + "Clique para evoluir!");
            } else {
                lore.add(ChatColor.GOLD + "Nível máximo atingido.");
            }
            lore.add(ChatColor.YELLOW + "Adquirido!");
        } else {
            lore.add(ChatColor.GRAY + "Custo: " + ChatColor.YELLOW + getCustoDeCoins(1) + " coins");
            lore.add("");
            lore.add(ChatColor.GREEN + "Clique para comprar!");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        if (dados.isHabilidadeAtiva(grupo, getNome())) {
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            ItemMeta finalMeta = item.getItemMeta();
            finalMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(finalMeta);
        }

        return item;
    }

    @Override
    public String getPermissao() { return "skihabilidades.habilidade.vidaextra"; }
    @Override
    public int getCustoDePontos() { return 1; }
    @Override
    public double getCustoDeCoins(int paraNivel) {
        switch (paraNivel) {
            case 1: return 1500;
            case 2: return 3000;
            case 3: return 5000;
            default: return Double.MAX_VALUE;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGameStart(GameStateChangeEvent event) {
        if (event.getNewState() == GameState.playing) {
            for (Player p : event.getArena().getPlayers()) {
                verificarEaplicar(p);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerReSpawnEvent event) {
        verificarEaplicar(event.getPlayer());
    }

    private void verificarEaplicar(Player player) {
        IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(player);
        if (arena == null) return;
        String grupo = arena.getGroup();
        PlayerData dados = plugin.getPlayerSkillManager().carregarDados(player);

        if (dados.isHabilidadeAtiva(grupo, getNome())) {
            int nivel = dados.getNivelHabilidade(grupo, getNome());
            if (nivel > 0) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    double vidaExtra = nivel;
                    player.setMaxHealth(20.0 + vidaExtra);
                    player.setHealth(20.0 + vidaExtra);
                }, 1L);
            }
        } else {
            if (player.getMaxHealth() > 20.0) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.setMaxHealth(20.0);
                }, 1L);
            }
        }
    }
}