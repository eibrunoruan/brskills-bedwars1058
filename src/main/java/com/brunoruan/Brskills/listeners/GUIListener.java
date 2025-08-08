package com.brunoruan.Brskills.listeners;

import com.brunoruan.Brskills.SkiHabilidades;
import com.brunoruan.Brskills.data.PlayerData;
import com.brunoruan.Brskills.gui.GUIManager;
import com.brunoruan.Brskills.habilidades.Habilidade;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {
    private final SkiHabilidades plugin;

    public GUIListener(SkiHabilidades plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        String strippedTitle = ChatColor.stripColor(title); // Título sem cores
        GUIManager guiManager = plugin.getGuiManager();
        Player player = (Player) event.getWhoClicked();

        // Obtém os títulos dos menus (sem cores) para uma comparação segura
        String strippedSelecao = ChatColor.stripColor(guiManager.TITULO_SELECAO_MODO);
        String strippedHabilidades = ChatColor.stripColor(guiManager.TITULO_MENU_HABILIDADES);
        String strippedCompra = ChatColor.stripColor(guiManager.TITULO_MENU_COMPRA_PREFIX);
        String strippedUpgrade = ChatColor.stripColor(guiManager.TITULO_MENU_UPGRADE_PREFIX);

        // Verificação principal (e correta) se o clique foi em um de nossos menus
        if (!(strippedTitle.equals(strippedSelecao) ||
                strippedTitle.startsWith(strippedHabilidades) ||
                strippedTitle.startsWith(strippedCompra) ||
                strippedTitle.startsWith(strippedUpgrade))) {
            return;
        }

        event.setCancelled(true);
        ItemStack itemClicado = event.getCurrentItem();
        if (itemClicado == null || itemClicado.getType() == Material.AIR) return;

        // --- INÍCIO DO ROTEADOR DE CLIQUES ---

        // Se o clique foi no menu de SELEÇÃO DE MODO
        if (strippedTitle.equals(strippedSelecao)) {
            handleMenuSelecaoModoClick(player, itemClicado);
            return;
        }

        // Para todos os outros menus, precisamos do "grupo"
        String grupo = extrairGrupoDoTitulo(title);
        if (grupo == null) return; // Segurança caso o título não tenha o grupo

        // Se o clique foi no botão de VOLTAR
        if (itemClicado.getType() == Material.ARROW) {
            handleBotaoVoltar(player, strippedTitle, strippedHabilidades, grupo);
            return;
        }

        // Se o clique foi no menu de HABILIDADES
        if (strippedTitle.startsWith(strippedHabilidades)) {
            handleMenuHabilidadesClick(player, itemClicado, event, grupo);
        }
        // Se o clique foi no menu de COMPRA
        else if (strippedTitle.startsWith(strippedCompra)) {
            handleMenuCompraClick(player, itemClicado, title, grupo);
        }
        // Se o clique foi no menu de UPGRADE
        else if (strippedTitle.startsWith(strippedUpgrade)) {
            handleMenuUpgradeClick(player, itemClicado, title, event.getSlot(), grupo);
        }
    }

    private void handleBotaoVoltar(Player player, String strippedTitle, String strippedHabilidades, String grupo) {
        if (strippedTitle.startsWith(strippedHabilidades)) {
            plugin.getGuiManager().abrirMenuSelecaoModo(player);
        } else {
            plugin.getGuiManager().abrirMenuHabilidades(player, grupo);
        }
    }

    private void handleMenuSelecaoModoClick(Player player, ItemStack itemClicado) {
        if (!itemClicado.hasItemMeta() || itemClicado.getType() != Material.EXP_BOTTLE) return;
        String nomeItem = ChatColor.stripColor(itemClicado.getItemMeta().getDisplayName());

        if (nomeItem.equalsIgnoreCase("Solo")) {
            plugin.getGuiManager().abrirMenuHabilidades(player, "Solo");
        } else if (nomeItem.equalsIgnoreCase("Duplas")) {
            plugin.getGuiManager().abrirMenuHabilidades(player, "Duplas");
        } else if (nomeItem.equalsIgnoreCase("Quartetos")) {
            plugin.getGuiManager().abrirMenuHabilidades(player, "Quartetos");
        }
    }

    private void handleMenuHabilidadesClick(Player player, ItemStack itemClicado, InventoryClickEvent event, String grupo) {
        if (!itemClicado.hasItemMeta()) return;
        String nomeDoItem = ChatColor.stripColor(itemClicado.getItemMeta().getDisplayName().split(" \\(")[0].trim());

        for (Habilidade habilidade : plugin.getHabilidadeManager().getHabilidadesRegistradas()) {
            if (habilidade.getNome().equalsIgnoreCase(nomeDoItem)) {
                PlayerData dados = plugin.getPlayerSkillManager().carregarDados(player);
                if (dados.possuiHabilidade(grupo, habilidade.getNome())) {
                    if (event.isLeftClick()) {
                        plugin.getGuiManager().abrirMenuUpgrade(player, habilidade, grupo);
                    } else if (event.isRightClick()) {
                        plugin.getPlayerSkillManager().toggleHabilidade(player, habilidade, grupo);
                    }
                } else {
                    if (event.isLeftClick()) {
                        plugin.getGuiManager().abrirMenuConfirmacaoCompra(player, habilidade, grupo);
                    }
                }
                return;
            }
        }
    }

    private void handleMenuCompraClick(Player player, ItemStack itemClicado, String title, String grupo) {
        String nomeHabilidade = extrairNomeHabilidadeDoTitulo(title, plugin.getGuiManager().TITULO_MENU_COMPRA_PREFIX);

        if (itemClicado.getType() == Material.STAINED_GLASS_PANE && itemClicado.getDurability() == 5) { // Confirmar
            Habilidade habilidade = getHabilidadePeloNome(nomeHabilidade);
            if (habilidade != null) plugin.getPlayerSkillManager().tentarComprarOuUparHabilidade(player, habilidade, grupo);
        } else if (itemClicado.getType() == Material.STAINED_GLASS_PANE && itemClicado.getDurability() == 14) { // Cancelar
            plugin.getGuiManager().abrirMenuHabilidades(player, grupo);
        }
    }

    private void handleMenuUpgradeClick(Player player, ItemStack itemClicado, String title, int slot, String grupo) {
        String nomeHabilidade = extrairNomeHabilidadeDoTitulo(title, plugin.getGuiManager().TITULO_MENU_UPGRADE_PREFIX);
        Habilidade habilidadeClicada = getHabilidadePeloNome(nomeHabilidade);
        if (habilidadeClicada == null) return;

        if (itemClicado.getType() == Material.STAINED_GLASS_PANE && itemClicado.getDurability() == 4) {
            plugin.getPlayerSkillManager().tentarComprarOuUparHabilidade(player, habilidadeClicada, grupo);
        }
        else if (itemClicado.getType() == Material.INK_SACK && slot == 30) {
            plugin.getPlayerSkillManager().toggleHabilidade(player, habilidadeClicada, grupo);
        }
    }

    // --- Métodos Auxiliares ---
    private Habilidade getHabilidadePeloNome(String nome) {
        for (Habilidade h : plugin.getHabilidadeManager().getHabilidadesRegistradas()) {
            if (h.getNome().equalsIgnoreCase(nome)) {
                return h;
            }
        }
        return null;
    }

    private String extrairGrupoDoTitulo(String titulo) {
        if (titulo.contains("(") && titulo.contains(")")) {
            try {
                return titulo.substring(titulo.indexOf("(") + 1, titulo.indexOf(")"));
            } catch (Exception e) { return null; }
        }
        return null;
    }

    private String extrairNomeHabilidadeDoTitulo(String titulo, String prefixo) {
        String semPrefixo = titulo.replace(prefixo, "");
        if (semPrefixo.contains("(")) {
            return semPrefixo.substring(0, semPrefixo.indexOf(" (")).trim();
        }
        return semPrefixo.trim();
    }
}