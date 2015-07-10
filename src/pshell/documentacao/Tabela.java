/*
 * Tabela.java
 *
 * Created on 8 de Novembro de 2008, 15:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pshell.documentacao;

import java.util.ArrayList;

/**
 *
 * @author Lourival
 */
public class Tabela
{
    
    private int id;
    private String nome;
    private String anotacao;
    private ArrayList<Coluna> colunas;
    private BancoDados bancoDados;
    
    public Tabela()
    {
        setColunas(new ArrayList());
    }
    
    public void adicionarColuna(Coluna col)
    {
        getColunas().add(col);
        col.setTabela(this);
    }

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

    public ArrayList<Coluna> getColunas()
    {
        return colunas;
    }

    public void setColunas(ArrayList<Coluna> colunas)
    {
        this.colunas = colunas;
    }

    public BancoDados getBancoDados()
    {
        return bancoDados;
    }

    public void setBancoDados(BancoDados bancoDados)
    {
        this.bancoDados = bancoDados;
    }
    
}
