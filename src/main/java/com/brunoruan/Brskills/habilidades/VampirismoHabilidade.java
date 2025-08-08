package com.brunoruan.Brskills.habilidades;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.brunoruan.Brskills.SkiHabilidades;
import com.brunoruan.Brskills.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
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

public class VampirismoHabilidade implements Habilidade {
    private final SkiHabilidades plugin;
    private final BedWars bedwarsAPI;

    public VampirismoHabilidade(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.bedwarsAPI = SkiHabilidades.getBedwarsAPI();
    }

    @Override
    public String getNome() { return "Vampirismo"; }
    @Override
    public int getMaxLevel() { return 1; }
    @Override
    public TipoDeHabilidade getTipo() { return TipoDeHabilidade.INDIVIDUAL; }

    @Override
    public String[] getDescricao() {
        return new String[]{
                ChatColor.GRAY + "Alimente-se da força vital de",
                ChatColor.GRAY + "seus oponentes. Cada abate que",
                ChatColor.GRAY + "você consegue restaura 2 corações",
                ChatColor.GRAY + "da sua própria vida."
        };
    }

    @Override
    public ItemStack getItemIcone(Player player, String grupo) {
        ItemStack item = new ItemStack(Material.NETHER_STALK);
        ItemMeta meta = item.getItemMeta();
        PlayerData dados = plugin.getPlayerSkillManager().carregarDados(player);
        int nivel = dados.getNivelHabilidade(grupo, getNome());

        ChatColor corDoNome = (nivel > 0) ? ChatColor.GREEN : ChatColor.YELLOW;
        meta.setDisplayName(corDoNome + getNome());

        List<String> lore = new ArrayList<>(Arrays.asList(getDescricao()));
        lore.add("");
        if (nivel < getMaxLevel()) {
            lore.add(ChatColor.GRAY + "Custo: " + ChatColor.YELLOW + getCustoDeCoins(1) + " coins");
            lore.add("");
            lore.add(ChatColor.GREEN + "Clique para comprar!");
        } else {
            lore.add(ChatColor.YELLOW + "Adquirido!");
            lore.add(ChatColor.GRAY + "Sem evoluções!");
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
    public String getPermissao() { return "skihabilidades.habilidade.vampirismo"; }
    @Override
    public int getCustoDePontos() { return 3; }
    @Override
    public double getCustoDeCoins(int paraNivel) { return 7000; }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerKill(PlayerKillEvent event) {
        Player assassino = event.getKiller();
        if (assassino == null) return;

        IArena arena = event.getArena();
        if (arena == null) return;

        String grupo = arena.getGroup();
        PlayerData dados = plugin.getPlayerSkillManager().carregarDados(assassino);
        if (!dados.isHabilidadeAtiva(grupo, getNome())) return;

        double vidaParaCurar = 4.0;
        double vidaMaxima = assassino.getMaxHealth();
        double novaVida = assassino.getHealth() + vidaParaCurar;
        assassino.setHealth(Math.min(novaVida, vidaMaxima));
        assassino.sendMessage(ChatColor.DARK_RED + "[Vampirismo] " + ChatColor.GREEN + "Você recuperou 2 corações!");
        assassino.playSound(assassino.getLocation(), Sound.DRINK, 1.0f, 1.2f);
    }
}