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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HadesHabilidade implements Habilidade {
    private final SkiHabilidades plugin;
    private final BedWars bedwarsAPI;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public HadesHabilidade(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.bedwarsAPI = SkiHabilidades.getBedwarsAPI();
    }

    @Override
    public String getNome() { return "Hades"; }
    @Override
    public int getMaxLevel() { return 1; }
    @Override
    public TipoDeHabilidade getTipo() { return TipoDeHabilidade.INDIVIDUAL; }

    @Override
    public String[] getDescricao() {
        return new String[]{
                ChatColor.GRAY + "O submundo te fortalece. Estar",
                ChatColor.GRAY + "em chamas não te enfraquece, pelo",
                ChatColor.GRAY + "contrário, desperta uma fúria",
                ChatColor.GRAY + "infernal que aumenta seu dano."
        };
    }

    @Override
    public ItemStack getItemIcone(Player player, String grupo) {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
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
    public String getPermissao() { return "skihabilidades.habilidade.hades"; }
    @Override
    public int getCustoDePontos() { return 3; }
    @Override
    public double getCustoDeCoins(int paraNivel) { return 5000; }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerTakeFireDamage(EntityDamageEvent event) {
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.FIRE && cause != EntityDamageEvent.DamageCause.FIRE_TICK && cause != EntityDamageEvent.DamageCause.LAVA) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(player);
        if (arena == null) return;

        String grupo = arena.getGroup();
        PlayerData dados = plugin.getPlayerSkillManager().carregarDados(player);
        if (!dados.isHabilidadeAtiva(grupo, getNome())) return;

        if (cooldowns.containsKey(player.getUniqueId()) && System.currentTimeMillis() < cooldowns.get(player.getUniqueId())) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, 0));
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 60000L);
        player.sendMessage(ChatColor.GOLD + "[Hades] " + ChatColor.RED + "O fogo alimenta sua fúria! Você recebeu Força I!");
        player.playSound(player.getLocation(), Sound.GHAST_FIREBALL, 0.8f, 1.0f);
    }
}