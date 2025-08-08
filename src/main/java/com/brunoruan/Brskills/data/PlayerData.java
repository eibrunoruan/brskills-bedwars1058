package com.brunoruan.Brskills.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerData {

    public static class SkillData {
        public Map<String, Integer> habilidadesCompradas = new HashMap<>();
        public Set<String> habilidadesAtivas = new HashSet<>();
    }

    private final Map<String, SkillData> dadosPorModo = new HashMap<>();

    public Map<String, SkillData> getDadosPorModo() {
        return dadosPorModo;
    }

    private SkillData getSkillData(String grupo) {
        return dadosPorModo.computeIfAbsent(grupo, k -> new SkillData());
    }

    public int getNivelHabilidade(String grupo, String nomeHabilidade) {
        return getSkillData(grupo).habilidadesCompradas.getOrDefault(nomeHabilidade.toLowerCase(), 0);
    }

    public void comprarOuUparHabilidade(String grupo, String nomeHabilidade) {
        SkillData dadosDoModo = getSkillData(grupo);
        int nivelAtual = dadosDoModo.habilidadesCompradas.getOrDefault(nomeHabilidade.toLowerCase(), 0);
        dadosDoModo.habilidadesCompradas.put(nomeHabilidade.toLowerCase(), nivelAtual + 1);
    }

    public void comprarOuUparHabilidade(String grupo, String nomeHabilidade, int nivel) {
        getSkillData(grupo).habilidadesCompradas.put(nomeHabilidade.toLowerCase(), nivel);
    }

    public boolean possuiHabilidade(String grupo, String nomeHabilidade) {
        return getSkillData(grupo).habilidadesCompradas.containsKey(nomeHabilidade.toLowerCase());
    }

    public boolean isHabilidadeAtiva(String grupo, String nomeHabilidade) {
        return getSkillData(grupo).habilidadesAtivas.contains(nomeHabilidade.toLowerCase());
    }

    public void ativarHabilidade(String grupo, String nomeHabilidade) {
        getSkillData(grupo).habilidadesAtivas.add(nomeHabilidade.toLowerCase());
    }

    public void desativarHabilidade(String grupo, String nomeHabilidade) {
        getSkillData(grupo).habilidadesAtivas.remove(nomeHabilidade.toLowerCase());
    }
}