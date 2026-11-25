package br.com.petshop.modelo;
   
import java.util.Locale;

public class VendaProduto implements Pagavel {
    private final String produto;
    private final int quantidade;
    private final float valorUnitario;
    private Cliente cliente;
    private boolean paga;
    private int pontosGerados;
    private float valorPago;
    
    // Venda avulsa.
    public VendaProduto(String produto, int quantidade, float valorUnitario){
        this.produto = validarProduto(produto);
        this.quantidade = validarQuantidade(quantidade);
        this.valorUnitario = TabelaPrecos.validarValor(valorUnitario);
        validarTotalVenda(this.quantidade, this.valorUnitario);
        
        this.cliente = null;
        this.paga = false;
        this.pontosGerados = 0;
        this.valorPago = 0.0f;
    }
    
    // Item 5 - Agregação. A venda usa o cliente já cadastrado para creditar os pontos. A venda avulsa dispensa o cadastro.
      public VendaProduto(String produto, int quantidade, float valorUnitario, Cliente cliente){
        this.produto = validarProduto(produto);
        this.quantidade = validarQuantidade(quantidade);
        this.valorUnitario = TabelaPrecos.validarValor(valorUnitario);
        validarTotalVenda(this.quantidade, this.valorUnitario);
        
        if(cliente == null){
            throw new IllegalArgumentException("Precisar considerar o nome do cliente");
        }
        this.cliente = cliente;
        this.paga = false;
        this.pontosGerados = 0;
        this.valorPago = 0.0f;
    }
      
      public Cliente getCliente(){
          return this.cliente;
      }
      
      public boolean isPaga(){
          return this.paga;
      }
      
      public int getPontosGerados(){
          return this.pontosGerados;
      }
      
      public float getValorPago(){
          return this.valorPago;
      }      

    private static String validarProduto(String produto){
        if(produto == null || produto.trim().isEmpty()){
           throw new IllegalArgumentException("A descrição do produto é obrigatória!!");
        }        
        return produto.trim();
    }
    
    private static int validarQuantidade(int quantidade){
        if (quantidade < 1 || quantidade > 1000){
            throw new IllegalArgumentException("A quantidade digitada, tem que estar no limite determinado.");
        }
        return quantidade;
    }
    
    private static void validarTotalVenda(int quantidade, float valorUnitario){
        TabelaPrecos.validarValor(quantidade * valorUnitario);
    }
    
    private String validarPagamento(){
      return this.paga ? "Efetuado" : "Pendente";
    }

    @Override
    public void registrarPagamento() {
        if(this.paga == true){
            throw new IllegalStateException("Esse produto já foi pago");
        }
        
        float total = calcularTotal();
        if (this.cliente != null && (int) total > Integer.MAX_VALUE - this.cliente.getPontosFidelidade()) {
            throw new IllegalStateException("O limite de pontos de fidelidade foi excedido.");
        }

        this.valorPago = total;
        if (this.cliente != null) {
            // Item 7.2 - Polimorfismo de coerção (casting explícito). Só os reais inteiros de valorPago entram na contagem de pontos.
            this.pontosGerados = (int) this.valorPago;
            this.cliente.adicionarPontos(this.pontosGerados);
        }
        this.paga = true;
    }

    @Override
    public float calcularTotal() {        
        return TabelaPrecos.arredondar(this.quantidade * this.valorUnitario);
    }
    
    @Override
    public String emitirRecibo() {
        if (!this.paga) {
            throw new IllegalStateException("O pagamento ainda não foi registrado.");
        }
        Locale brasil = Locale.forLanguageTag("pt-BR");
        StringBuilder recibo = new StringBuilder();
        
        recibo.append("------------RECIBO---------------\n");
        recibo.append("Produto: ").append(this.produto).append("\n");
        recibo.append("Quantidade: ").append(this.quantidade).append("\n");
        recibo.append(this.cliente == null ? "Venda avulsa" : "Cliente: " + this.cliente.getNome()).append("\n");
        recibo.append(String.format(brasil, "Valor unitário: R$ %.2f\n", this.valorUnitario));
        recibo.append(String.format(brasil, "Valor pago: R$ %.2f\n", this.valorPago));
        recibo.append("Pontos gerados: ").append(this.pontosGerados).append("\n");
        recibo.append("Pagamento: ").append(validarPagamento()).append("\n");
        
        return recibo.toString();
    }

}
