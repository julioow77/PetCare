# PetCare

Sistema de gestão para pet shop e clínica veterinária desenvolvido em Java, com interface gráfica em Swing e modo de execução via console.

## Funcionalidades

- **Clientes**: cadastro de responsáveis (incluindo entrada via caixas de diálogo) e acúmulo de pontos de fidelidade em compras identificadas.
- **Animais**: cadastro de cachorros e gatos com porte (`PEQUENO`, `MEDIO`, `GRANDE`), estimativa de tempo de banho por espécie/porte e controle da validade da vacina.
- **Consultas**: agendamento com data e hora, registro de sintomas e laudo no prontuário, adicional automático para atendimento de urgência, cálculo com ou sem desconto, pagamento, cancelamento e emissão de recibo.
- **Serviços e vendas**:
  - **Banho e tosa**: recusa o atendimento via exceção própria (`VacinaAtrasadaException`) caso a vacina esteja vencida.
  - **Exames**: agendamento e registro tipado de laudo textual e medida numérica (`Prontuario<T>`).
  - **Venda de produtos**: venda avulsa ou vinculada a um cliente cadastrado para pontuação.
- **Histórico em arquivo**: gravação e leitura das consultas realizadas em `dados/historico_consultas.txt` (ou outro caminho informado por argumento).

## Estrutura do projeto

```text
PetCare/
├── src/
│   └── br/com/petshop/
│       ├── modelo/          # Entidades, enums, interfaces, exceção e regras de negócio
│       └── sistema/         # PetShopMain, PetShopServico, PetShopTela e recursos gráficos
├── dados/
│   └── historico_consultas.txt
├── nbproject/               # Configuração do projeto Apache NetBeans
├── build.xml                # Script de build Apache Ant
├── Diagrama_PetCare.puml    # Fonte PlantUML do diagrama de classes
├── Diagrama de Classes.png  # Diagrama de classes exportado
└── indice_dos_criterios.md  # Índice de conceitos de POO aplicados no projeto
```

## Requisitos

- **Java Development Kit (JDK) 17 ou superior** (configurado com `release="17"` e codificação `UTF-8`).
- **Apache Ant** ou **Apache NetBeans** (opcional, para usar o `build.xml`).

## Como compilar e executar

### Usando Apache Ant (ou NetBeans)

Para compilar e gerar o arquivo `dist/PetCare.jar`:

```bash
ant rebuild
```

Para executar diretamente pelo Ant:

```bash
ant run
```

Ou executando o JAR gerado:

```bash
java -jar dist/PetCare.jar
```

### Usando `javac` e `java` diretamente

No diretório raiz do projeto (`PetCare/`):

**Windows (PowerShell):**

```powershell
mkdir -Force build\classes
$files = Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName }
javac -encoding UTF-8 --release 17 -d build\classes $files
Copy-Item src\br\com\petshop\sistema\pet.png build\classes\br\com\petshop\sistema\
java -cp build\classes br.com.petshop.sistema.PetShopMain
```

**Linux / macOS:**

```bash
mkdir -p build/classes
javac -encoding UTF-8 --release 17 -d build/classes $(find src -name "*.java")
cp src/br/com/petshop/sistema/pet.png build/classes/br/com/petshop/sistema/
java -cp build/classes br.com.petshop.sistema.PetShopMain
```

### Modos de execução

`PetShopMain` aceita os seguintes parâmetros:

```text
java -cp build/classes br.com.petshop.sistema.PetShopMain [--tela|--menu|--demo|--ajuda] [caminho-do-txt]
```

- `--tela` *(padrão)*: abre a interface gráfica Swing (`PetShopTela`). Em ambientes headless (sem interface gráfica), inicia o menu no console.
- `--menu`: inicia o menu interativo no console.
- `--demo`: executa uma demonstração automática dos cadastros, consulta, serviços, venda e persistência em arquivo.
- `--ajuda`: exibe as opções de uso na linha de comando.
- `[caminho-do-txt]`: caminho opcional para o arquivo de histórico (padrão: `dados/historico_consultas.txt`).

## Documentação complementar

- [indice_dos_criterios.md](indice_dos_criterios.md) — mapeamento dos conceitos de Orientação a Objetos implementados no código.
- [Diagrama_PetCare.puml](Diagrama_PetCare.puml) e [Diagrama de Classes.png](Diagrama%20de%20Classes.png) — visão estrutural das classes e relacionamentos.
