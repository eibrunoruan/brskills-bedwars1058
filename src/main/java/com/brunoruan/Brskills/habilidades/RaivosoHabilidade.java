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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

public class RaivosoHabilidade implements Habilidade {
    private final SkiHabilidades plugin;
    private final BedWars bedwarsAPI;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public RaivosoHabilidade(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.bedwarsAPI = SkiHabilidades.getBedwarsAPI();
    }

    @Override
    public String getNome() { return "Raivoso"; }
    @Override
    public int getMaxLevel() { return 1; }
    @Override
    public TipoDeHabilidade getTipo() { return TipoDeHabilidade.INDIVIDUAL; }

    @Override
    public String[] getDescricao() {
        return new String[]{
                ChatColor.GRAY + "A dor te fortalece. Ao entrar em",
                ChatColor.GRAY + "combate, sua adrenalina dispara,",
                ChatColor.GRAY + "concedendo-lhe Força por 3s."
        };
    }

    @Override
    public ItemStack getItemIcone(Player player, String grupo) {
        ItemStack item = new ItemStack(Material.REDSTONE);
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
    public String getPermissao() { return "skihabilidades.habilidade.raivoso"; }
    @Override
    public int getCustoDePontos() { return 2; }
    @Override
    public double getCustoDeCoins(int paraNivel) { return 4000; }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerCombat(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        Player atacante = (Player) event.getDamager();
        IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(atacante);
        if (arena == null) return;

        String grupo = arena.getGroup();
        boolean temHabilidadeAtiva = plugin.getPlayerSkillManager().carregarDados(atacante).isHabilidadeAtiva(grupo, getNome());
        if (!temHabilidadeAtiva) return;

        if (cooldowns.containsKey(atacante.getUniqueId())) {
            if (System.currentTimeMillis() < cooldowns.get(atacante.getUniqueId())) {
                return;
            }
        }
        atacante.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60, 0));
        long cooldownTime = System.currentTimeMillis() + 15000L;
        cooldowns.put(atacante.getUniqueId(), cooldownTime);
        atacante.sendMessage(ChatColor.RED + "[Raivoso] " + ChatColor.GRAY + "Sua fúria foi despertada! Você recebeu Força I!");
    }
}