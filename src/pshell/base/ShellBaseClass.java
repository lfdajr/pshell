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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;
import pshell.GeradorEntidade;

/**
 * Classe principal do pshell. Possui a estrutura básica de métodos
 * a serem utilizados pelas classes que serão chamadas. Esta classe fornece
 * alguns métodos básicos que já estão disponíveis na linha de comando.
 * @author <B>Lourival Almeida</B>
 * @version 1.3
 */
public class ShellBaseClass
{
    /** Instancia em uso da classe*/
    private static Object instancia = null;
    
    private BufferedReader br = null;
    
    private HashMap atalhos;

    private CommandManager commandManager;
    
    private HashMap helpBuffer;
    
    private static ConsoleReader console;
    
    protected String[] palavras;
    
    /**
     * Construtor da classe.
     */
    public ShellBaseClass()
    {
        //println("ShellBaseClass");
        instancia = this;
        //this.br = new BufferedReader(new InputStreamReader(System.in));
        atalhos = new HashMap();
        helpBuffer = new HashMap();
        
        //palavras = new String[]{"sair", "help", "pwd"};
        
        montarHelpBuffer();
        
        inicializarPalavras();
    }
    
    protected void montarHelpBuffer()
    {
        helpBuffer.put("help", "Exibe o help de um determinado comando");
        helpBuffer.put("sair", "Encerra o shel");
        helpBuffer.put("set", "Seta um atalho para um determinado comando. Possui uma versão que pode ser utilizada passando "
                + "dois parâmetros, <link> e <comando>");
        helpBuffer.put("use", "Mostra alguns atalhos rápidos do shell. Se for passado uma classe como parâmetro, o shell"
                + " carregará as informações");
        helpBuffer.put("versao", "Exibe a versão do shell");        
    }
    
    protected void inicializarPalavras()
    {
        palavras = new String[helpBuffer.keySet().size()];
        int i = 0;
        for (Object chave : helpBuffer.keySet())
        {
            palavras[i] = ((String)chave);
            i++;
        }
    }
    
    protected void setConsole(ConsoleReader console)
    {
        this.console = console;
    }
    
    public ConsoleReader getConsole() throws Exception
    {
        if (console == null)
        {
            console = new ConsoleReader();
            initiateCodeCompletionRules();
        }
        
        return console;
    }
    
    protected void cleanHelpBuffer()
    {
        helpBuffer.clear();
    }
    
    protected void println(String mensagem) throws Exception
    {
        getConsole().println(mensagem);
    }
    
    protected void cleanCodeCompletionRules() throws Exception
    {
        for (Completer c : getConsole().getCompleters())
            getConsole().removeCompleter(c);
    }
    
    protected void initiateCodeCompletionRules()
    {
        StringsCompleter sc = new StringsCompleter(palavras);
        console.addCompleter(new ArgumentCompleter (sc, new FileNameCompleter()));
        //console.addCompleter(sc);
    }
    
    /**
     * Retorna qual a classe que está sendo utilizada no shell em um determinado momento.
     * @return Classe em utilização no shell.
     */
    public Object getInstancia()
    {
        return instancia;
    }
    
    /**
     * Abre uma classe para execução de comandos no pshell.
     * @param classe nome completo da classe a ser utilizada. Ex.: pshell.base.ShellDBUtils
     * @throws java.lang.ClassNotFoundException 
     * @throws java.lang.InstantiationException 
     * @throws java.lang.IllegalAccessException 
     */
    public final void use(String classe) throws Exception
    {
        try
        {
            instancia = Class.forName(classe).newInstance();
            return ;
        }
        catch (Exception e)
        {
        }
        
        try
        {
            instancia = Class.forName(getConfigFile().getString("cmd.use." + classe)).newInstance();
            return ;
        }
        catch (Exception e)
        {
            println("Classe informada como parâmetro não foi encontrada.");
        }        
        
    }
    
    public final void use() throws IOException, Exception
    {
        println("Atalho 1: " + getConfigFile().getString("cmd.use.1"));
        println("Atalho 2: " + getConfigFile().getString("cmd.use.2"));
        println("Atalho 3: " + getConfigFile().getString("cmd.use.3"));
        println("Atalho 4: " + getConfigFile().getString("cmd.use.4"));
        println("Atalho 5: " + getConfigFile().getString("cmd.use.5"));
        println("Atalho 6: " + getConfigFile().getString("cmd.use.6"));
        println("Atalho 7: " + getConfigFile().getString("cmd.use.7"));
        println("Atalho 8: " + getConfigFile().getString("cmd.use.8"));
        println("Atalho 9: " + getConfigFile().getString("cmd.use.9"));
    }
    
    /**
     * Abre no pshell uma entrada de dados para o usuário.
     * @param mensagem mensagem que será exibida para o usuário antes da entrada de dados.
     * @throws java.io.IOException 
     * @return 
     */
    public String lerTeclado(String mensagem) throws IOException, Exception
    {
        //ShellBaseClass.getConsole().print(mensagem);
        //ShellBaseClass.getConsole().setPrompt("");
        String teste = getConsole().readLine(mensagem);
        
        return teste;
    }
    
    /**
     * Encerra o pshell.
     */
    public void sair() throws Exception
    {
        println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("pshell.sair"));
        System.exit(0);
    }    
    
    /**
     * Exibe todos os métodos disponíveis em uma classe para que o usuário
     * possa utilizar no pshell.
     */
    public void help() throws Exception
    {
        println("Exibindo todos os comandos disponíveis para classe carregada:\n");
        for (String pal : palavras)
        {
            println(" - " + pal);
        }
        println("\nPara saber detalhes de utilização dos comandos: help <comando>");
    }
    
    /**
     * Exibe o help disponível na classe que está em utilização.
     */
    public void help(String comando) throws Exception
    {
        println("HELP - " + comando);
        println("=====================");
        println("");
        println((String) helpBuffer.get(comando.trim()));
    }    
    
    /**
     * Exibe a versão em utilização do pshell.
     */
    public final void versao() throws Exception
    {
        println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("pshell.versao"));
    }
    
    /**
     * Seta um atalho (link) para um comando.
     * Com este método o usuário pode criar um "apelido" para um comando muito utilizado.
     * Ex.:<br>
     * <B> Comando:</B> set load [use pshell.base.ShellDBBaseClass]<br>
     * <B> Resultado:</B> Cria um atalho chamado "load" para o comando "use pshell.base.ShellDBBaseClass".<br>
     * O atalho criado poderá ser chamado no pshell como se fosse um método existente da classe.
     * Também é possível utilizar parâmetros nos atalhos criados. Ex.:<br>
     * <B> Comando:</B> set con [conectar ?]<br>
     * <B> Resultado:</B> Cria um atalho que possui um parâmetro. Sempre que o atalho for acionado deverá
     * ser passado mais este parâmetro para o pshell. Neste caso, por exemplo, poderia ser chamado
     * "con mysql" que seria transformado em "conectar mysql".
     * @param link nome do atalho a ser criado.
     * @param comando comando a ser associado ao atalho. Este comando deve estar entre "[" e "]".
     */
    public void set(String link, String comando)
    {
        atalhos.put(link, comando);
        commandManager.registrarAtalho(link, comando);
    }
    
    /**
     * Exibe todos os atalhos existentes para o pshell em um dado momento.
     */
    public void set() throws Exception
    {
        println(atalhos.toString());
    }
    
    /**
     * Retorna o arquivo de configuração onde estão mapeadas as propriedades 
     * das bases de dados que poderao ser utilizadas.
     */
    protected ResourceBundle getConfigFile() throws Exception
    {
        File arquivo = new File("pshell.properties");
        
        if (!arquivo.exists())
        {
            try
            {
                InputStream inputStream = getClass().getResourceAsStream("/pshell/base/default.properties");
                
                FileWriter outputStream = new FileWriter("pshell.properties");
                
                int c;
                while ((c = inputStream.read()) != -1)
                    outputStream.write(c);

                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } 
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
            
            println("Arquivo pshell.properties criado no diretorio " + System.getProperty("user.dir"));
        }
        
        try
        {
            ResourceBundle.clearCache();
            return ResourceBundle.getBundle("pshell");
        }
        catch (Exception e)
        {
            println("Arquivo não encontrado");
        }
        
        return null;
    }

    /**
     * Seta o Gerenciador de Comando associado a esta classe.
     * @param cmd Objeto que representa o Gerenciador de Comando.
     */
    public void setCommandManager(CommandManager cmd)
    {
        this.commandManager = cmd;
    }
    
    protected HashMap getHelpBuffer()
    {
        return helpBuffer;
    }
    
    public void charsetDisponiveis() throws Exception
    {
        //Charset charset = Charset.forName("ISO-8859-1");
        Map map = Charset.availableCharsets();
        Iterator it = map.keySet().iterator();
        while (it.hasNext()) 
        {
            String charsetName = (String)it.next();
            println(charsetName);
        }
        
        println("Charset em utilização pela JVM: " + System.getProperty("file.encoding"));
    }
    
    /**
     * @exclude
     * @param valor
     * @param charset
     * @return
     * @throws UnsupportedEncodingException 
     */
    protected String converte(String valor, String charset) 
        throws UnsupportedEncodingException
    {
        if (valor == null)
            return null;
        
        byte[] saida = valor.getBytes(charset);
        return new String(saida, 0, saida.length);
    }
    
    /*
    Informa a classe que está sendo utilizada no momento
    */
    public void pwd() throws Exception
    {
        println("  - " + instancia.getClass().getName());
    }
    
    /**
     * Exibe as informações de diretório de trabalho da aplicação para o usuário.
     * <br/><br/>
     * Utilização: > info 
     * @throws Exception 
     */
    public void info() throws Exception
    {
        println("user.dir: " + System.getProperty("user.dir"));
        println("user.home: " + System.getProperty("user.home"));
        println("user.name: " + System.getProperty("user.name"));
    }
    
    public void tela()
    {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GeradorEntidade().setVisible(true);
            }
        });
    }
    
}