package com.brunoruan.Brskills.data;

import com.brunoruan.Brskills.SkiHabilidades;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MongoManager implements IDataManager {

    private final SkiHabilidades plugin;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    private final String connectionUri, databaseName, collectionName;

    public MongoManager(SkiHabilidades plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.connectionUri = config.getString("storage.mongodb.connection-uri");
        this.databaseName = config.getString("storage.mongodb.database");
        this.collectionName = config.getString("storage.mongodb.collection");
    }

    @Override
    public void connect() {
        try {
            this.mongoClient = MongoClients.create(connectionUri);
            this.database = mongoClient.getDatabase(databaseName);
            this.collection = database.getCollection(collectionName);
            plugin.getLogger().info("Conexão com o banco de dados MongoDB estabelecida.");
        } catch (Exception e) {
            plugin.getLogger().severe("Não foi possível conectar ao banco de dados MongoDB! Verifique sua connection-uri.");
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @Override
    public PlayerData loadPlayerData(UUID playerUuid) {
        PlayerData playerData = new PlayerData();
        Document filter = new Document("_id", playerUuid.toString());
        Document data = collection.find(filter).first();

        if (data == null) {
            return playerData;
        }

        Document modos = data.get("modos", Document.class);
        if (modos != null) {
            for (String grupo : modos.keySet()) {
                Document modoData = modos.get(grupo, Document.class);
                if (modoData != null) {
                    Document compradas = modoData.get("compradas", Document.class);
                    if (compradas != null) {
                        for (Map.Entry<String, Object> entry : compradas.entrySet()) {
                            if (entry.getValue() instanceof Integer) {
                                playerData.comprarOuUparHabilidade(grupo, entry.getKey(), (Integer) entry.getValue());
                            }
                        }
                    }
                    List<String> ativas = modoData.getList("ativas", String.class);
                    if (ativas != null) {
                        for (String skillName : ativas) {
                            playerData.ativarHabilidade(grupo, skillName);
                        }
                    }
                }
            }
        }
        return playerData;
    }

    @Override
    public void savePlayerData(UUID playerUuid, PlayerData playerData) {
        Document playerDocument = new Document("_id", playerUuid.toString());
        Document modosDocument = new Document();

        for (Map.Entry<String, PlayerData.SkillData> entry : playerData.getDadosPorModo().entrySet()) {
            String grupo = entry.getKey();
            PlayerData.SkillData skillData = entry.getValue();

            Document modoData = new Document();

            // CORREÇÃO APLICADA AQUI: Passando o mapa diretamente, sem "new Document()"
            modoData.put("compradas", skillData.habilidadesCompradas);
            modoData.put("ativas", new ArrayList<>(skillData.habilidadesAtivas));

            modosDocument.put(grupo, modoData);
        }
        playerDocument.put("modos", modosDocument);

        Document filter = new Document("_id", playerUuid.toString());
        ReplaceOptions options = new ReplaceOptions().upsert(true);

        collection.replaceOne(filter, playerDocument, options);
    }
}