package com.brunoruan.Brskills.managers;

import com.brunoruan.Brskills.SkiHabilidades;
import com.brunoruan.Brskills.habilidades.*;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import java.util.List;

public class HabilidadeManager {
    private final List<Habilidade> habilidadesRegistradas = new ArrayList<>();

    public HabilidadeManager(SkiHabilidades plugin) {
        registrarHabilidade(new FenixHabilidade(plugin), plugin);
        registrarHabilidade(new VidaExtraHabilidade(plugin), plugin);
        registrarHabilidade(new ProtetorHabilidade(plugin), plugin);
        registrarHabilidade(new RaivosoHabilidade(plugin), plugin);
        registrarHabilidade(new UltimaChanceHabilidade(plugin), plugin);
        registrarHabilidade(new ConstrutorHabilidade(plugin), plugin);
        registrarHabilidade(new RessarcimentoHabilidade(plugin), plugin);
        registrarHabilidade(new PesoPenaHabilidade(plugin), plugin);
        registrarHabilidade(new EspiaoHabilidade(plugin), plugin);
        registrarHabilidade(new RevelacaoHabilidade(plugin), plugin);
        registrarHabilidade(new VampirismoHabilidade(plugin), plugin);
        registrarHabilidade(new ToqueIgneoHabilidade(plugin), plugin);
        registrarHabilidade(new HadesHabilidade(plugin), plugin);
    }

    private void registrarHabilidade(Habilidade habilidade, SkiHabilidades plugin) {
        habilidadesRegistradas.add(habilidade);
        Bukkit.getPluginManager().registerEvents(habilidade, plugin);
    }

    public List<Habilidade> getHabilidadesRegistradas() {
        return habilidadesRegistradas;
    }
}