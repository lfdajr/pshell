 /*
 * Coluna.java
 *
 * Created on 8 de Novembro de 2008, 15:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pshell.documentacao;

/**
 *
 * @author Lourival
 */
public class Coluna
{
    
    private int id;
    private String nome;
    private String anotacao;
    private String tipo;
    private int nulo;
    private String tamanho;
    private boolean autoIncremento;
    private Coluna chaveEstrangeira;
    private Tabela tabela;
    private boolean chavePrimaria;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getNome()
    {
        return nome;
    }

    public void setNome(String nome)
    {
        this.nome = nome;
    }

    public String getAnotacao()
    {
        return anotacao;
    }

    public void setAnotacao(String anotacao)
    {
        this.anotacao = anotacao;
    }

    public String getTipo()
    {
        return tipo;
    }

    public void setTipo(String tipo)
    {
        this.tipo = tipo;
    }

    public int getNulo()
    {
        return nulo;
    }

    public void setNulo(int nulo)
    {
        this.nulo = nulo;
    }

    public String getTamanho()
    {
        return tamanho;
    }

    public void setTamanho(String tamanho)
    {
        this.tamanho = tamanho;
    }

    public boolean isAutoIncremento()
    {
        return autoIncremento;
    }

    public void setAutoIncremento(boolean autoIncremento)
    {
        this.autoIncremento = autoIncremento;
    }

    public Coluna getChaveEstrangeira()
    {
        return chaveEstrangeira;
    }

    public void setChaveEstrangeira(Coluna chaveEstrangeira)
    {
        this.chaveEstrangeira = chaveEstrangeira;
    }

    public Tabela getTabela()
    {
        return tabela;
    }

    public void setTabela(Tabela tabela)
    {
        this.tabela = tabela;
    }

    public boolean isChavePrimaria()
    {
        return chavePrimaria;
    }

    public void setChavePrimaria(boolean chavePrimaria)
    {
        this.chavePrimaria = chavePrimaria;
    }
    
}
