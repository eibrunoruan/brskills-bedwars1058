package com.brunoruan.Brskills.habilidades;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.brunoruan.Brskills.SkiHabilidades;
import com.brunoruan.Brskills.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ToqueIgneoHabilidade implements Habilidade {
    private final SkiHabilidades plugin;
    private final BedWars bedwarsAPI;
    private final Random random = new Random();

    public ToqueIgneoHabilidade(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.bedwarsAPI = SkiHabilidades.getBedwarsAPI();
    }

    @Override
    public String getNome() { return "Toque Ígneo"; }
    @Override
    public int getMaxLevel() { return 3; }
    @Override
    public TipoDeHabilidade getTipo() { return TipoDeHabilidade.INDIVIDUAL; }

    @Override
    public String[] getDescricao() {
        return new String[]{
                ChatColor.GRAY + "Suas mãos ardem com poder",
                ChatColor.GRAY + "elemental. Seus ataques têm uma",
                ChatColor.GRAY + "chance de colocar seus inimigos",
                ChatColor.GRAY + "em chamas."
        };
    }

    private int getChancePorNivel(int nivel) {
        switch (nivel) {
            case 1: return 10;
            case 2: return 15;
            case 3: return 20;
            default: return 0;
        }
    }

    @Override
    public ItemStack getItemIcone(Player player, String grupo) {
        ItemStack item = new ItemStack(Material.FLINT_AND_STEEL);
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
    public String getPermissao() { return "skihabilidades.habilidade.toqueigneo"; }
    @Override
    public int getCustoDePontos() { return 2; }
    @Override
    public double getCustoDeCoins(int paraNivel) {
        switch (paraNivel) {
            case 1: return 4000;
            case 2: return 5500;
            case 3: return 7000;
            default: return Double.MAX_VALUE;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        Player atacante = (Player) event.getDamager();
        Player vitima = (Player) event.getEntity();
        IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(atacante);
        if (arena == null) return;
        if (arena.getTeam(atacante) != null && arena.getTeam(atacante).isMember(vitima)) return;

        String grupo = arena.getGroup();
        PlayerData dados = plugin.getPlayerSkillManager().carregarDados(atacante);
        if (!dados.isHabilidadeAtiva(grupo, getNome())) return;

        int nivel = dados.getNivelHabilidade(grupo, getNome());
        if (nivel == 0) return;

        double chance = getChancePorNivel(nivel) / 100.0;
        if (new Random().nextDouble() <= chance) {
            vitima.setFireTicks(40);
            atacante.playSound(atacante.getLocation(), Sound.FIRE_IGNITE, 0.7f, 1.2f);
        }
    }
}