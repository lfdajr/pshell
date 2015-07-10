package pshell.base;

import java.util.HashMap;
import java.util.Map;

/*
De para entre tipos do banco de dados e tipos em java para geração de código
*/
public class DeParaTipos {
    public static Map mapa;
    public static Map anotacao;
    
    static
    {
        mapa = new HashMap();
        mapa.put("BIGINT", "Long");
        mapa.put("VARCHAR", "String");
        mapa.put("DATE", "Date");
        mapa.put("DATETIME", "Date");
        mapa.put("TIMESTAMP", "Date");
        mapa.put("INT", "Integer");
        mapa.put("INTEGER", "Integer");
        mapa.put("SMALLINT", "Integer");
        mapa.put("TINYINT", "Boolean");
        mapa.put("DECIMAL", "BigDecimal");
        mapa.put("BOOLEAN", "Boolean");
        mapa.put("TIME", "Date");
        
        anotacao = new HashMap();
        anotacao.put("DATE", "@Temporal(TemporalType.DATE)");
        anotacao.put("DATETIME", "@Temporal(TemporalType.TIMESTAMP)");
        anotacao.put("TIMESTAMP", "@Temporal(TemporalType.TIMESTAMP)");
        anotacao.put("TIME", "@Temporal(TemporalType.TIME)");

    }
}
