package com.brunoruan.Brskills.managers;

import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.brunoruan.Brskills.SkiHabilidades;
import com.brunoruan.Brskills.data.PlayerData;
import com.brunoruan.Brskills.habilidades.Habilidade;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.bukkit.Bukkit.getServer;

public class PlayerSkillManager {
    private Economy economy = null;
    private final SkiHabilidades plugin;
    private final Map<UUID, PlayerData> dadosDosJogadores = new HashMap<>();
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public PlayerSkillManager(SkiHabilidades plugin) {
        this.plugin = plugin;
        if (!setupEconomy()) {
            plugin.getLogger().severe("Vault ou um plugin de economia não foi encontrado! A compra de habilidades estará desativada.");
        }
    }

    public int[] getProgressoHabilidades(Player player, String grupo) {
        PlayerData dados = carregarDados(player);
        int totalNiveisPossiveis = 0;
        int niveisDesbloqueados = 0;
        for (Habilidade hab : plugin.getHabilidadeManager().getHabilidadesRegistradas()) {
            totalNiveisPossiveis += hab.getMaxLevel();
            niveisDesbloqueados += dados.getNivelHabilidade(grupo, hab.getNome());
        }
        return new int[]{niveisDesbloqueados, totalNiveisPossiveis};
    }

    public PlayerData carregarDados(Player player) {
        attachments.computeIfAbsent(player.getUniqueId(), k -> player.addAttachment(plugin));
        return dadosDosJogadores.computeIfAbsent(player.getUniqueId(), uuid -> plugin.getDataManager().loadPlayerData(uuid));
    }

    public void descarregarDados(Player player) {
        PlayerData dados = dadosDosJogadores.get(player.getUniqueId());
        if (dados != null) {
            plugin.getDataManager().savePlayerData(player.getUniqueId(), dados);
            dadosDosJogadores.remove(player.getUniqueId());
        }
        PermissionAttachment att = attachments.remove(player.getUniqueId());
        if (att != null) {
            player.removeAttachment(att);
        }
    }

    public int getNivelMaximoDaHabilidadeNoTime(ITeam time, String nomeHabilidade) {
        int maxNivel = 0;
        String grupo = time.getArena().getGroup();
        for (Player membro : time.getMembers()) {
            PlayerData dados = carregarDados(membro);
            if (dados.isHabilidadeAtiva(grupo, nomeHabilidade)) {
                int nivelDoMembro = dados.getNivelHabilidade(grupo, nomeHabilidade);
                if (nivelDoMembro > maxNivel) {
                    maxNivel = nivelDoMembro;
                }
            }
        }
        return maxNivel;
    }

    public boolean isHabilidadeDeTimeAtiva(ITeam time, String nomeHabilidade) {
        String grupo = time.getArena().getGroup();
        for (Player membro : time.getMembers()) {
            PlayerData dados = carregarDados(membro);
            if (dados.isHabilidadeAtiva(grupo, nomeHabilidade)) {
                return true;
            }
        }
        return false;
    }

    public void tentarComprarOuUparHabilidade(Player player, Habilidade habilidade, String grupo) {
        if (economy == null) {
            player.sendMessage(ChatColor.RED + "O sistema de economia não está funcionando. Contate um administrador.");
            return;
        }
        PlayerData dados = carregarDados(player);
        int nivelAtual = dados.getNivelHabilidade(grupo, habilidade.getNome());
        if (nivelAtual >= habilidade.getMaxLevel()) {
            player.sendMessage(ChatColor.RED + "Você já atingiu o nível máximo desta habilidade!");
            return;
        }
        int proximoNivel = nivelAtual + 1;
        double custo = habilidade.getCustoDeCoins(proximoNivel);
        OfflinePlayer offlinePlayer = getServer().getOfflinePlayer(player.getUniqueId());
        if (economy.getBalance(offlinePlayer) >= custo) {
            EconomyResponse response = economy.withdrawPlayer(offlinePlayer, custo);
            if (response.transactionSuccess()) {
                dados.comprarOuUparHabilidade(grupo, habilidade.getNome());
                String acao = (nivelAtual == 0) ? "comprou" : "evoluiu";
                player.sendMessage(ChatColor.GREEN + "Você " + acao + " a habilidade " + habilidade.getNome() + " para o nível " + proximoNivel + "!");
                plugin.getGuiManager().abrirMenuHabilidades(player, grupo);
            } else {
                player.sendMessage(ChatColor.RED + "Ocorreu um erro na transação. Tente novamente.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Você não tem coins suficientes! Custa: " + custo);
            player.closeInventory();
        }
    }

    public void toggleHabilidade(Player player, Habilidade habilidade, String grupo) {
        PlayerData dados = carregarDados(player);
        if (!dados.possuiHabilidade(grupo, habilidade.getNome())) {
            player.sendMessage(ChatColor.RED + "Você precisa comprar esta habilidade primeiro!");
            return;
        }
        if (dados.isHabilidadeAtiva(grupo, habilidade.getNome())) {
            desativarHabilidade(player, habilidade, grupo);
        } else {
            ativarHabilidade(player, habilidade, grupo);
        }
    }

    public void ativarHabilidade(Player player, Habilidade habilidade, String grupo) {
        PlayerData dados = carregarDados(player);
        if (dados.isHabilidadeAtiva(grupo, habilidade.getNome())) return;
        int pontosUsados = getPontosUsados(player, grupo);
        int maxPontos = getMaxPontos(player);
        int custoPontos = habilidade.getCustoDePontos();
        if ((pontosUsados + custoPontos) <= maxPontos) {
            dados.ativarHabilidade(grupo, habilidade.getNome());
            attachments.get(player.getUniqueId()).setPermission(habilidade.getPermissao(), true);
            player.sendMessage(ChatColor.GREEN + "Habilidade " + habilidade.getNome() + " ativada!");
            plugin.getGuiManager().abrirMenuHabilidades(player, grupo);
        } else {
            player.sendMessage(ChatColor.RED + "Você não tem Pontos de Habilidade suficientes!");
            player.sendMessage(ChatColor.RED + "Limite: " + (pontosUsados + custoPontos) + "/" + maxPontos);
        }
    }

    public void desativarHabilidade(Player player, Habilidade habilidade, String grupo) {
        PlayerData dados = carregarDados(player);
        if (!dados.isHabilidadeAtiva(grupo, habilidade.getNome())) return;
        dados.desativarHabilidade(grupo, habilidade.getNome());
        attachments.get(player.getUniqueId()).unsetPermission(habilidade.getPermissao());
        player.sendMessage(ChatColor.YELLOW + "Habilidade " + habilidade.getNome() + " desativada!");
        plugin.getGuiManager().abrirMenuHabilidades(player, grupo);
    }

    public int getPontosUsados(Player player, String grupo) {
        PlayerData dados = carregarDados(player);
        int total = 0;
        for (Habilidade hab : plugin.getHabilidadeManager().getHabilidadesRegistradas()) {
            if (dados.isHabilidadeAtiva(grupo, hab.getNome())) {
                total += hab.getCustoDePontos();
            }
        }
        return total;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public int getMaxPontos(Player player) {
        if (player.hasPermission("skihabilidades.limite.mvp_plus")) return 12;
        if (player.hasPermission("skihabilidades.limite.mvp")) return 10;
        if (player.hasPermission("skihabilidades.limite.vip")) return 8;
        return 20;
    }
}