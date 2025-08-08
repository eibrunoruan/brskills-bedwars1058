package com.brunoruan.Brskills.gui;

import com.andrei1058.bedwars.api.BedWars;
import com.brunoruan.Brskills.SkiHabilidades;
import com.brunoruan.Brskills.data.PlayerData;
import com.brunoruan.Brskills.habilidades.Habilidade;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GUIManager {
    private final SkiHabilidades plugin;
    private final BedWars bedwarsAPI;
    public final String TITULO_SELECAO_MODO = ChatColor.DARK_GRAY + "Habilidades - Selecionar Modo";
    public final String TITULO_MENU_HABILIDADES = "Habilidades";
    public final String TITULO_MENU_UPGRADE_PREFIX = ChatColor.DARK_GRAY + "Evoluir: ";
    public final String TITULO_MENU_COMPRA_PREFIX = ChatColor.DARK_GRAY + "Comprar: ";

    public GUIManager(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.bedwarsAPI = SkiHabilidades.getBedwarsAPI();
    }

    public void abrirMenuSelecaoModo(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TITULO_SELECAO_MODO);

        gui.setItem(11, criarIconeDeModo(player, "Solo", ChatColor.GREEN));
        gui.setItem(13, criarIconeDeModo(player, "Duplas", ChatColor.AQUA));
        gui.setItem(15, criarIconeDeModo(player, "Quartetos", ChatColor.RED));

        player.openInventory(gui);
    }

    private ItemStack criarIconeDeModo(Player player, String grupo, ChatColor cor) {
        ItemStack item = new ItemStack(Material.EXP_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(cor + grupo);

        int[] progresso = plugin.getPlayerSkillManager().getProgressoHabilidades(player, grupo);
        int desbloqueados = progresso[0];
        int total = progresso[1];
        double porcentagem = (total == 0) ? 0.0 : ((double) desbloqueados / total) * 100.0;
        DecimalFormat df = new DecimalFormat("#");
        ChatColor corPorcentagem = (desbloqueados == total && total > 0) ? ChatColor.GREEN : ChatColor.YELLOW;

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Clique para comprar ou");
        lore.add(ChatColor.GRAY + "evoluir uma habilidade.");
        lore.add("");
        lore.add(ChatColor.WHITE + "Desbloqueados: " + ChatColor.GRAY + desbloqueados + "/" + total + " " + corPorcentagem + "(" + df.format(porcentagem) + "%)");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public void abrirMenuHabilidades(Player player, String grupo) {
        String tituloFormatado = ChatColor.translateAlternateColorCodes('&', "&aHabilidades &f(" + grupo + ")");
        Inventory gui = Bukkit.createInventory(null, 54, tituloFormatado);

        for (Habilidade habilidade : plugin.getHabilidadeManager().getHabilidadesRegistradas()) {
            switch (habilidade.getNome()) {
                case "Construtor": gui.setItem(11, habilidade.getItemIcone(player, grupo)); break;
                case "Toque Ígneo": gui.setItem(12, habilidade.getItemIcone(player, grupo)); break;
                case "Raivoso": gui.setItem(13, habilidade.getItemIcone(player, grupo)); break;
                case "Protetor": gui.setItem(14, habilidade.getItemIcone(player, grupo)); break;
                case "Vida Extra": gui.setItem(15, habilidade.getItemIcone(player, grupo)); break;
                case "Fênix": gui.setItem(20, habilidade.getItemIcone(player, grupo)); break;
                case "Última Chance": gui.setItem(21, habilidade.getItemIcone(player, grupo)); break;
                case "Peso Pena": gui.setItem(22, habilidade.getItemIcone(player, grupo)); break;
                case "Espião": gui.setItem(23, habilidade.getItemIcone(player, grupo)); break;
                case "Ressarcimento": gui.setItem(24, habilidade.getItemIcone(player, grupo)); break;
                case "Vampirismo": gui.setItem(30, habilidade.getItemIcone(player, grupo)); break;
                case "Revelação": gui.setItem(31, habilidade.getItemIcone(player, grupo)); break;
                case "Hades": gui.setItem(32, habilidade.getItemIcone(player, grupo)); break;
            }
        }

        int pontosUsados = plugin.getPlayerSkillManager().getPontosUsados(player, grupo);
        int maxPontos = plugin.getPlayerSkillManager().getMaxPontos(player);
        ItemStack pontosItem = new ItemStack(Material.BOOK);
        ItemMeta pontosMeta = pontosItem.getItemMeta();
        pontosMeta.setDisplayName(ChatColor.AQUA + "Seus Pontos de Habilidade");
        pontosMeta.setLore(Arrays.asList(
                "",
                ChatColor.GRAY + "Você está usando " + ChatColor.YELLOW + pontosUsados + ChatColor.GRAY + " de " + ChatColor.GREEN + maxPontos,
                ChatColor.GRAY + "pontos disponíveis para o modo " + ChatColor.AQUA + grupo + ChatColor.GRAY + "."
        ));
        pontosItem.setItemMeta(pontosMeta);
        gui.setItem(49, pontosItem);

        gui.setItem(45, criarItemVoltar());
        player.openInventory(gui);
    }

    public void abrirMenuConfirmacaoCompra(Player player, Habilidade habilidade, String grupo) {
        String titulo = TITULO_MENU_COMPRA_PREFIX + habilidade.getNome() + " (" + grupo + ")";
        Inventory gui = Bukkit.createInventory(null, 27, titulo);
        double custo = habilidade.getCustoDeCoins(1);
        ItemStack confirmar = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 5);
        ItemMeta confirmarMeta = confirmar.getItemMeta();
        confirmarMeta.setDisplayName(ChatColor.GREEN + "Confirmar Compra");
        confirmarMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Modo: " + ChatColor.AQUA + grupo,
                ChatColor.GRAY + "Custo: " + ChatColor.YELLOW + custo + " coins"
        ));
        confirmar.setItemMeta(confirmarMeta);
        ItemStack cancelar = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 14);
        ItemMeta cancelarMeta = cancelar.getItemMeta();
        cancelarMeta.setDisplayName(ChatColor.RED + "Cancelar");
        cancelarMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Clique para voltar ao menu de habilidades."));
        cancelar.setItemMeta(cancelarMeta);
        gui.setItem(11, confirmar);
        gui.setItem(13, habilidade.getItemIcone(player, grupo));
        gui.setItem(15, cancelar);
        gui.setItem(18, criarItemVoltar());
        player.openInventory(gui);
    }

    public void abrirMenuUpgrade(Player player, Habilidade habilidade, String grupo) {
        String titulo = TITULO_MENU_UPGRADE_PREFIX + habilidade.getNome() + " (" + grupo + ")";
        Inventory gui = Bukkit.createInventory(null, 36, titulo);
        PlayerData dados = plugin.getPlayerSkillManager().carregarDados(player);
        int nivelAtual = dados.getNivelHabilidade(grupo, habilidade.getNome());
        int maxLevel = habilidade.getMaxLevel();
        int startSlot = 13 - (maxLevel / 2);
        for (int i = 1; i <= maxLevel; i++) {
            ItemStack painel;
            ItemMeta painelMeta;
            int slotAtual = startSlot + i - 1;
            if (i <= nivelAtual) {
                painel = new ItemStack(Material.STAINED_GLASS_PANE, i, (byte) 5);
                painelMeta = painel.getItemMeta();
                painelMeta.setDisplayName(ChatColor.GREEN + "Nível " + i + " (Adquirido)");
                // CORREÇÃO AQUI: removido o argumento (i)
                painelMeta.setLore(Arrays.asList(habilidade.getDescricao()));
            } else if (i == nivelAtual + 1) {
                painel = new ItemStack(Material.STAINED_GLASS_PANE, i, (byte) 4);
                painelMeta = painel.getItemMeta();
                painelMeta.setDisplayName(ChatColor.YELLOW + "Evoluir para o Nível " + i);
                // CORREÇÃO AQUI: removido o argumento (i)
                List<String> lore = new ArrayList<>(Arrays.asList(habilidade.getDescricao()));
                lore.add("");
                lore.add(ChatColor.GOLD + "Custo: " + ChatColor.WHITE + habilidade.getCustoDeCoins(i) + " coins");
                lore.add("");
                lore.add(ChatColor.GREEN + "Clique para evoluir!");
                painelMeta.setLore(lore);
            } else {
                painel = new ItemStack(Material.STAINED_GLASS_PANE, i, (byte) 14);
                painelMeta = painel.getItemMeta();
                painelMeta.setDisplayName(ChatColor.RED + "Nível " + i + " (Bloqueado)");
                painelMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Você precisa adquirir o nível anterior."));
            }
            painel.setItemMeta(painelMeta);
            gui.setItem(slotAtual, painel);
        }
        ItemStack toggleButton;
        if (dados.isHabilidadeAtiva(grupo, habilidade.getNome())) {
            toggleButton = new ItemStack(Material.INK_SACK, 1, (byte) 10);
            ItemMeta meta = toggleButton.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Habilidade Ativa");
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Clique para desativar"));
            toggleButton.setItemMeta(meta);
        } else {
            toggleButton = new ItemStack(Material.INK_SACK, 1, (byte) 8);
            ItemMeta meta = toggleButton.getItemMeta();
            meta.setDisplayName(ChatColor.GRAY + "Habilidade Inativa");
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Clique para ativar"));
            toggleButton.setItemMeta(meta);
        }
        gui.setItem(30, toggleButton);
        gui.setItem(32, criarItemVoltar());
        player.openInventory(gui);
    }

    private ItemStack criarItemVoltar() {
        ItemStack voltar = new ItemStack(Material.ARROW);
        ItemMeta voltarMeta = voltar.getItemMeta();
        voltarMeta.setDisplayName(ChatColor.YELLOW + "Voltar");
        voltar.setItemMeta(voltarMeta);
        return voltar;
    }
}