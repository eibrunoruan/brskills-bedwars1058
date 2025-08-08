package com.brunoruan.Brskills.habilidades;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.brunoruan.Brskills.SkiHabilidades;
import com.brunoruan.Brskills.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ConstrutorHabilidade implements Habilidade {
    private final SkiHabilidades plugin;
    private final BedWars bedwarsAPI;
    private final Random random = new Random();

    public ConstrutorHabilidade(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.bedwarsAPI = SkiHabilidades.getBedwarsAPI();
    }

    @Override
    public String getNome() { return "Construtor"; }
    @Override
    public int getMaxLevel() { return 3; }
    @Override
    public TipoDeHabilidade getTipo() { return TipoDeHabilidade.INDIVIDUAL; }

    @Override
    public String[] getDescricao() {
        return new String[]{
                ChatColor.GRAY + "Um mestre da eficiência. Sua",
                ChatColor.GRAY + "experiência em construção permite",
                ChatColor.GRAY + "ter uma chance de não gastar",
                ChatColor.GRAY + "blocos ao colocá-los."
        };
    }

    private int getChancePorNivel(int nivel) {
        switch (nivel) {
            case 1: return 10;
            case 2: return 25;
            case 3: return 35;
            default: return 0;
        }
    }

    @Override
    public ItemStack getItemIcone(Player player, String grupo) {
        ItemStack item = new ItemStack(Material.BRICK);
        ItemMeta meta = item.getItemMeta();
        PlayerData dados = plugin.getPlayerSkillManager().carregarDados(player);
        int nivelAtual = dados.getNivelHabilidade(grupo, getNome());

        ChatColor corDoNome = (nivelAtual > 0) ? ChatColor.GREEN : ChatColor.YELLOW;
        meta.setDisplayName(corDoNome + getNome());

        List<String> lore = new ArrayList<>(Arrays.asList(getDescricao()));
        lore.add("");
        if (nivelAtual > 0) {
            lore.add(ChatColor.YELLOW + "Adquirido!");
            if (nivelAtual < getMaxLevel()) {
                lore.add(ChatColor.GREEN + "Clique para evoluir!");
            } else {
                lore.add(ChatColor.GOLD + "Nível máximo atingido.");
            }
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
    public String getPermissao() { return "skihabilidades.habilidade.construtor"; }
    @Override
    public int getCustoDePontos() { return 2; }
    @Override
    public double getCustoDeCoins(int paraNivel) {
        switch (paraNivel) {
            case 1: return 2500;
            case 2: return 4000;
            case 3: return 5500;
            default: return Double.MAX_VALUE;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(player);
        if (arena == null) return;

        String grupo = arena.getGroup();
        PlayerData dados = plugin.getPlayerSkillManager().carregarDados(player);
        if (!dados.isHabilidadeAtiva(grupo, getNome())) return;

        int nivel = dados.getNivelHabilidade(grupo, getNome());
        if (nivel == 0) return;
        double chance = getChancePorNivel(nivel) / 100.0;
        if (random.nextDouble() <= chance) {
            ItemStack itemNaMao = event.getItemInHand();
            final ItemStack itemParaDevolver = new ItemStack(itemNaMao.getType(), 1, itemNaMao.getDurability());
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.getInventory().addItem(itemParaDevolver);
                player.updateInventory();
            }, 1L);
            player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.5f, 1.5f);
        }
    }
}