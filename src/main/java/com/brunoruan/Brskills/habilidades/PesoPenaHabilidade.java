package com.brunoruan.Brskills.habilidades;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.brunoruan.Brskills.SkiHabilidades;
import com.brunoruan.Brskills.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import java.util.List;

public class PesoPenaHabilidade implements Habilidade {
    private final SkiHabilidades plugin;
    private final BedWars bedwarsAPI;

    public PesoPenaHabilidade(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.bedwarsAPI = SkiHabilidades.getBedwarsAPI();
    }

    @Override
    public String getNome() { return "Peso Pena"; }
    @Override
    public int getMaxLevel() { return 3; }
    @Override
    public TipoDeHabilidade getTipo() { return TipoDeHabilidade.INDIVIDUAL; }

    @Override
    public String[] getDescricao() {
        return new String[]{
                ChatColor.GRAY + "A gravidade é apenas uma sugestão.",
                ChatColor.GRAY + "Seus movimentos ágeis reduzem",
                ChatColor.GRAY + "drasticamente o dano que você",
                ChatColor.GRAY + "sofre de quedas."
        };
    }

    private int getReducaoPorNivel(int nivel) {
        switch (nivel) {
            case 1: return 50;
            case 2: return 75;
            case 3: return 100;
            default: return 0;
        }
    }

    @Override
    public ItemStack getItemIcone(Player player, String grupo) {
        ItemStack item = new ItemStack(Material.IRON_BOOTS);
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
    public String getPermissao() { return "skihabilidades.habilidade.pesopena"; }
    @Override
    public int getCustoDePontos() { return 1; }
    @Override
    public double getCustoDeCoins(int paraNivel) {
        switch (paraNivel) {
            case 1: return 1000;
            case 2: return 2000;
            case 3: return 3000;
            default: return Double.MAX_VALUE;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(player);
        if (arena == null) return;

        String grupo = arena.getGroup();
        PlayerData dados = plugin.getPlayerSkillManager().carregarDados(player);
        if (!dados.isHabilidadeAtiva(grupo, getNome())) return;

        int nivel = dados.getNivelHabilidade(grupo, getNome());
        if (nivel == 0) return;

        double reducaoPercentual = getReducaoPorNivel(nivel) / 100.0;
        double danoOriginal = event.getDamage();
        double danoReduzido = danoOriginal * reducaoPercentual;
        event.setDamage(danoOriginal - danoReduzido);
    }
}