/*
 * Pleno Shell (pshell)
 * ==========================
 *
 * O pshell eh um aplicativo shell de utilizacao simples, e que pode ser
 * expandido para facilitar a automacao de tarefas. Todos os comandos do shell
 * sao metodos de classes Java que podem ser acionados a partir da interface-padrao
 * deste aplicativo. Sendo assim, qualquer classe Java e seus metodos poderao ser
 * chamados via pshell.
 */
package pshell.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;


/**
 * Parser para os comandos do pshell.
 * @author Lourival Almeida
 * @version 1.4
 */
public class ShellCommandBaseClass implements CommandManager
{
    private String comando;
    private String[] parametros;
    private Class[] tipoParametros;
    private HashMap atalhos;

    public ShellCommandBaseClass()
    {
        atalhos = new HashMap();
    }
    
    public void parseComando(String str)
    {
        String[] parametrosAtalho = null;
        parametros = new String[0];
        tipoParametros = new Class[0];
        comando = null;
        int i = 0;
        
        if (setComandoStr(str))
        {
            tipoParametros = new Class[parametros.length];
            
            for (i = 0; i < parametros.length; i++)
                tipoParametros[i] = String.class;
        }
        
        //Verificando se para o comando dado existia um atalho. Em caso positivo,
        //o gerenciador de comando deve repassar para ser executado a interpretacao
        //do comando passado como parametro.
        String cmdReduzido = (String) atalhos.get(comando);
        
        if (cmdReduzido != null)
        {
            comando = cmdReduzido;
            parametrosAtalho = parametros;
            //parametros = new String[0];
            
            if (setComandoStr(cmdReduzido))
            {
                int x = 0;
                tipoParametros = new Class[parametros.length];

                for (i = 0; i < parametros.length; i++)
                {
                    tipoParametros[i] = String.class;
                    
                
                    if (parametros[i].equals("?"))
                    {
                        parametros[i] = parametrosAtalho[x];
                        x++;
                    }
                }
            }
        }
    }
    
    /*
    Quebra o comando e suas tokens em seus determinados lugares para serem processados
    */
    private boolean setComandoStr(String linhaCodigo)
    {
        String[] strSpl = linhaCodigo.trim().split("\\s+");
        
        comando = strSpl[0];
        
        if (strSpl[0].equals(""))
            return false;
        
        comando = Utils.transformarNomeMetodo(comando);
        
        parametros = new String[strSpl.length - 1];
        
        for (int i = 0; i < parametros.length; i++)
        {
            parametros[i] = strSpl[i+1];
        }
        
        return true;
    }
    
    public String getComando()
    {
        return comando;
    }

    public String[] getParametros()
    {
        return parametros;
    }
    
    public Class[] getTipoParametros()
    {
        return tipoParametros;
    }

    public void registrarAtalho(String atalho, String comando)
    {
        atalhos.put(atalho, comando);
    }
}