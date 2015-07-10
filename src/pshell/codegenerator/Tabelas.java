package pshell.codegenerator;

import java.util.Map;

public class Tabelas {
    private Tabela[] tabelas;
    
    public void inicializar()
    {
        for (Tabela t : tabelas)
            t.inicializar();
    }

    /**
     * @return the tabelas
     */
    public Tabela[] getTabelas() {
        return tabelas;
    }

    /**
     * @param tabelas the tabelas to set
     */
    public void setTabelas(Tabela[] tabelas) {
        this.tabelas = tabelas;
    }
}
