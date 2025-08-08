package com.brunoruan.Brskills;

import com.andrei1058.bedwars.api.BedWars;
import com.brunoruan.Brskills.comandos.ComandoHabilidades;
import com.brunoruan.Brskills.data.IDataManager;
import com.brunoruan.Brskills.data.MongoManager;
import com.brunoruan.Brskills.data.MySQLManager;
import com.brunoruan.Brskills.data.YamlManager;
import com.brunoruan.Brskills.gui.GUIManager;
import com.brunoruan.Brskills.listeners.GUIListener;
import com.brunoruan.Brskills.listeners.PlayerConnectionListener;
import com.brunoruan.Brskills.managers.HabilidadeManager;
import com.brunoruan.Brskills.managers.PlayerSkillManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SkiHabilidades extends JavaPlugin {

    private static SkiHabilidades instance;
    private static BedWars bedwarsAPI;
    private IDataManager dataManager; // Agora usa a Interface
    private HabilidadeManager habilidadeManager;
    private PlayerSkillManager playerSkillManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        if (Bukkit.getPluginManager().getPlugin("BedWars1058") == null || Bukkit.getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Uma ou mais dependencias (BedWars1058, Vault) nao foram encontradas. Desabilitando...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        setupDataManager();
        dataManager.connect();

        bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();

        playerSkillManager = new PlayerSkillManager(this);
        habilidadeManager = new HabilidadeManager(this);
        guiManager = new GUIManager(this);

        getCommand("habilidades").setExecutor(new ComandoHabilidades(this));

        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(this), this);

        getLogger().info("SkiHabilidades foi ativado com sucesso!");
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerSkillManager.descarregarDados(player);
        }
        if (dataManager != null) {
            dataManager.disconnect();
        }
        getLogger().info("SkiHabilidades foi desabilitado.");
    }

    private void setupDataManager() {
        String method = getConfig().getString("storage.method", "YAML").toUpperCase();
        switch (method) {
            case "MYSQL":
                dataManager = new MySQLManager(this);
                break;
            case "MONGODB":
                dataManager = new MongoManager(this);
                break;
            default: // YAML ou qualquer outra coisa
                dataManager = new YamlManager(this);
                break;
        }
    }

    public static SkiHabilidades getInstance() { return instance; }
    public static BedWars getBedwarsAPI() { return bedwarsAPI; }
    public IDataManager getDataManager() { return dataManager; }
    public HabilidadeManager getHabilidadeManager() { return habilidadeManager; }
    public PlayerSkillManager getPlayerSkillManager() { return playerSkillManager; }
    public GUIManager getGuiManager() { return guiManager; }
}