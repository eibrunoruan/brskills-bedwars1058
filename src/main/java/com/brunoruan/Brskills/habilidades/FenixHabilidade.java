package com.brunoruan.Brskills.habilidades;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FenixHabilidade implements Habilidade {
    private final Set<UUID> fenixJaUsada = new HashSet<>();
    private final Set<UUID> jogadoresRevivendoComFenix = new HashSet<>();
    private final SkiHabilidades plugin;
    private final BedWars bedwarsAPI;

    public FenixHabilidade(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.bedwarsAPI = SkiHabilidades.getBedwarsAPI();
    }

    @Override
    public String getNome() { return "Fênix"; }

    @Override
    public int getMaxLevel() { return 1; }

    @Override
    public TipoDeHabilidade getTipo() { return TipoDeHabilidade.INDIVIDUAL; }

    @Override
    public String[] getDescricao() {
        return new String[]{
                ChatColor.GRAY + "Renasça das cinzas! Ao sofrer",
                ChatColor.GRAY + "uma morte final, esta habilidade",
                ChatColor.GRAY + "te dá uma segunda chance em batalha."
        };
    }

    @Override
    public ItemStack getItemIcone(Player player, String grupo) {
        ItemStack item = new ItemStack(Material.BLAZE_POWDER);
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
    public String getPermissao() { return "skihabilidades.habilidade.fenix"; }

    @Override
    public int getCustoDePontos() { return 3; }

    @Override
    public double getCustoDeCoins(int paraNivel) { return 5000; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(player);
        if (arena == null) return;

        if (player.getHealth() - event.getFinalDamage() <= 0) {
            String grupo = arena.getGroup();
            boolean temHabilidadeAtiva = plugin.getPlayerSkillManager().carregarDados(player).isHabilidadeAtiva(grupo, getNome());
            boolean camaQuebrada = arena.getTeam(player) != null && arena.getTeam(player).isBedDestroyed();

            if (temHabilidadeAtiva && camaQuebrada && !fenixJaUsada.contains(player.getUniqueId())) {
                event.setCancelled(true);
                fenixJaUsada.add(player.getUniqueId());
                jogadoresRevivendoComFenix.add(player.getUniqueId());
                player.getWorld().playEffect(player.getLocation(), org.bukkit.Effect.STEP_SOUND, Material.REDSTONE_WIRE);
                arena.startReSpawnSession(player, 5);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerReSpawnEvent event) {
        Player player = event.getPlayer();
        if (jogadoresRevivendoComFenix.contains(player.getUniqueId())) {
            jogadoresRevivendoComFenix.remove(player.getUniqueId());
            IArena arena = event.getArena();
            if (arena == null) return;
            String mensagem = ChatColor.translateAlternateColorCodes('&', "&6[Fenix] &eO jogador " + player.getName() + " renasceu como uma Fenix!");
            arena.getPlayers().forEach(p -> p.sendMessage(mensagem));
            arena.getSpectators().forEach(p -> p.sendMessage(mensagem));
        }
    }

    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        event.getArena().getPlayers().forEach(player -> {
            fenixJaUsada.remove(player.getUniqueId());
            jogadoresRevivendoComFenix.remove(player.getUniqueId());
        });
    }
}