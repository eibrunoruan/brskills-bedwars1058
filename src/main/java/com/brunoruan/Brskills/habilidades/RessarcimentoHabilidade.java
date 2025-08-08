package com.brunoruan.Brskills.habilidades;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.brunoruan.Brskills.SkiHabilidades;
import com.brunoruan.Brskills.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class RessarcimentoHabilidade implements Habilidade {
    private final SkiHabilidades plugin;
    private final BedWars bedwarsAPI;
    private final Random random = new Random();
    private final String METADATA_KEY = "SKILL_RESSARCIMENTO_OWNER";

    public RessarcimentoHabilidade(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.bedwarsAPI = SkiHabilidades.getBedwarsAPI();
    }

    @Override
    public String getNome() { return "Ressarcimento"; }
    @Override
    public int getMaxLevel() { return 3; }
    @Override
    public TipoDeHabilidade getTipo() { return TipoDeHabilidade.INDIVIDUAL; }

    @Override
    public String[] getDescricao() {
        return new String[]{
                ChatColor.GRAY + "Sua defesa é persistente. Blocos",
                ChatColor.GRAY + "que você coloca têm uma chance de",
                ChatColor.GRAY + "reaparecerem logo após serem",
                ChatColor.GRAY + "quebrados por um inimigo."
        };
    }

    private int getChancePorNivel(int nivel) {
        switch (nivel) {
            case 1: return 25;
            case 2: return 35;
            case 3: return 45;
            default: return 0;
        }
    }

    @Override
    public ItemStack getItemIcone(Player player, String grupo) {
        ItemStack item = new ItemStack(Material.BEDROCK);
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
    public String getPermissao() { return "skihabilidades.habilidade.ressarcimento"; }
    @Override
    public int getCustoDePontos() { return 2; }
    @Override
    public double getCustoDeCoins(int paraNivel) {
        switch (paraNivel) {
            case 1: return 3000;
            case 2: return 4500;
            case 3: return 6000;
            default: return Double.MAX_VALUE;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(player);
        if (arena == null) return;

        String grupo = arena.getGroup();
        PlayerData dados = plugin.getPlayerSkillManager().carregarDados(player);
        if (dados.isHabilidadeAtiva(grupo, getNome())) {
            event.getBlock().setMetadata(METADATA_KEY, new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block bloco = event.getBlock();
        if (bloco.hasMetadata(METADATA_KEY)) {
            Player quebrador = event.getPlayer();
            IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(quebrador);
            if (arena == null) return;
            UUID donoUuid = UUID.fromString(bloco.getMetadata(METADATA_KEY).get(0).asString());
            bloco.removeMetadata(METADATA_KEY, plugin);
            if (quebrador.getUniqueId().equals(donoUuid)) return;
            Player donoDoBloco = Bukkit.getPlayer(donoUuid);
            if (donoDoBloco == null || !donoDoBloco.isOnline()) return;

            String grupo = arena.getGroup();
            PlayerData dadosDono = plugin.getPlayerSkillManager().carregarDados(donoDoBloco);
            int nivel = dadosDono.getNivelHabilidade(grupo, getNome());
            if (nivel == 0) return;
            double chance = getChancePorNivel(nivel) / 100.0;
            if (random.nextDouble() <= chance) {
                Material tipo = bloco.getType();
                byte data = bloco.getData();
                final IArena finalArena = arena;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    bloco.setType(tipo);
                    bloco.setData(data);
                    finalArena.addPlacedBlock(bloco);
                }, 1L);
                quebrador.playSound(bloco.getLocation(), Sound.DIG_GRASS, 1.0f, 0.8f);
                donoDoBloco.playSound(donoDoBloco.getLocation(), Sound.NOTE_PLING, 1.0f, 1.5f);
            }
        }
    }
}