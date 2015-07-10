package pshell.codegenerator;

import java.util.ArrayList;
import pshell.base.Utils;

public class Tabela {
    private String nome;
    private ArrayList[] propriedades;
    private String[] pks;
    private ArrayList[] fks;
    private ArrayList<Campo> campos;
    
    
    public void inicializar()
    {
        setCampos((ArrayList<Campo>) new ArrayList(propriedades.length));
        
        for (int i = 0; i < propriedades.length; i++)
        {
            Campo aux = new Campo(nome, propriedades[i]);
            getCampos().add(aux);
        }
        
        //setando as chaves primárias
        for (int i = 0; i < pks.length; i++)
        {
            for (int j = 0; j < campos.size(); j++)
                if (campos.get(j).getNome().equals(pks[i]))
                {
                    campos.get(j).setChavePrimaria(true);
                }
        }
        
        //setando as chaves extrangeiras
        for (int i = 0; i < fks.length; i++)
        {
            for (int j = 0; j < campos.size(); j++)
                if (campos.get(j).getNome().equals(fks[i].get(0)))
                {
                    campos.get(j).setChaveEstrangeira(fks[i]);
                }
        }
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
     * @return the nomeCamel
     */
    public String getNomeCamelCase() {
        return Utils.toCamelCase(nome);
    }
    
    public String getNomeLowerCamelCase() {
        return Utils.toLowerCamelCase(nome);
    }

   

    /**
     * @return the propriedades
     */
    public ArrayList[] getPropriedades() {
        return propriedades;
    }

    /**
     * @param propriedades the propriedades to set
     */
    public void setPropriedades(ArrayList[] propriedades) {
        this.propriedades = propriedades;
    }

    /**
     * @return the pks
     */
    public String[] getPks() {
        return pks;
    }
    
    public String getPksString()
    {
        if (pks.length == 1)
            return pks[0];
        else if (pks.length == 2)
            return pks[0] + ", " + pks[1];
        else if (pks.length == 3)
            return pks[0] + ", " + pks[1] + ", " + pks[2];
        else 
            return null;
    }
    
    public String getPksStringToTemplate()
    {
        if (pks.length == 1)
            return "'" + pks[0] + "'";
        else if (pks.length == 2)
            return "'" + pks[0] + "', '" + pks[1] + "'";
        else if (pks.length == 3)
            return "'" + pks[0] + "', '" + pks[1] + "', '" + pks[2] + "'";
        else 
            return null;
    }

    /**
     * @param pks the pks to set
     */
    public void setPks(String[] pks) {
        this.pks = pks;
    }
    
    public String getPks(int index)
    {
        return this.pks[index];
    }

    /**
     * @return the fks
     */
    public ArrayList[] getFks() {
        return fks;
    }

    /**
     * @param fks the fks to set
     */
    public void setFks(ArrayList[] fks) {
        this.fks = fks;
    }

    /**
     * @return the campos
     */
    public ArrayList<Campo> getCampos() {
        return campos;
    }

    /**
     * @param campos the campos to set
     */
    public void setCampos(ArrayList<Campo> campos) 
    {
        this.campos = campos;
    }
    
    public void adicionarCampo(Campo campo)
    {
        if (campos == null)
            campos = new ArrayList<Campo>();
        
        campos.add(campo);
    }
}
