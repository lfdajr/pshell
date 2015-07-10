package pshell.base;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import pshell.codegenerator.Campo;
import pshell.codegenerator.Tabelas;
import pshell.codegenerator.Tabela;

/**
 * Classe básica para geração de código fonte com Apache Velocity
 * @author 99282895491
 */

public class ShellCodeGeneratorClass extends ShellDBBaseClass
{
    public ShellCodeGeneratorClass ()
    {
        super();        
    }
    
    @Override
    protected void montarHelpBuffer() {
        super.montarHelpBuffer(); //To change body of generated methods, choose Tools | Templates.
        getHelpBuffer().put("gerar-arquivo", "Gera o arquivo pedido a partir de um template Velocity");
        getHelpBuffer().put("gerar-codigo-completo", "Gera script de banco de dados e classes");
        getHelpBuffer().put("engenharia-reversa", "Gera o script de configuração a partir de um database criado.\n" +
                "Deve estar conectado a um banco primeiramente. Vide <conectar>");
    }
    
    /*
    Cria um gerador de arquivo genérico para qualquer VM que seja passado
    A string arquiva pedida como parametro deve ser informado apenas o arquivo 
    sem extensão. Ex.: tabelas, será utilizado o arquivo tabelas.vm com o template
    e tabelas.txt com o conteúdo.
    */

    /**
     * Gera um arquivo a partir do nome de arquivo 
     * @throws Exception
     */
    
    public void gerarArquivo(String arquivo) throws Exception
    {
        File arquivoConteudo = new File(arquivo);
        File arquivoTemplate = new File(arquivo.substring(0, arquivo.indexOf(".")) + ".vm");
        File arquivoSaida = new File(arquivo.substring(0, arquivo.indexOf(".")) + "_" + new Date().getTime() + ".out");
        
        
        if (!arquivoConteudo.exists() || !arquivoTemplate.exists())
        {
            println("Forneça um caminho de arquivo de conteúdo a partir de " + System.getProperty("user.dir") + "/");
            println("O arquivo de template será o mesmo nome do arquivo anterior com extensão .vm");
            return ;
        }
        else
        {
            println("Arquivo conteúdo encontrado: " + arquivoConteudo.getAbsolutePath());
            println("Arquivo template encontrado: " + arquivoTemplate.getAbsolutePath());
        }
        
        Gson gson = new Gson();
        Map<String,String> map = new HashMap<String,String>();
        map = (Map<String,String>) gson.fromJson(Utils.lerArquivo(arquivoConteudo.getPath()), map.getClass());

        Velocity.init();

        VelocityContext context = new VelocityContext();

        context.put("obj", map);

        Template template = null;
        

        try 
        {
            template = Velocity.getTemplate(arquivoTemplate.getPath());
        } 
        catch (ResourceNotFoundException rnfe) {
        } 
        catch (ParseErrorException pee) {
        }
        catch (MethodInvocationException mie) {
        } catch (Exception e) {
        }

        FileWriter w = new FileWriter(arquivoSaida.getPath());

        template.merge(context, w);
        
        w.close();

        println("Arquivo criado: " + arquivoSaida.getPath());
    }
    
    private void gerarCodigo(String templatesDiretorio, String pack) throws Exception
    {
        
        String diretorio = pack.replace(".", System.getProperty("file.separator"));
        //String arquivo = lerTeclado("Informe o arquivo : ");
        
        File diretorioPadrao = new File(templatesDiretorio);
        File[] arquivos = diretorioPadrao.listFiles();
        ArrayList<Tabela> tabelasArrayList = new ArrayList<Tabela>();
        
        new File((diretorio)).mkdirs();
        new File((diretorio + "/dominio")).mkdirs();
        new File((diretorio + "/sql")).mkdirs();
        Velocity.init();
        VelocityContext context = new VelocityContext();
        Template template = null;
        
        for (File arqCont : arquivos)
        {
            if (arqCont.getName().endsWith(".txt"))
            {
                Gson gson = new Gson();
                Tabela t = (Tabela) gson.fromJson(Utils.lerArquivo(System.getProperty("user.dir") + "/" + templatesDiretorio + "/"+ arqCont.getName()), Tabela.class);
                t.inicializar();

                context.put("tabela", t);

                try 
                {
                    template = Velocity.getTemplate(templatesDiretorio + "/tabelas.vm");
                } 
                catch (ResourceNotFoundException rnfe) {
                } 
                catch (ParseErrorException pee) {
                }
                catch (MethodInvocationException mie) {
                } catch (Exception e) {
                }

                String nomeArquivo = diretorio + "/sql/" + t.getNome() + ".sql";
                
                FileWriter w = new FileWriter(nomeArquivo);

                template.merge(context, w);

                w.close();
                println("Arquivo criado: " + nomeArquivo);
                
                tabelasArrayList.add(t);
            }
        }
                
        Tabela[] tabelas = tabelasArrayList.toArray(new Tabela[0]);
        
        for (Tabela i : tabelas)
        {
        
            context = new VelocityContext();

            context.put("tabela", i);
            context.put("pack", pack);

            try 
            {
                template = Velocity.getTemplate(templatesDiretorio + "/classes.vm");
            } 
            catch (ResourceNotFoundException rnfe) {
            } 
            catch (ParseErrorException pee) {
            }
            catch (MethodInvocationException mie) {
            } catch (Exception e) {
            }

            String nomeArquivo = diretorio + "/dominio/" + i.getNomeCamelCase() + ".java";
            FileWriter w = new FileWriter(nomeArquivo);

            template.merge(context, w);

            w.close();
            
            println("Arquivo criado: " + nomeArquivo);
        }

        //println("Acabou");
    }    
    
    /**
     *
     * @throws Exception
     */
    public void gerarCodigoCompleto() throws Exception
    {

        String diretorio = lerTeclado("Informe o endereço dos templates. <ENTER> = templates/01 : ");
        String pack = lerTeclado("Informe o diretório base. <ENTER> =  br.com.app : ");
        
        if (diretorio.equals(""))
            diretorio = "templates/01";
        if (pack.equals(""))
            pack = "br.com.app";
        
        gerarCodigo(diretorio, pack);
    }


    
    
    
    /**
     *
     * @throws Exception
     */
    public void teste() throws Exception
    {
        println(System.getProperty("user.dir"));
        Gson gson = new Gson();
        Tabelas map = new Tabelas();
        map = (Tabelas) gson.fromJson(Utils.lerArquivo("/home/99282895491/projetos/pshell/templates/01/config.txt"), Tabelas.class);
        
        println("aqui");
    }
    
    /**
     *
     * @throws SQLException
     * @throws Exception
     */
    public void engenhariaReversa() throws SQLException, Exception
    {
        if (con == null)
        {
            println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("conexao.nenhuma"));
            return;
        }
        
        String diretorio = lerTeclado("Informe o endereço dos templates. <ENTER> = templates/01 : ");
        if (diretorio.equals(""))
            diretorio = "templates/01";
        
        ArrayList<String> tabelas = new ArrayList<String>();
        String[] types =
        {
            "TABLE"
        };
        DatabaseMetaData rsmd = con.getMetaData();
        ResultSet rs = rsmd.getTables(null, null, "%", types);
        while (rs.next())
        {
            String table = rs.getString("TABLE_NAME");
            tabelas.add(table);
        }
        
        if (rs != null)
        {
            rs.close();
        }
        
        Velocity.init();
        VelocityContext context = null;
        Template template = null;
        
        for (String str : tabelas)
        {
            context = new VelocityContext();
            Tabela novaTabela = detalheTabela(str);
            
            context.put("tabela", novaTabela);

            try 
            {
                template = Velocity.getTemplate(diretorio + "/config_template.vm");
            } 
            catch (ResourceNotFoundException rnfe) {
            } 
            catch (ParseErrorException pee) {
            }
            catch (MethodInvocationException mie) {
            } catch (Exception e) {
            }

            String nomeArquivo = diretorio + "/" + novaTabela.getNome() + ".txt";
            FileWriter w = new FileWriter(nomeArquivo);

            template.merge(context, w);

            w.close();

            println("Arquivo criado: " + nomeArquivo);
        }
    }
    
    private Tabela detalheTabela(String tabela) throws SQLException
    {
        Statement stmt = null;
        ResultSet rs = null;
        Tabela res = new Tabela();
        res.setNome(tabela);

        try
        {
            String SQL = "SELECT * FROM " + tabela;
            stmt = con.createStatement();
            stmt.setMaxRows(1);

            rs = stmt.executeQuery(SQL);
            ResultSetMetaData rsmd = rs.getMetaData();
            ResultSet chavesPrimarias = con.getMetaData().getPrimaryKeys(null, null, tabela);
            int cols = rsmd.getColumnCount();

            for (int i = 1; i <= cols; i++)
            {
                Campo cp = new Campo();
                cp.setAutoIncremento(rsmd.isAutoIncrement(i));
                
                cp.setNome(rsmd.getColumnName(i));
                cp.setTipo(rsmd.getColumnTypeName(i) + "("  + rsmd.getPrecision(i) + ")");
                
                if (rsmd.isNullable(i) == 1)
                    cp.setNotNull(false);
                else
                    cp.setNotNull(true);
                
                cp.setChavePrimaria(false);
                
                res.adicionarCampo(cp);
            }
            
            ArrayList<String> chavesAux = new ArrayList<String>();
            int i = 0;
            
            while (chavesPrimarias.next())
            {
                String aux = chavesPrimarias.getString("COLUMN_NAME");
                Campo c = new Campo();
                c.setNome(aux);
                if (res.getCampos().contains(c))
                {
                    res.getCampos().get(i).setChavePrimaria(true);
                    chavesAux.add(aux);
                    i++;
                }
            }
            
            res.setPks((String[])chavesAux.toArray(new String[0]));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (rs != null)
        {
            rs.close();
        }
        if (stmt != null)
        {
            stmt.close();
        }
        
        DatabaseMetaData rsmd = con.getMetaData();
        rs = rsmd.getImportedKeys(null, null, tabela);
        ArrayList<ArrayList> chavesEstrangeiras = new ArrayList<ArrayList>();
        
        while (rs.next())
        {
            ArrayList<String> chaveEstrangeira = new ArrayList<String>();
            chaveEstrangeira.add(rs.getString("FKCOLUMN_NAME"));
            chaveEstrangeira.add(rs.getString("PKTABLE_NAME"));
            chaveEstrangeira.add(rs.getString("PKCOLUMN_NAME"));
            chavesEstrangeiras.add(chaveEstrangeira);
        }
        
        res.setFks(chavesEstrangeiras.toArray(new ArrayList[0]));

        if (rs != null)
        {
            rs.close();
        }
        
        
        return res;
    }    
    
}
