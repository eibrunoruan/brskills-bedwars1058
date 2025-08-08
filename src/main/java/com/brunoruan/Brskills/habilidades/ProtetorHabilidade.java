package com.brunoruan.Brskills.habilidades;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.brunoruan.Brskills.SkiHabilidades;
import com.brunoruan.Brskills.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProtetorHabilidade implements Habilidade {
    private final SkiHabilidades plugin;
    private final BedWars bedwarsAPI;
    private final Map<ITeam, Long> cooldowns = new HashMap<>();

    public ProtetorHabilidade(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.bedwarsAPI = SkiHabilidades.getBedwarsAPI();
    }

    @Override
    public String getNome() { return "Protetor"; }
    @Override
    public int getMaxLevel() { return 1; }
    @Override
    public TipoDeHabilidade getTipo() { return TipoDeHabilidade.TIME; }

    @Override
    public String[] getDescricao() {
        return new String[]{
                ChatColor.GRAY + "Sua cama é sua fortaleza. A primeira vez",
                ChatColor.GRAY + "que um inimigo tentar quebrá-la, ele será",
                ChatColor.GRAY + "repelido e a cama ficará invulnerável",
                ChatColor.GRAY + "por 3 segundos."
        };
    }

    @Override
    public ItemStack getItemIcone(Player player, String grupo) {
        ItemStack item = new ItemStack(Material.GOLD_CHESTPLATE);
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
    public String getPermissao() { return "skihabilidades.habilidade.protetor"; }
    @Override
    public int getCustoDePontos() { return 4; }
    @Override
    public double getCustoDeCoins(int paraNivel) { return 8000; }

    @EventHandler(priority = EventPriority.LOW)
    public void onBedBreakAttempt(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player quebrando = event.getPlayer();
        Block bloco = event.getBlock();
        IArena arena = bedwarsAPI.getArenaUtil().getArenaByPlayer(quebrando);
        if (arena == null || !arena.isTeamBed(bloco.getLocation())) return;
        ITeam timeDefensor = arena.getBedsTeam(bloco.getLocation());
        ITeam timeAtacante = arena.getTeam(quebrando);
        if (timeDefensor == null || timeDefensor.equals(timeAtacante)) return;
        if (cooldowns.containsKey(timeDefensor) && System.currentTimeMillis() < cooldowns.get(timeDefensor)) {
            event.setCancelled(true);
            return;
        }
        boolean habilidadeAtivaNoTime = plugin.getPlayerSkillManager().isHabilidadeDeTimeAtiva(timeDefensor, getNome());
        if (habilidadeAtivaNoTime) {
            if (cooldowns.containsKey(timeDefensor)) {
                return;
            }
            event.setCancelled(true);
            cooldowns.put(timeDefensor, System.currentTimeMillis() + 3000L);
            Vector direcao = quebrando.getLocation().toVector().subtract(bloco.getLocation().toVector()).normalize();
            direcao.setY(0.5);
            quebrando.setVelocity(direcao.multiply(1.5));
            String nomeDoTime = timeDefensor.getColor().chat() + timeDefensor.getName();
            String msgGlobal = ChatColor.AQUA + "[Protetor] " + ChatColor.GREEN + "A cama da equipe " + nomeDoTime + " foi protegida!";
            arena.getPlayers().forEach(p -> p.sendMessage(msgGlobal));
            arena.getSpectators().forEach(p -> p.sendMessage(msgGlobal));
            quebrando.sendMessage(ChatColor.AQUA + "[Protetor] " + ChatColor.RED + "Você foi repelido pela proteção da cama!");
            enviarMensagensDeCooldown(quebrando, timeDefensor);
        }
    }

    private void enviarMensagensDeCooldown(Player atacante, ITeam timeDefensor) {
        String prefixo = ChatColor.translateAlternateColorCodes('&', "&e[Protetor] ");
        String msgDefensorBase = prefixo + ChatColor.YELLOW + "A " + ChatColor.RED + "sua cama" + ChatColor.YELLOW + " ficará vulnerável em ";
        String msgAtacanteBase = prefixo + ChatColor.YELLOW + "A cama " + ChatColor.RED + "inimiga " + ChatColor.YELLOW + "ficará vulnerável em ";
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String msgDefensor = msgDefensorBase + "2 segundos.";
            String msgAtacante = msgAtacanteBase + "2 segundos.";
            if (atacante.isOnline()) atacante.sendMessage(msgAtacante);
            timeDefensor.getMembers().forEach(membro -> {
                if (membro.isOnline()) membro.sendMessage(msgDefensor);
            });
        }, 20L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String msgDefensor = msgDefensorBase + "1 segundo.";
            String msgAtacante = msgAtacanteBase + "1 segundo.";
            if (atacante.isOnline()) atacante.sendMessage(msgAtacante);
            timeDefensor.getMembers().forEach(membro -> {
                if (membro.isOnline()) membro.sendMessage(msgDefensor);
            });
        }, 40L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String msgDefensorFinal = prefixo + ChatColor.YELLOW + "Sua cama perdeu a proteção!";
            String msgAtacanteFinal = prefixo + ChatColor.YELLOW + "A cama inimiga perdeu a proteção!";
            if (atacante.isOnline()) atacante.sendMessage(msgAtacanteFinal);
            timeDefensor.getMembers().forEach(membro -> {
                if (membro.isOnline()) membro.sendMessage(msgDefensorFinal);
            });
        }, 60L);
    }
}