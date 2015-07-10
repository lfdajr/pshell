
package pshell;

import java.lang.reflect.Method;
import java.util.ArrayList;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;
import pshell.base.ShellBaseClass;
import pshell.base.ShellCommandBaseClass;

/**
 * PShell<br/>
 * ===============<br/><br/>
 *
 * O pshell é um aplicativo shell de utilização simples, e que pode ser
 * expandido para facilitar a automacao de tarefas a partir de java. Todos os comandos do shell
 * são metodos de classes Java que podem ser acionados a partir da interface-padrão
 * deste aplicativo. Sendo assim, qualquer classe Java e seus métodos ficarão expostos 
 * para serem acionados via a linha de comando.<br/><br/>
 * 
 * Para iniciar o pshell: <b>java -cp ".:PlenoShell.jar" pshell.Main</b><br/><br/>
 * 
 * Uma vez iniciado, o prompt padrão iniciará. A partir de então a classe que estiver ativa
 * terá seus métodos expostos para serem executados. Quando iniciado o pshell carrega a classe
 * pshell.base.ShellBaseClass. Para entender melhor como funciona, experimente o primeiro
 * comando. 'pwd' exibe a classe que está carregada no momento.<br/>
 * Abaixo você poderá verificar os principais comandos disponíveis pela classe básica
 * do pshell.
 * 
 * @see pshell.base.ShellBaseClass
 */
public class Main {

    /**
     * Este shell serve para automatização de processos baseados em código java.
     * O que for passado no shell como parâmetro será procurado na classe exibida a partir do comando
     * 'pwd' o método correspondente para ser executado.
     * @param args argumentos da linha de comando.
     */
    public static void main(String[] args) {
        ShellCommandBaseClass cmd = new ShellCommandBaseClass();
        //ShellBaseClass main = new ShellDBBaseClass();
        //ShellBaseClass main = new ShellDBDocumentacao();
        ShellBaseClass main = new ShellBaseClass();
        main.setCommandManager(cmd);

        String comando = null;

        while (true) {
            try {

                ConsoleReader console = main.getConsole();
                console.setPrompt(":-> ");

                //console.addCompleter(new FileNameCompleter());
                comando = console.readLine();
                cmd.parseComando(comando);
                Method m = main.getInstancia().getClass().getMethod(cmd.getComando(), cmd.getTipoParametros());
                m.invoke(main.getInstancia(), cmd.getParametros());

            } catch (Exception ex) {
                //System.out.println("ERRO: Comando inexistente ou parâmetros inválidos.");
                ex.printStackTrace();
            } finally {
                /*try {
                    TerminalFactory.get().restore();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            }
        }
    }

}
