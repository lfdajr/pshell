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

/**
 * Interface para o que deve estar contido em um Gerenciador de Comandos.
 * @author Lourival Almeida
 * @version 1.0
 */
public interface CommandManager
{
    public void parseComando(String comando);
    public void registrarAtalho(String atalho, String comando);
}
