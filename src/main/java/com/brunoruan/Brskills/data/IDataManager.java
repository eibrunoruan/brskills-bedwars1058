package com.brunoruan.Brskills.data;

import java.util.UUID;

public interface IDataManager {

    /**
     * Inicia a conexão com a fonte de dados (banco de dados ou arquivos).
     */
    void connect();

    /**
     * Fecha a conexão com a fonte de dados.
     */
    void disconnect();

    /**
     * Carrega os dados de um jogador da fonte de dados para um objeto PlayerData.
     * @param playerUuid O UUID do jogador.
     * @return Um objeto PlayerData com os dados carregados, ou um novo se não houver dados.
     */
    PlayerData loadPlayerData(UUID playerUuid);

    /**
     * Salva os dados de um jogador da memória para a fonte de dados.
     * @param playerUuid O UUID do jogador.
     * @param playerData O objeto PlayerData contendo os dados a serem salvos.
     */
    void savePlayerData(UUID playerUuid, PlayerData playerData);
}