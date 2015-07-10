package pshell.codegenerator;

import java.util.ArrayList;
import pshell.base.DeParaTipos;
import pshell.base.Utils;

public class Campo 
{
    private String nome;
    private String tipo;
    private String tipoJava;
    private boolean unsigned;
    private boolean notNull;
    private boolean autoIncremento;
    private boolean chavePrimaria;
    private Campo chaveEstrangeira;
    private String tabela;
    
    public Campo()
    {
        
    }
    
    public Campo(String tabela, ArrayList propriedades)
    {
        this.chavePrimaria = false;
        this.nome = (String) propriedades.get(0);
        this.tipo = (String) propriedades.get(1);
        this.unsigned = (propriedades.get(2).equals("UNSIGNED") ? true : false);
        this.notNull = (propriedades.get(3).equals("NOT NULL") ? true : false);
        this.autoIncremento = (propriedades.get(4).equals("AUTO_INCREMENT") ? true : false);
        
        setTipoJava();
    }
    
    private void setTipoJava()
    {
        if (tipo.indexOf("(") > 0)
            this.tipoJava = tipo.substring(0, tipo.indexOf("("));
        else
            this.tipoJava = tipo;
        tipoJava = (String) DeParaTipos.mapa.get(tipoJava);
    }
    
    public String getTipoJava()
    {
        return tipoJava;
    }
    
    public String getNomeCamelCase()
    {
        return Utils.toCamelCase(nome);
    }
    
    public String getNomeLowerCamelCase()
    {
        return Utils.toLowerCamelCase(nome);
    }

    /**
     * @return the nome
     */
    public String getNome() {
        return nome;
    }

    /**
     * @param nome the nome to set
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * @return the tipo
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * @param tipo the tipo to set
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * @return the unsigned
     */
    public boolean isUnsigned() {
        return unsigned;
    }
    
    public String getUnsigned()
    {
        return (this.unsigned ? "UNSIGNED" : "");
    }

    /**
     * @param unsigned the unsigned to set
     */
    public void setUnsigned(boolean unsigned) {
        this.unsigned = unsigned;
    }

    /**
     * @return the nulo
     */
    public boolean isNotNull() {
        return notNull;
    }
    
    public String getNotNull()
    {
        return (this.notNull ? "NOT NULL" : "");
    }

    /**
     * @param nulo the nulo to set
     */
    public void setNotNull(boolean nulo) {
        this.notNull = nulo;
    }

    /**
     * @return the autoIncremento
     */
    public boolean isAutoIncremento() {
        return autoIncremento;
    }
    
    public String getAutoIncremento()
    {
        return (this.autoIncremento ? "AUTO_INCREMENT" : "");
    }    

    /**
     * @param autoIncremento the autoIncremento to set
     */
    public void setAutoIncremento(boolean autoIncremento) {
        this.autoIncremento = autoIncremento;
    }

    /**
     * @return the chavePrimaria
     */
    public boolean isChavePrimaria() {
        return chavePrimaria;
    }

    /**
     * @param chavePrimaria the chavePrimaria to set
     */
    public void setChavePrimaria(boolean chavePrimaria) {
        this.chavePrimaria = chavePrimaria;
    }

    /**
     * @return the chaveEstrangeira
     */
    public Campo getChaveEstrangeira() {
        return chaveEstrangeira;
    }

    /**
     * @return the tabela
     */
    public String getTabela() {
        return tabela;
    }
    
    public String getTabelaCamelCase() {
        return Utils.toCamelCase(tabela);
    }
    
    public String getTabelaLowerCamelCase() {
        return Utils.toLowerCamelCase(tabela);
    }
    
    /**
     * @param tabela the tabela to set
     */
    public void setTabela(String tabela) {
        this.tabela = tabela;
    }
    
    public void setChaveEstrangeira(ArrayList propriedades)
    {
        Campo aux = new Campo();
        aux.setTabela((String) propriedades.get(1));
        aux.setNome((String) propriedades.get(2));
        this.chaveEstrangeira = aux;
    }

    @Override
    public boolean equals(Object obj) {
        return this.nome.equals(((Campo) obj).getNome());
    }
    
    public String testeCampoData()
    {
        if (tipo.indexOf("(") > 0)
            this.tipo = tipo.substring(0, tipo.indexOf("("));

        return (String) DeParaTipos.anotacao.get(this.tipo );
    }
    
}
