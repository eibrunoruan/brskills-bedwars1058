package com.brunoruan.Brskills.habilidades;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.brunoruan.Brskills.SkiHabilidades;
import com.brunoruan.Brskills.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RevelacaoHabilidade implements Habilidade {
    private final SkiHabilidades plugin;
    private final BedWars bedwarsAPI;
    private final double RAIO_DE_DETECCAO = 10.0;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public RevelacaoHabilidade(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.bedwarsAPI = SkiHabilidades.getBedwarsAPI();
        iniciarRadarDeInvisibilidade();
    }

    @Override
    public String getNome() { return "Revelação"; }
    @Override
    public int getMaxLevel() { return 1; }
    @Override
    public TipoDeHabilidade getTipo() { return TipoDeHabilidade.INDIVIDUAL; }

    @Override
    public String[] getDescricao() {
        return new String[]{
                ChatColor.GRAY + "Ninguém se esconde de você.",
                ChatColor.GRAY + "Uma aura perceptiva ao seu redor",
                ChatColor.GRAY + "revela inimigos invisíveis que",
                ChatColor.GRAY + "se atrevem a chegar perto demais."
        };
    }

    @Override
    public ItemStack getItemIcone(Player player, String grupo) {
        ItemStack item = new ItemStack(Material.COMPASS);
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
    public String getPermissao() { return "skihabilidades.habilidade.revelacao"; }
    @Override
    public int getCustoDePontos() { return 2; }
    @Override
    public double getCustoDeCoins(int paraNivel) { return 3500; }

    private void iniciarRadarDeInvisibilidade() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player jogadorComSkill : Bukkit.getOnlinePlayers()) {
                IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(jogadorComSkill);
                if (arena == null) continue;

                String grupo = arena.getGroup();
                PlayerData dados = plugin.getPlayerSkillManager().carregarDados(jogadorComSkill);
                if (!dados.isHabilidadeAtiva(grupo, getNome())) continue;

                if (cooldowns.containsKey(jogadorComSkill.getUniqueId()) && System.currentTimeMillis() < cooldowns.get(jogadorComSkill.getUniqueId())) {
                    continue;
                }
                List<Entity> entidadesProximas = jogadorComSkill.getNearbyEntities(RAIO_DE_DETECCAO, RAIO_DE_DETECCAO, RAIO_DE_DETECCAO);
                for (Entity entidade : entidadesProximas) {
                    if (entidade instanceof Player) {
                        Player jogadorProximo = (Player) entidade;
                        if (jogadorProximo.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                            ITeam timeDoAlvo = arena.getTeam(jogadorProximo);
                            ITeam timeDoDonoDaSkill = arena.getTeam(jogadorComSkill);
                            if (timeDoAlvo != null && !timeDoAlvo.equals(timeDoDonoDaSkill)) {
                                jogadorProximo.removePotionEffect(PotionEffectType.INVISIBILITY);
                                cooldowns.put(jogadorComSkill.getUniqueId(), System.currentTimeMillis() + 60000L);
                                String prefixo = ChatColor.YELLOW + "[Revelação] ";
                                jogadorProximo.sendMessage(prefixo + ChatColor.RED + "Sua invisibilidade foi quebrada por um inimigo próximo!");
                                jogadorComSkill.sendMessage(prefixo + ChatColor.GREEN + "Você revelou um jogador inimigo invisível!");
                                jogadorComSkill.sendMessage(prefixo + ChatColor.RED + "Sua habilidade entrou em tempo de recarga por 60 segundos.");
                                jogadorProximo.playSound(jogadorProximo.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 0.5f);
                                break;
                            }
                        }
                    }
                }
            }
        }, 0L, 10L);
    }
}