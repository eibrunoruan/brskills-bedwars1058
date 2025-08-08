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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UltimaChanceHabilidade implements Habilidade {
    private final SkiHabilidades plugin;
    private final BedWars bedwarsAPI;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public UltimaChanceHabilidade(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.bedwarsAPI = SkiHabilidades.getBedwarsAPI();
    }

    @Override
    public String getNome() { return "Última Chance"; }
    @Override
    public int getMaxLevel() { return 4; }
    @Override
    public TipoDeHabilidade getTipo() { return TipoDeHabilidade.INDIVIDUAL; }

    @Override
    public String[] getDescricao() {
        return new String[]{
                ChatColor.GRAY + "Lute até o fim! Quando sua vida",
                ChatColor.GRAY + "está por um fio, esta habilidade",
                ChatColor.GRAY + "te dá uma explosão de vitalidade."
        };
    }

    @Override
    public ItemStack getItemIcone(Player player, String grupo) {
        ItemStack item = new ItemStack(Material.TORCH);
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
    public String getPermissao() { return "skihabilidades.habilidade.ultimachance"; }
    @Override
    public int getCustoDePontos() { return 3; }
    @Override
    public double getCustoDeCoins(int paraNivel) {
        switch (paraNivel) {
            case 1: return 2000;
            case 2: return 3500;
            case 3: return 5000;
            case 4: return 7500;
            default: return Double.MAX_VALUE;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(player);
        if (arena == null) return;

        String grupo = arena.getGroup();
        PlayerData dados = plugin.getPlayerSkillManager().carregarDados(player);
        if (!dados.isHabilidadeAtiva(grupo, getNome()) || (cooldowns.containsKey(player.getUniqueId()) && System.currentTimeMillis() < cooldowns.get(player.getUniqueId()))) {
            return;
        }
        double vidaInicial = player.getHealth();
        double danoFinal = event.getFinalDamage();
        double vidaFinal = vidaInicial - danoFinal;
        if (vidaInicial > 2.0 && vidaFinal <= 2.0 && vidaFinal > 0) {
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 20000L);
            int nivel = dados.getNivelHabilidade(grupo, getNome());
            double vidaParaRecuperar = 1.0 + nivel;
            double vidaMaximaAtual = player.getMaxHealth();
            double novaVida = player.getHealth() + vidaParaRecuperar;
            player.setHealth(Math.min(novaVida, vidaMaximaAtual));
            player.sendMessage(ChatColor.YELLOW + "[Última Chance] " + ChatColor.GREEN + "Sua habilidade foi ativada, você recuperou vida!");
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.5f);
        }
    }
}