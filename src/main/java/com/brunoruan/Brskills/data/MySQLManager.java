package com.brunoruan.Brskills.data;

import com.brunoruan.Brskills.SkiHabilidades;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.Map;
import java.util.UUID;

public class MySQLManager implements IDataManager {

    private final SkiHabilidades plugin;
    private Connection connection;

    private final String host, database, username, password, tablePrefix;
    private final int port;

    public MySQLManager(SkiHabilidades plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.host = config.getString("storage.mysql.host");
        this.port = config.getInt("storage.mysql.port");
        this.database = config.getString("storage.mysql.database");
        this.username = config.getString("storage.mysql.username");
        this.password = config.getString("storage.mysql.password");
        this.tablePrefix = config.getString("storage.mysql.table_prefix");
    }

    @Override
    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            String url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?autoReconnect=true&useSSL=false";
            this.connection = DriverManager.getConnection(url, this.username, this.password);
            plugin.getLogger().info("Conexão com o banco de dados MySQL estabelecida.");
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Não foi possível conectar ao banco de dados MySQL! Verifique suas credenciais no config.yml");
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void createTables() {
        String tableName = tablePrefix + "player_skills";
        // Usamos VARCHAR(36) para o UUID e UNIQUE KEY para evitar dados duplicados
        String sql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
                " `id` INT AUTO_INCREMENT PRIMARY KEY," +
                " `player_uuid` VARCHAR(36) NOT NULL," +
                " `game_mode` VARCHAR(64) NOT NULL," +
                " `skill_name` VARCHAR(64) NOT NULL," +
                " `skill_level` INT NOT NULL," +
                " `is_active` BOOLEAN NOT NULL DEFAULT FALSE," +
                " UNIQUE KEY `unique_skill` (`player_uuid`, `game_mode`, `skill_name`)" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Nao foi possivel criar a tabela do MySQL.");
            e.printStackTrace();
        }
    }

    @Override
    public PlayerData loadPlayerData(UUID playerUuid) {
        PlayerData playerData = new PlayerData();
        String tableName = tablePrefix + "player_skills";
        String sql = "SELECT `game_mode`, `skill_name`, `skill_level`, `is_active` FROM `" + tableName + "` WHERE `player_uuid` = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String gameMode = rs.getString("game_mode");
                String skillName = rs.getString("skill_name");
                int skillLevel = rs.getInt("skill_level");
                boolean isActive = rs.getBoolean("is_active");

                playerData.comprarOuUparHabilidade(gameMode, skillName, skillLevel);
                if (isActive) {
                    playerData.ativarHabilidade(gameMode, skillName);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Nao foi possivel carregar os dados do UUID: " + playerUuid);
            e.printStackTrace();
        }
        return playerData;
    }

    @Override
    public void savePlayerData(UUID playerUuid, PlayerData playerData) {
        String tableName = tablePrefix + "player_skills";

        // Primeiro, deletamos todos os dados antigos do jogador para garantir consistência
        String deleteSql = "DELETE FROM `" + tableName + "` WHERE `player_uuid` = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return; // Se não conseguir deletar, não tenta inserir para evitar dados corrompidos
        }

        // Agora, inserimos todos os dados atuais do jogador
        String insertSql = "INSERT INTO `" + tableName + "` (`player_uuid`, `game_mode`, `skill_name`, `skill_level`, `is_active`) VALUES (?,?,?,?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
            for (Map.Entry<String, PlayerData.SkillData> entry : playerData.getDadosPorModo().entrySet()) {
                String gameMode = entry.getKey();
                PlayerData.SkillData skillData = entry.getValue();

                for (Map.Entry<String, Integer> compradasEntry : skillData.habilidadesCompradas.entrySet()) {
                    String skillName = compradasEntry.getKey();
                    int skillLevel = compradasEntry.getValue();
                    boolean isActive = skillData.habilidadesAtivas.contains(skillName);

                    pstmt.setString(1, playerUuid.toString());
                    pstmt.setString(2, gameMode);
                    pstmt.setString(3, skillName);
                    pstmt.setInt(4, skillLevel);
                    pstmt.setBoolean(5, isActive);
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            plugin.getLogger().severe("Nao foi possivel salvar os dados do UUID: " + playerUuid);
            e.printStackTrace();
        }
    }
}