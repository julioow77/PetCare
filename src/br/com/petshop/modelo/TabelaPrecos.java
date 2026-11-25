package br.com.petshop.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class TabelaPrecos { 
    // Item 1.5 - Constante primitiva. Consultas urgentes usam o mesmo adicional de R$ 35,50.
    private static final float TAXA_URGENCIA = 35.5f;
    private static final float LIMITE_VALOR = 100000.0f;

    private TabelaPrecos() {
    }
    
    // Item 2.2 - Método estático. Consulta calcula o adicional pelo indicador de urgência, sem precisar criar uma TabelaPrecos.
    public static float adicionalUrgencia(boolean urgente){
        return urgente ? TAXA_URGENCIA : 0.0f;
    }
    
    public static float validarValor(float valor){
        if(Float.isNaN(valor) || Float.isInfinite(valor)){
            throw new IllegalArgumentException("O valor deve ser finito");
        }
        if(valor < 0.0f || valor > LIMITE_VALOR){
            throw new IllegalArgumentException("O valor tem que estar entre o intervalo de R$0,00 e R$100.000,00");
        }
        
        return arredondar(valor);
    }
    
    public static float arredondar(float valor){
        if (Float.isNaN(valor) || Float.isInfinite(valor)){
            throw new IllegalArgumentException("Não é possível arredondar valores que não sejam finitos.");
        }
        
        // O preço fica com duas casas decimais.
        return new BigDecimal(Float.toString(valor)).setScale(2, RoundingMode.HALF_UP).floatValue(); 
    }
    
}
