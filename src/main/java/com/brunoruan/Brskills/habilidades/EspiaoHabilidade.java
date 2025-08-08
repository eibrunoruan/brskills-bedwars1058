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
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EspiaoHabilidade implements Habilidade {
    private final SkiHabilidades plugin;
    private final BedWars bedwarsAPI;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, BukkitTask> tarefasAgendadas = new HashMap<>();

    public EspiaoHabilidade(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.bedwarsAPI = SkiHabilidades.getBedwarsAPI();
    }

    @Override
    public String getNome() { return "Espião"; }
    @Override
    public int getMaxLevel() { return 1; }
    @Override
    public TipoDeHabilidade getTipo() { return TipoDeHabilidade.INDIVIDUAL; }

    @Override
    public String[] getDescricao() {
        return new String[]{
                ChatColor.GRAY + "Torne-se uma sombra. Fique agachado",
                ChatColor.GRAY + "por 10 segundos para se camuflar",
                ChatColor.GRAY + "no ambiente, tornando-se invisível."
        };
    }

    @Override
    public ItemStack getItemIcone(Player player, String grupo) {
        ItemStack item = new ItemStack(Material.EYE_OF_ENDER);
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
    public String getPermissao() { return "skihabilidades.habilidade.espiao"; }
    @Override
    public int getCustoDePontos() { return 3; }
    @Override
    public double getCustoDeCoins(int paraNivel) { return 6000; }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(player);
        if (arena == null) return;

        String grupo = arena.getGroup();
        PlayerData dados = plugin.getPlayerSkillManager().carregarDados(player);
        if (!dados.isHabilidadeAtiva(grupo, getNome())) return;

        String prefixo = ChatColor.YELLOW + "[Espião] ";
        if (event.isSneaking()) {
            if (cooldowns.containsKey(playerUuid) && System.currentTimeMillis() < cooldowns.get(playerUuid)) {
                long tempoRestante = cooldowns.get(playerUuid) - System.currentTimeMillis();
                long segundosRestantes = TimeUnit.MILLISECONDS.toSeconds(tempoRestante) + 1;
                player.sendMessage(prefixo + ChatColor.RED + "Você precisa esperar " + segundosRestantes + " segundos para usar novamente.");
                return;
            }
            BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                tarefasAgendadas.remove(playerUuid);
                if (cooldowns.containsKey(playerUuid) && System.currentTimeMillis() < cooldowns.get(playerUuid)) {
                    return;
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
                cooldowns.put(playerUuid, System.currentTimeMillis() + 60000L);
                player.sendMessage(prefixo + ChatColor.GREEN + "Você ficou invisível por 10 segundos!");
                player.playSound(player.getLocation(), Sound.FIZZ, 1.0f, 1.0f);
            }, 200L);
            tarefasAgendadas.put(playerUuid, task);
        } else {
            if (tarefasAgendadas.containsKey(playerUuid)) {
                tarefasAgendadas.get(playerUuid).cancel();
                tarefasAgendadas.remove(playerUuid);
            }
        }
    }
}