<h1 align="center">
  <br>
  <img src="https://i.imgur.com/T5x4G9A.png" alt="BRSkills Logo" width="128">
  <br>
  BrSkills
  <br>
</h1>

<p align="center">
  <em>Um addon de habilidades completo e estratégico para o BedWars1058</em>
</p>

<p align="center">
  <a href="#">
    <img src="https://img.shields.io/badge/Minecraft-1.8.8-green.svg" alt="Minecraft Version">
  </a>
  <a href="#">
    <img src="https://img.shields.io/badge/Java-17+-orange.svg" alt="Java Version">
  </a>
    <a href="#">
    <img src="https://img.shields.io/badge/BedWars1058-25.2+-blue.svg" alt="BedWars1058 Version">
  </a>
  <a href="#">
    <img src="https://img.shields.io/badge/License-MIT-lightgrey.svg" alt="License">
  </a>
</p>

---

### 📖 Descrição
**BrSkills** é um plugin de addon para o popular `BedWars1058`. Ele introduz um sistema complexo e estratégico de habilidades persistentes que os jogadores podem comprar e evoluir. Com progressão separada para cada modo de jogo (Solo, Duplas, etc.), um sistema de economia via Vault, e limites de poder baseados em "Pontos de Habilidade", este plugin adiciona uma nova camada de profundidade e personalização à experiência do BedWars.

### ✨ Recursos Principais
* ⚙️ **Sistema de Habilidades Modulares:** Uma arquitetura profissional com 13 habilidades únicas, permitindo adicionar ou modificar novas facilmente.
* 🗺️ **Progressão por Modo de Jogo:** As habilidades compradas e evoluídas em um modo (ex: Solo) são independentes das de outro modo (ex: Quartetos).
* 💰 **Economia via Vault:** Integração total com qualquer plugin de economia suportado pelo Vault para a compra e evolução de habilidades.
* ⚖️ **Limites de Pontos de Habilidade:** Os jogadores têm um limite de pontos (definido por permissão) para ativar suas habilidades, incentivando escolhas estratégicas.
* 🖥️ **Menus (GUIs) Interativos:** Interfaces gráficas intuitivas para selecionar o modo de jogo, ver, comprar, evoluir e ativar habilidades.
* 💾 **Salvamento de Dados Flexível:** Suporte nativo para armazenamento de dados em arquivos **YAML**, **MySQL** ou **MongoDB**, totalmente configurável.

---

### ⚔️ Habilidades Disponíveis (13)

| Habilidade | Tipo | Descrição |
| :--- | :---: | :--- |
| **Fênix** | Individual | Renasça das cinzas! Ao sofrer uma morte final, esta habilidade te dá uma segunda chance em batalha. |
| **Vida Extra** | Individual | Mais resistente que o normal. Comece a partida e renasça sempre com corações extras para sobreviver. |
| **Raivoso** | Individual | A dor te fortalece. Ao entrar em combate, sua adrenalina dispara, concedendo-lhe Força I temporária. |
| **Última Chance** | Individual | Lute até o fim! Quando sua vida está por um fio, esta habilidade te dá uma explosão de vitalidade, recuperando parte da sua vida. |
| **Vampirismo** | Individual | Alimente-se da força vital de seus oponentes. Cada abate que você consegue restaura uma parte da sua própria vida. |
| **Toque Ígneo** | Individual | Suas mãos ardem com poder elemental. Seus ataques têm uma chance de colocar seus inimigos em chamas. |
| **Hades** | Individual | O submundo te fortalece. Estar em chamas não te enfraquece, pelo contrário, desperta uma fúria infernal que aumenta seu dano. |
| **Peso Pena** | Individual | A gravidade é apenas uma sugestão. Seus movimentos ágeis reduzem drasticamente o dano que você sofre de quedas. |
| **Espião** | Individual | Torne-se uma sombra. Fique agachado por um tempo para se camuflar no ambiente, tornando-se completamente invisível. |
| **Revelação** | Individual | Ninguém se esconde de você. Uma aura perceptiva ao seu redor revela inimigos invisíveis que se atrevem a chegar perto demais. |
| **Construtor** | Individual | Um mestre da eficiência. Sua experiência em construção permite que você tenha uma chance de não gastar blocos ao colocá-los. |
| **Ressarcimento** | Individual | Sua defesa é persistente. Blocos que você coloca têm uma chance de reaparecerem logo após serem quebrados por um inimigo. |
| **Protetor** | Time | Sua cama é sua fortaleza. A primeira vez que um inimigo tentar quebrá-la, ele será repelido e a cama ficará invulnerável por um curto período. |

---

### 🔌 Requisitos
Para que o `BrSkills` funcione, seu servidor precisa ter:
1.  **Servidor:** Spigot, Paper ou forks compatíveis, versão 1.8.8.
    > **⚠️ Importante:** O servidor **deve** rodar em **Java 17** ou superior.
2.  **Plugin Principal:** `BedWars1058` (versão 25.2 ou compatível).
3.  **API de Economia:** `Vault`.
4.  **Plugin de Economia:** Qualquer plugin de economia que se conecte ao Vault (ex: EssentialsX).
5.  **Plugin de Permissões:** Qualquer plugin de permissões (ex: LuckPerms).

### 🚀 Instalação
1.  Certifique-se de que todos os plugins listados em **Requisitos** estão instalados na pasta `plugins`.
2.  Coloque o arquivo `BrSkills.jar` na sua pasta `plugins`.
3.  Inicie o servidor uma vez para que a pasta `BrSkills` e o arquivo `config.yml` sejam gerados.
4.  Configure os arquivos conforme a seção abaixo.

### 🛠️ Configuração

#### `config.yml`
O arquivo `plugins/BrSkills/config.yml` permite escolher o método de armazenamento de dados:
* `storage.method`: Mude de `YAML` para `MYSQL` ou `MONGODB` conforme sua necessidade.
* `storage.mysql`: Preencha com as credenciais do seu banco de dados MySQL.
* `storage.mongodb`: Preencha com a URI de conexão do seu banco de dados MongoDB.

#### Grupos do BedWars1058
> Este é um passo **crucial** para que a progressão por modo de jogo funcione.
>
Vá em `plugins/BedWars1058/Arenas/` e, em cada arquivo `.yml` de arena, defina o grupo para separar o progresso das habilidades:
* Para um mapa solo: `group: Solo`
* Para um mapa de duplas: `group: Duplas`
* Para um mapa de quartetos: `group: Quartetos`

---

### 💬 Comandos
| Comando | Alias | Descrição |
| :--- | :--- | :--- |
| `/habilidades` | `/skills`, `/habs` | Abre o menu de seleção de modo para gerenciar as habilidades. |

### 🔑 Permissões

#### Limites de Pontos de Habilidade
Use estas permissões para definir quantos Pontos de Habilidade cada grupo de jogador pode usar.
* `brskills.limite.default` - Limite de 4 pontos (padrão para todos).
* `brskills.limite.vip` - Limite de 8 pontos.
* `brskills.limite.mvp` - Limite de 10 pontos.
* `brskills.limite.mvp_plus` - Limite de 12 pontos.

#### Habilidades
As permissões abaixo são gerenciadas internamente pelo plugin após a compra e ativação.
* `brskills.habilidade.fenix`
* `brskills.habilidade.vidaextra`
* `brskills.habilidade.protetor`
* `brskills.habilidade.raivoso`
* `brskills.habilidade.ultimachance`
* `brskills.habilidade.construtor`
* `brskills.habilidade.ressarcimento`
* `brskills.habilidade.pesopena`
* `brskills.habilidade.espiao`
* `brskills.habilidade.revelacao`
* `brskills.habilidade.vampirismo`
* `brskills.habilidade.toqueigneo`
* `brskills.habilidade.hades`
