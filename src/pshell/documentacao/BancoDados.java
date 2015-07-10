/*
 * BancoDados.java
 *
 * Created on 8 de Novembro de 2008, 15:57
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
public class BancoDados
{
    
    private int id;
    private String nome;
    private String anotacao;
    private ArrayList<Tabela> tabelas;

    public BancoDados()
    {
        setTabelas(new ArrayList());
    }
    
    public void adicionarTabela(Tabela tab)
    {
        getTabelas().add(tab);
        tab.setBancoDados(this);
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

    public ArrayList<Tabela> getTabelas()
    {
        return tabelas;
    }

    public void setTabelas(ArrayList<Tabela> tabelas)
    {
        this.tabelas = tabelas;
    }
    
}
