package com.brunoruan.Brskills.data;

import com.brunoruan.Brskills.SkiHabilidades;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class DataManager {

    private final SkiHabilidades plugin;
    private final File dataFolder;

    public DataManager(SkiHabilidades plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public PlayerData loadPlayerData(Player player) {
        File playerFile = new File(dataFolder, player.getUniqueId().toString() + ".yml");
        PlayerData playerData = new PlayerData();

        if (!playerFile.exists()) {
            return playerData;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        if (config.isConfigurationSection("modos")) {
            for (String grupo : config.getConfigurationSection("modos").getKeys(false)) {
                if (config.isConfigurationSection("modos." + grupo + ".compradas")) {
                    for (String skillName : config.getConfigurationSection("modos." + grupo + ".compradas").getKeys(false)) {
                        int level = config.getInt("modos." + grupo + ".compradas." + skillName);
                        playerData.comprarOuUparHabilidade(grupo, skillName, level);
                    }
                }
                if (config.isList("modos." + grupo + ".ativas")) {
                    for (String skillName : config.getStringList("modos." + grupo + ".ativas")) {
                        playerData.ativarHabilidade(grupo, skillName);
                    }
                }
            }
        }
        return playerData;
    }

    public void savePlayerData(Player player, PlayerData playerData) {
        File playerFile = new File(dataFolder, player.getUniqueId().toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, PlayerData.SkillData> entry : playerData.getDadosPorModo().entrySet()) {
            String grupo = entry.getKey();
            PlayerData.SkillData skillData = entry.getValue();

            for (Map.Entry<String, Integer> compradasEntry : skillData.habilidadesCompradas.entrySet()) {
                config.set("modos." + grupo + ".compradas." + compradasEntry.getKey(), compradasEntry.getValue());
            }
            config.set("modos." + grupo + ".ativas", new ArrayList<>(skillData.habilidadesAtivas));
        }

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Nao foi possivel salvar os dados do jogador " + player.getName());
            e.printStackTrace();
        }
    }
}