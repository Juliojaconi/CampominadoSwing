# Campo Minado Swing 💣

Implementação do clássico jogo **Campo Minado** em **Java** utilizando a biblioteca gráfica **Swing**. O projeto aplica o padrão de projeto Observer para uma arquitetura orientada a eventos e segue a separação de camadas **MVC** (Model-View-Controller).

---

## Sumário

- [Sobre o Projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Arquitetura](#arquitetura)
  - [Camada Modelo](#camada-modelo-brcomcod3rcmmodelo)
  - [Camada Visão](#camada-visão-brcomcod3rcmvisao)
- [Padrões de Projeto](#padrões-de-projeto)
- [Regras do Jogo](#regras-do-jogo)
- [Pré-requisitos](#pré-requisitos)
- [Como Compilar e Executar](#como-compilar-e-executar)
- [Configuração do Tabuleiro](#configuração-do-tabuleiro)
- [Problemas Conhecidos](#problemas-conhecidos)

---

## Sobre o Projeto

O **CampominadoSwing** é um jogo Campo Minado completo desenvolvido como projeto de estudo de Java. O objetivo é demonstrar na prática:

- Uso do padrão Observer para comunicação entre camadas
- Separação de responsabilidades com MVC
- Algoritmos recursivos para abertura em cascata de células
- Construção de interfaces gráficas com Swing

---

## Funcionalidades

- Tabuleiro de **16 linhas × 30 colunas** com **50 minas** posicionadas aleatoriamente
- **Clique esquerdo** para abrir uma célula
- **Clique direito** para marcar/desmarcar uma célula suspeita com a letra `M`
- **Abertura em cascata** automática ao revelar células sem minas vizinhas
- Exibição do **número de minas adjacentes** com codificação por cores
- Diálogo de **vitória** ou **derrota** ao término da partida
- **Reinício automático** do jogo após cada partida (minas redistribuídas)

---

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 25 |
| Java Swing | (incluído no JDK) |
| Apache Maven | 3.6+ |

> O projeto não possui dependências externas — utiliza exclusivamente a biblioteca padrão do Java.

---

## Estrutura do Projeto

```
CampominadoSwing/
├── pom.xml                          # Configuração do build Maven
└── src/
    └── main/
        └── java/
            └── br/
                └── com/
                    └── cod3r/
                        └── cm/
                            ├── modelo/              # Lógica do jogo (Model)
                            │   ├── Campo.java           # Célula individual do tabuleiro
                            │   ├── Tabuleiro.java       # Gerenciamento do tabuleiro e regras
                            │   ├── CampoObservador.java # Interface Observer de célula
                            │   ├── CampoEvento.java     # Enum de eventos de célula
                            │   └── ResultadoEvento.java # Resultado (vitória/derrota)
                            └── visao/               # Interface gráfica (View)
                                ├── TelaPrincipal.java   # Janela principal do jogo
                                ├── PainelTabuleiro.java # Painel com o grid de células
                                └── BotaoCampo.java      # Botão visual de cada célula
```

---

## Arquitetura

O projeto adota a arquitetura **MVC**:

- **Model** (`modelo`): contém toda a lógica do jogo, sem nenhuma dependência de UI.
- **View** (`visao`): componentes Swing responsáveis apenas pela exibição e interação.
- **Controller**: responsabilidade implícita em `BotaoCampo`, que traduz eventos de mouse em ações do modelo.

```
┌─────────────────────────────────────────────────────┐
│                     VISÃO (View)                    │
│  TelaPrincipal → PainelTabuleiro → BotaoCampo       │
│          ↑ Observer (ResultadoEvento)                │
│          ↑ Observer (CampoEvento)                   │
├─────────────────────────────────────────────────────┤
│                    MODELO (Model)                   │
│        Tabuleiro ──────────────── Campo             │
│    (gerarCampos, sortearMinas,   (abrir, marcar,    │
│     associarVizinhos)             notificarObserv.) │
└─────────────────────────────────────────────────────┘
```

---

### Camada Modelo (`br.com.cod3r.cm.modelo`)

#### `Campo.java` — Célula Individual

Representa cada célula do tabuleiro.

**Atributos principais:**

| Atributo | Tipo | Descrição |
|---|---|---|
| `linha` | `int` | Índice da linha da célula |
| `coluna` | `int` | Índice da coluna da célula |
| `minado` | `boolean` | Indica se a célula contém uma mina |
| `aberto` | `boolean` | Indica se a célula foi revelada |
| `marcado` | `boolean` | Indica se a célula está marcada com bandeira |
| `vizinhos` | `List<Campo>` | Lista de células adjacentes (até 8) |

**Principais métodos:**

| Método | Descrição |
|---|---|
| `abrir()` | Abre a célula; se não houver minas vizinhas, abre recursivamente as vizinhas |
| `alterarMarcacao()` | Alterna a marcação de bandeira em células fechadas |
| `adicionarVizinho(Campo)` | Registra uma célula como vizinha |
| `minasNaVizinhanca()` | Retorna a quantidade de minas entre as vizinhas |
| `vizinhoSeguro()` | Verifica se todas as vizinhas são livres de minas |
| `objetivoAlcancado()` | Célula satisfaz a condição de vitória? |
| `reiniciar()` | Redefine o estado da célula para o início de partida |
| `registrarObservador(CampoObservador)` | Registra um observador de eventos da célula |

**Lógica de abertura recursiva:**

```
abrir()
  └── célula fechada e não marcada?
        ├── é mina → dispara EXPLODIR → notifica observadores
        └── não é mina → marca como aberta
              └── todas as vizinhas são seguras?
                    └── sim → chama abrir() em cada vizinha (recursão)
```

---

#### `Tabuleiro.java` — Gerenciador do Jogo

Controla o estado global do tabuleiro e as regras de vitória/derrota.

**Atributos principais:**

| Atributo | Tipo | Descrição |
|---|---|---|
| `linhas` | `int` | Número de linhas do tabuleiro |
| `colunas` | `int` | Número de colunas do tabuleiro |
| `minas` | `int` | Quantidade de minas a posicionar |
| `campos` | `List<Campo>` | Todas as células do tabuleiro |
| `observadores` | `List<Consumer<ResultadoEvento>>` | Observadores do resultado do jogo |

**Fluxo de inicialização (construtor):**

```
Tabuleiro(linhas, colunas, minas)
  ├── gerarCampos()       → cria linhas × colunas células
  ├── associarVizinhos()  → liga cada célula às suas 8 vizinhas
  └── sortearMinas()      → distribui aleatoriamente as minas
```

**Principais métodos:**

| Método | Descrição |
|---|---|
| `abrir(linha, coluna)` | Abre a célula na posição indicada |
| `alternarMarcacao(linha, coluna)` | Alterna marcação da célula na posição indicada |
| `objetivoAlcancado()` | Retorna `true` quando o jogador vence |
| `reiniciar()` | Reinicia o tabuleiro e redistribui as minas |
| `paraCada(Consumer<Campo>)` | Itera sobre todas as células |
| `registrarObservador(Consumer<ResultadoEvento>)` | Registra listener para resultado do jogo |

**Condição de vitória:**  
Todas as células não minadas estão abertas **e/ou** todas as minas estão marcadas.

**Condição de derrota:**  
Uma mina é aberta → todas as minas não marcadas são reveladas automaticamente.

---

#### `CampoObservador.java` — Interface Observer

Interface funcional utilizada para observar eventos em células individuais.

```java
public interface CampoObservador {
    void eventoOcorreu(Campo c, CampoEvento evento);
}
```

---

#### `CampoEvento.java` — Tipos de Eventos

Enum com os possíveis eventos de uma célula:

| Evento | Descrição |
|---|---|
| `ABRIR` | Célula foi aberta |
| `MARCAR` | Célula foi marcada com bandeira |
| `DESMARCAR` | Marcação foi removida da célula |
| `EXPLODIR` | Mina foi aberta (explosão) |
| `REINICIAR` | Tabuleiro foi reiniciado |

---

#### `ResultadoEvento.java` — Resultado da Partida

Encapsula o resultado de uma partida.

| Atributo | Tipo | Descrição |
|---|---|---|
| `ganhou` | `boolean` | `true` = vitória, `false` = derrota |

---

### Camada Visão (`br.com.cod3r.cm.visao`)

#### `TelaPrincipal.java` — Janela Principal

Configura e exibe a janela principal do jogo (`JFrame`).

**Configurações da janela:**

| Propriedade | Valor |
|---|---|
| Título | `"Campo Minado"` |
| Largura | 690 px |
| Altura | 438 px |
| Posição | Centralizada na tela |
| Comportamento ao fechar | `DISPOSE_ON_CLOSE` |

**Configuração do tabuleiro:**

```java
Tabuleiro tabuleiro = new Tabuleiro(16, 30, 50);
```

---

#### `PainelTabuleiro.java` — Painel do Tabuleiro

`JPanel` que organiza os botões das células em grade usando `GridLayout(16, 30)`.

**Responsabilidades:**
- Cria um `BotaoCampo` para cada célula do tabuleiro
- Registra observador de resultado para exibir diálogos de vitória/derrota
- Solicita o reinício automático do jogo após cada partida

---

#### `BotaoCampo.java` — Botão de Célula

`JButton` que representa visualmente uma célula e responde a eventos de mouse.

**Estados visuais:**

| Estado | Fundo | Texto | Borda |
|---|---|---|---|
| Fechada (padrão) | Cinza `RGB(184,184,184)` | — | Chanfrada (3D) |
| Marcada | Azul `RGB(8,179,247)` | `M` | Chanfrada (3D) |
| Aberta sem minas vizinhas | Cinza | — | Linha simples |
| Aberta com minas vizinhas | Cinza | Número colorido | Linha simples |
| Explodida (mina) | Vermelho `RGB(189,66,68)` | `X` | Linha simples |

**Cores do contador de minas:**

| Quantidade | Cor |
|---|---|
| 1 | Verde |
| 2 | Azul |
| 3 | Amarelo |
| 4, 5 ou 6 | Vermelho |
| 7 ou 8 | Rosa |

**Eventos de mouse:**

| Ação | Efeito |
|---|---|
| Clique esquerdo | `campo.abrir()` |
| Clique direito | `campo.alterarMarcacao()` |

---

## Padrões de Projeto

### Observer
Utilizado em dois níveis:

1. **Nível de célula** — `CampoObservador` notifica `BotaoCampo` sobre mudanças visuais (`ABRIR`, `MARCAR`, `DESMARCAR`, `EXPLODIR`, `REINICIAR`).
2. **Nível de tabuleiro** — `Consumer<ResultadoEvento>` notifica `PainelTabuleiro` sobre o resultado da partida (vitória ou derrota).

### MVC (Model-View-Controller)
- **Model**: pacote `modelo` — sem dependências de UI.
- **View**: pacote `visao` — sem lógica de jogo.
- **Controller**: `BotaoCampo` — traduz interações do usuário em chamadas ao modelo.

---

## Regras do Jogo

1. O tabuleiro possui células **fechadas**, **abertas** ou **marcadas**.
2. **Clique esquerdo** em uma célula fechada a abre:
   - Se for **mina** → fim de jogo (derrota).
   - Se tiver **minas vizinhas** → exibe o número de minas adjacentes.
   - Se **não tiver minas vizinhas** → abre automaticamente todas as vizinhas (cascata).
3. **Clique direito** em uma célula fechada coloca/remove a marcação `M`.
4. **Vitória**: todas as células sem mina estão abertas ou todas as minas estão marcadas.
5. **Derrota**: qualquer mina é aberta.
6. Após vitória ou derrota, o jogo reinicia automaticamente com novas posições de minas.

---

## Pré-requisitos

- **Java 25** (ou superior) instalado e configurado no `PATH`
- **Apache Maven 3.6+** instalado e configurado no `PATH`

Verifique as instalações:

```bash
java -version
mvn -version
```

---

## Como Compilar e Executar

### Compilar

```bash
mvn clean compile
```

### Executar pela IDE

Abra o projeto em **IntelliJ IDEA** ou **Eclipse**, localize a classe `TelaPrincipal` e execute-a diretamente.

### Executar pela linha de comando (após compilação)

```bash
java -cp target/classes br.com.cod3r.cm.visao.TelaPrincipal
```

### Gerar JAR executável

Adicione o plugin `maven-jar-plugin` ao `pom.xml` e execute:

```bash
mvn clean package
java -jar target/campo-minado-swing-1.0-SNAPSHOT.jar
```

---

## Configuração do Tabuleiro

Os parâmetros do jogo são definidos em `TelaPrincipal.java`:

```java
Tabuleiro tabuleiro = new Tabuleiro(16, 30, 50);
//                                   ↑    ↑   ↑
//                                linhas cols minas
```

| Parâmetro | Valor padrão | Descrição |
|---|---|---|
| `linhas` | 16 | Número de linhas do tabuleiro |
| `colunas` | 30 | Número de colunas do tabuleiro |
| `minas` | 50 | Quantidade de minas escondidas |

---

## Problemas Conhecidos

| # | Arquivo | Descrição |
|---|---|---|
| 1 | `Campo.java` | Comparação incorreta ao detectar vizinhos diagonais: `this.linha != vizinho.coluna` deveria ser `this.coluna != vizinho.coluna` |
| 2 | `TelaPrincipal.java` | Método `main` sem o parâmetro `String[] args`, impedindo execução direta via linha de comando |
