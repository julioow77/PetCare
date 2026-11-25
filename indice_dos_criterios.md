# Índice dos 22 Itens Obrigatórios

| Item | Requisito | Classe(s) | Arquivo e linha |
| --- | --- | --- | --- |
| 1.1 | Encapsulamento (atributos `private`) | `Pessoa` (e demais classes) | `Pessoa.java` (linha 6) |
| 1.2 | Atributos `String`, `int` e `float` na mesma classe | `Consulta` | `Consulta.java` (linha 12) |
| 1.3 | Atributo do tipo `enum` | `Animal`, `Porte`, `Consulta`, `StatusConsulta` | `Animal.java` (linha 10) |
| 1.4 | Atributo `ArrayList` | `PetShopServico` | `PetShopServico.java` (linha 22) |
| 1.5 | Constante de tipo primitivo usada em outra classe | `TabelaPrecos`, `Consulta` | `TabelaPrecos.java` (linha 7) |
| 2.1 | Método com argumentos variados (`varargs`) e `for` | `Consulta` | `Consulta.java` (linha 75) |
| 2.2 | Método estático | `TabelaPrecos`, `Consulta` | `TabelaPrecos.java` (linha 14) |
| 3 | Construtor em pelo menos duas classes concretas | `Cliente`, `Veterinario` | `Cliente.java` (linha 8) / `Veterinario.java` (linha 7) |
| 4 | Associação por Composição | `Consulta`, `ProntuarioSimples` | `Consulta.java` (linha 54) |
| 5 | Associação por Agregação | `VendaProduto`, `Cliente` | `VendaProduto.java` (linha 27) |
| 6.1 | Atributo `final` em superclasse concreta | `Pessoa` | `Pessoa.java` (linha 9) |
| 6.2 | Acoplamento dinâmico | `Veterinario`, `VeterinarioCirurgiao`, `Consulta` | `Consulta.java` (linha 101) |
| 7.1 | Polimorfismo de sobrecarga | `Consulta` | `Consulta.java` (linha 121) |
| 7.2 | Polimorfismo de coerção explícito (casting) | `VendaProduto` | `VendaProduto.java` (linha 94) |
| 7.3 | Polimorfismo de subtipagem | `Cliente`, `Pessoa` | `Cliente.java` (linha 30) / `Pessoa.java` (linha 20) |
| 7.4 | Polimorfismo paramétrico (`Generics`) | `Exame`, `Prontuario` | `Exame.java` (linha 12) |
| 8 | Três classes implementando interface (uma com duas interfaces) | `Consulta`, `Exame`, `VendaProduto`, `Agendavel`, `Pagavel` | `Consulta.java` (linha 7) / `Exame.java` (linha 5) / `VendaProduto.java` (linha 5) |
| 9 | Dois pacotes | `br.com.petshop.modelo`, `br.com.petshop.sistema` | `PetShopMain.java` (linha 2) |
| 10 | Duas classes abstratas e suas classes concretas | `Animal` (`Cachorro`, `Gato`), `ServicoPet` (`BanhoETosa`, `Exame`) | `Animal.java` (linha 61) / `ServicoPet.java` (linha 18) |
| 11 | Exceção criada, levantada e tratada | `VacinaAtrasadaException`, `BanhoETosa`, `PetShopMain`, `PetShopTela` | `BanhoETosa.java` (linha 23) / `VacinaAtrasadaException.java` (linha 3) |
| 12 | Entrada de dados com Caixas de Diálogo (`JOptionPane`) | `PetShopMain` | `PetShopMain.java` (linha 195) |
| 13 | Gravação e leitura de arquivo | `PetShopServico`, `PetShopMain`, `PetShopTela` | `PetShopMain.java` (linha 108) / `PetShopServico.java` (linha 68) |
