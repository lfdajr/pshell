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
package pshell.documentacao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import pshell.base.ShellDBBaseClass;


/**
 * Classe responsável por criar a documentação de uma base de dados.
 * @author Lourival Almeida
 * @version 1.1
 */
public class ShellDBDocumentacao extends ShellDBBaseClass
{
    private int databaseID;
    
    public ShellDBDocumentacao()
    {
        super();
    }
    
    @Override
    protected void montarHelpBuffer() {
        super.montarHelpBuffer(); //To change body of generated methods, choose Tools | Templates.
        getHelpBuffer().put("buscar-info","buscar-info <banco> <tabela> <coluna>\n");
        getHelpBuffer().put("gerar-base-documentacao","Gerar base de documentação\n");
        getHelpBuffer().put("exportar","exportar <database> <arquivo saida>\n");
        getHelpBuffer().put("exportar-por-tabela","exportarPorTabela <database> <diretorio>\n");
        getHelpBuffer().put("inserir-info","inserirInfo <banco> <tabela> <coluna> <informacao>\n");
    }
    
    /**
     * Coloca as informaçẽss da estrutura do banco de dados ao qual o usuário está conectado em um outro banco de dados.
     * O novo banco gerado servirá para documentação (dicionário de dados).
     */
    public void gerarBaseDocumentacao() throws Exception
    {
        String tabDoc = lerTeclado("Deseja documentar as tabelas (sim/nao): ");
        String docProc = lerTeclado("Deseja documentar as procedures (sim/nao): ");
        
        Connection conexaoLocal = null;
       
        ResourceBundle rb = getConfigFile();
        
        if (rb == null)
            return ;
        
        try
        {
            Class.forName(rb.getString("banco.documentacao.drivername"));
            conexaoLocal = DriverManager.getConnection(rb.getString("banco.documentacao.connectionUrl"), rb.getString("banco.documentacao.username"), rb.getString("banco.documentacao.password"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ;
        }
        
        try
        {
            if (tabDoc.equals("sim"))
            {
                System.out.println("Documentando tabelas...");
                documentarTabelas(conexaoLocal);
                System.out.println("Documentando stored procedures...");
                documentarChavesEstrangeiras(conexaoLocal);
            }
            else
                System.out.println("Documentacao de tabelas nao serah gerada.");
            
            if (docProc.equals("sim"))
            {
                System.out.println("Documentacao gerada com sucesso!!!");            
                documentarProcedures(conexaoLocal);
            }
            else
                System.out.println("Documentacao de procedures nao serah gerada.");
            
            
            System.out.println("Documentando chaves estrangeiras...");

            
            if (conexaoLocal != null) conexaoLocal.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }
    
    /**
     * Busca o ID de uma coluna a partir do nome da coluna e do nome da tabela que a contém.
     */
    private int buscarColunaID(String tabela, String coluna, Connection conexao) throws SQLException
    {
        String sql = "SELECT C.ID FROM TABELA as T, COLUNA as C WHERE T.NOME like ? AND C.NOME like ? AND T.ID = C.TABELA_ID";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, tabela);
        ps.setString(2, coluna);
        ResultSet rs = ps.executeQuery();
        rs.next();        
        int id = -1;
        
        try
        {
            id = rs.getInt(1);
        }
        catch(Exception e)
        {
            System.out.println("ERRO! " + tabela + "." + coluna);
        }
        
        rs.close();
        ps.close();
        
        return id;
    }
    
    private void documentarChavesEstrangeiras(Connection bancoDocumentacao) throws SQLException
    {
        int colunaPK, colunaFK;
        int contador = 0;

        //Buscando todas as tabelas da base de dados
        String sql = "SELECT NOME FROM TABELA WHERE BANCO_DADOS_ID = ?";
        PreparedStatement ps = bancoDocumentacao.prepareStatement(sql);
        ps.setInt(1, databaseID);
        ResultSet rsTodasTabelas = ps.executeQuery();
        
        String sqlUpdt = "UPDATE COLUNA SET CHAVE_ESTRANGEIRA = ? WHERE ID = ?";
        PreparedStatement psUpdt = bancoDocumentacao.prepareStatement(sqlUpdt);
        
        while (rsTodasTabelas.next())
        {
            String tabela = rsTodasTabelas.getString("NOME");
        
            DatabaseMetaData rsmd = con.getMetaData();
            ResultSet rs = rsmd.getImportedKeys(null, null, tabela);
            
            while (rs.next())
            {
                colunaPK = buscarColunaID(rs.getString("PKTABLE_NAME"), rs.getString("PKCOLUMN_NAME"), bancoDocumentacao);
                colunaFK = buscarColunaID(rs.getString("FKTABLE_NAME"), rs.getString("FKCOLUMN_NAME"), bancoDocumentacao);
                
                psUpdt.setInt(1, colunaPK);
                psUpdt.setInt(2, colunaFK);
                psUpdt.execute();
                
                System.out.println("Chave Estrangeira #" + contador++);
            }

            if (rs != null) rs.close();
        }
        
        if (psUpdt != null) psUpdt.close();
        if (rsTodasTabelas != null) rsTodasTabelas.close();
        if (ps != null) ps.close();
    }
    
    private void documentarProcedures(Connection conexaoLocal)
    {
        int contador = 0;
        try
        {
            ResultSet rs = null, rs2 = null, keys = null;
            PreparedStatement cs = null, stmt = null;
            
            String sql = "INSERT INTO STORED_PROCEDURES (BANCO_DADOS_ID, NOME) VALUES (?, ?)";
            cs = conexaoLocal.prepareStatement(sql);
            DatabaseMetaData  rsmd = con.getMetaData();
            rs = rsmd.getProcedures(null, null, null);
            
            while (rs.next())
            {
                cs.setInt(1, databaseID);
                cs.setString(2, rs.getString("PROCEDURE_NAME"));
                cs.executeUpdate();
                
                System.out.println("Procedure #" + contador++);
            }
            
            
            if (keys != null) keys.close();
            if (rs != null) rs.close();
            if (rs2 != null) rs2.close();
            if (cs != null) cs.close();
            if (stmt != null) stmt.close();
            
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }        
    }
    
    private void documentarTabelas(Connection conexaoLocal)
    {
        int contador = 0;
        try
        {
            ResultSet rs = null, rs2 = null, keys = null;
            PreparedStatement cs = null, stmt = null;
            //Documentando as tabelas
            String sql = "INSERT INTO BANCO_DADOS (NOME) VALUES (?)";
            cs = conexaoLocal.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            cs.setString(1, con.getCatalog());
            cs.executeUpdate();
            keys = cs.getGeneratedKeys();
            keys.next();
            databaseID = keys.getInt(1);
            
            System.out.println("DatabaseID: " + databaseID);
            
            //Buscando as tabelas da base de dados e inserindo as tabelas
            sql = "INSERT INTO TABELA (BANCO_DADOS_ID, NOME) VALUES (?, ?)";
            cs = conexaoLocal.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            
            String[] types = {"TABLE"};
            DatabaseMetaData  rsmd = con.getMetaData();
            rs = rsmd.getTables(null,null,"%",types);
                        
            while (rs.next())
            {
                try
                {
                    cs.setInt(1, databaseID);
                    cs.setString(2, rs.getString("TABLE_NAME"));
                    cs.executeUpdate();

                    keys = cs.getGeneratedKeys();
                    keys.next();
                    int tabelaID = keys.getInt(1);

                    System.out.println("Tabela #" + contador++);

                    //Buscando as colunas da tabela
                    String sql2 = "SELECT * FROM " + rs.getString("TABLE_NAME");
                    stmt = con.prepareStatement(sql2);
                    stmt.setMaxRows(1);
                    rs2 = stmt.executeQuery();
                    ResultSetMetaData rsmd2 = rs2.getMetaData();
                    int cols = rsmd2.getColumnCount();
                    ResultSet chavesPrimarias = con.getMetaData().getPrimaryKeys(null, null, rs.getString("TABLE_NAME"));
                    ArrayList chavesPrimariasArray = new ArrayList();
                    
                    while (chavesPrimarias.next())
                        chavesPrimariasArray.add(chavesPrimarias.getString("COLUMN_NAME"));
                    
                    chavesPrimarias.close();
                    

                    sql2 = "INSERT INTO COLUNA (TABELA_ID, NOME, TIPO, NULO, TAMANHO, AUTO_INCREMENTO, CHAVE_PRIMARIA) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    stmt = conexaoLocal.prepareStatement(sql2);

                    for (int i =0; i < cols; i++)
                    {
                        stmt.setInt(1, tabelaID);
                        stmt.setString(2, rsmd2.getColumnName(i + 1));
                        stmt.setString(3, rsmd2.getColumnTypeName(i + 1));
                        stmt.setInt(4, rsmd2.isNullable(i + 1));
                        stmt.setString(5, "" + rsmd2.getPrecision(i + 1));
                        stmt.setBoolean(6, rsmd2.isAutoIncrement(i + 1));

                        if (chavesPrimariasArray.contains(rsmd2.getColumnName(i + 1)))
                            stmt.setBoolean(7, true);
                        else
                            stmt.setBoolean(7, false);
                        
                        stmt.execute();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            if (rs != null) rs.close();
            if (cs != null) cs.close();
            if (stmt != null) stmt.close();
            if (rs2 != null) rs2.close();    
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * Coloca informações sobre uma determinada coluna na base de dados de documentação
     * de um outro banco de dados. Essa base de documentação deve ter sido criada pelo
     * comando documentar.
     */
    public void inserirInfo(String banco, String tabela, String coluna, String informacao)
        throws SQLException
    {
        String sql = "SELECT c.id FROM banco_dados as bd, tabela as t, coluna as c " +
                "WHERE bd.nome like ? and bd.id = t.banco_dados_id and " +
                "t.nome like ? and t.id = c.tabela_id and c.nome like ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, banco);
        ps.setString(2, tabela);
        ps.setString(3, coluna);
        ResultSet rs = ps.executeQuery();
        rs.next();
        int colunaID = rs.getInt(1);
        
        sql = "UPDATE COLUNA SET ANOTACAO = ? WHERE ID = ?";
        ps = con.prepareStatement(sql);
        ps.setString(1, informacao);
        ps.setInt(2, colunaID);
        ps.execute();
        
        if (rs != null) rs.close();
        if (ps != null) ps.close();
    }
    
    /**
     * Coloca informações sobre uma determinada tabela na base de dados de documentação
     * de um outro banco de dados. Essa base de documentação deve ter sido criada pelo
     * comando gerarBaseDocumentacao.
     */
    public void inserirInfo(String banco, String tabela, String informacao)
        throws SQLException
    {
        String sql = "SELECT t.id FROM banco_dados as bd, tabela as t " +
                "WHERE bd.nome like ? and bd.id = t.banco_dados_id and " +
                "t.nome like ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, banco);
        ps.setString(2, tabela);
        ResultSet rs = ps.executeQuery();
        rs.next();
        int tabelaID = rs.getInt(1);
        
        sql = "UPDATE TABELA SET ANOTACAO = ? WHERE ID = ?";
        ps = con.prepareStatement(sql);
        ps.setString(1, informacao);
        ps.setInt(2, tabelaID);
        ps.execute();
        
        if (rs != null) rs.close();
        if (ps != null) ps.close();
    }    
    
    /**
     * Retorna informações de uma coluna específica de uma tabela que possua informações
     * de documentação na base de dados.
     * @param banco nome do banco de dados que a coluna pertence.
     * @param tabela nome da tabela que a coluna pertence.
     * @param coluna nome da coluna que se busca informações.
     * @throws java.sql.SQLException
     */
    public void buscarInfo(String banco, String tabela, String coluna)
    throws SQLException
    {
        String sql = "SELECT c.id, c.nome, c.tipo, c.tamanho, c.nulo, c.anotacao FROM banco_dados as bd, tabela as t, coluna as c " +
                "WHERE bd.nome like ? and bd.id = t.banco_dados_id and " +
                "t.nome like ? and t.id = c.tabela_id and c.nome like ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, banco);
        ps.setString(2, tabela);
        ps.setString(3, coluna);
        ResultSet rs = ps.executeQuery();
        rs.next();
        System.out.println("ID, NOME, TIPO, TAMANHO, NULO, ANOTACAO");
        System.out.println("-----------------------------------------------");
        System.out.print(rs.getInt(1) + ", ");
        System.out.print(rs.getString(2) + ", ");
        System.out.print(rs.getString(3) + ", ");
        System.out.print(rs.getString(4) + ", ");
        System.out.println("" + rs.getInt(5));
        System.out.println("" + rs.getString(6));
        
        if (rs != null) rs.close();
        if (ps != null) ps.close();
        
    }
    
    /**
     * Exporta a estrutura da base de dados gerada previamente para um modelo
     * que pode ser aberto na ferramenta Freemind.
     * @param database nome do banco de dados que será exportado.
     * @param saida nome do arquivo de saída que será gerado.
     */
    public void exportar(String database, String saida) throws Exception
    {
        //Properties p = new Properties();
        //p.setProperty("file.resource.loader.path", "pshell/base");
        //Velocity.init(p);
        Velocity.init();
        
        //Buscando informacoes sobre o banco de dados
        String sql = "SELECT ID, NOME, ANOTACAO FROM BANCO_DADOS WHERE NOME LIKE ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, database);
        ResultSet rs = ps.executeQuery();
        
        BancoDados bd = new BancoDados();
        rs.next();
        bd.setId(rs.getInt("ID"));
        bd.setNome(rs.getString("NOME"));
        bd.setAnotacao(rs.getString("ANOTACAO"));
        
        rs.close();
        ps.close();
        
        //Buscando informacoes das tabelas
        sql = "SELECT ID, NOME, ANOTACAO FROM TABELA WHERE BANCO_DADOS_ID = ?";
        ps = con.prepareStatement(sql);
        ps.setInt(1, bd.getId());
        rs = ps.executeQuery();
        
        HashMap ids = new HashMap(); //Combina o ID da chave estrangeira com o id da coluna
        HashMap idColuna = new HashMap(); //Combina o ID da coluna com o objeto que representa esta coluna
        while (rs.next())
        {
            Tabela t = new Tabela();
            t.setId(rs.getInt("ID"));
            t.setNome(rs.getString("NOME"));
            t.setAnotacao(rs.getString("ANOTACAO"));
            
            bd.adicionarTabela(t);
            
            String sqlColuna = "SELECT ID, NOME, ANOTACAO, TIPO, TAMANHO, AUTO_INCREMENTO, NULO, CHAVE_ESTRANGEIRA FROM COLUNA WHERE TABELA_ID = ?";
            PreparedStatement psColuna = con.prepareStatement(sqlColuna);
            psColuna.setInt(1, t.getId());
            ResultSet rsColuna = psColuna.executeQuery();
            
            while (rsColuna.next())
            {
                Coluna c = new Coluna();
                c.setId(rsColuna.getInt("ID"));
                c.setNome(rsColuna.getString("NOME"));
                c.setAnotacao(rsColuna.getString("ANOTACAO"));
                c.setTipo(rsColuna.getString("TIPO"));
                c.setTamanho(rsColuna.getString("TAMANHO"));
                c.setAutoIncremento(rsColuna.getBoolean("AUTO_INCREMENTO"));
                c.setNulo(rsColuna.getInt("NULO"));
                
                //Guardando temporariamente estas informacoes para criar os vinculos de chave estrangeira
                //Essa solucao foi tomada para tentar maximizar a performance e diminuir o numero de objetos
                //na memoria
                ids.put(rsColuna.getInt("ID"), rsColuna.getInt("CHAVE_ESTRANGEIRA"));
                idColuna.put(rsColuna.getInt("ID"), c);
                
                t.adicionarColuna(c);
            }
            
            rsColuna.close();
            psColuna.close();
            
        }
        
        rs.close();
        ps.close();
        
        Iterator it = idColuna.values().iterator();
        Integer aux = null;
        
        while (it.hasNext())
        {
            Coluna colP = (Coluna) it.next();            
            aux = (Integer) ids.get(colP.getId());            
            colP.setChaveEstrangeira((Coluna) idColuna.get(aux));
        }
        
        VelocityContext context = new VelocityContext();
        
        context.put("banco_dados", bd);
        
        Template template = null;
        
        try
        {
            template = Velocity.getTemplate("banco.vm");
        }
        catch( ResourceNotFoundException rnfe )
        {
            // couldn't find the template
        }
        catch( ParseErrorException pee )
        {
            // syntax error: problem parsing the template
        }
        catch( MethodInvocationException mie )
        {
            // something invoked in the template
            // threw an exception
        }
        catch( Exception e )
        {}
        
        FileWriter outputStream = new FileWriter(saida);
        
        template.merge(context, outputStream);
        
        outputStream.flush();
        outputStream.close();
    }
    
    /**
     * Exporta a estrutura da base de dados gerada previamente para um modelo
     * que pode ser aberto na ferramenta Freemind. Para cada tabela será gerado
     * um arquivo e todos os arquivos terão links uns para os outros.
     * @param database nome do banco de dados que será exportado.
     * @param saida nome do diretório onde estarão contidos os arquivos de exportação.
     */
    public void exportarPorTabela(String database, String saida) throws Exception
    {
        //Properties p = new Properties();
        //p.setProperty("file.resource.loader.path", "pshell/base");
        //Velocity.init(p);
        Velocity.init();
        
        //Buscando informacoes sobre o banco de dados
        String sql = "SELECT ID, NOME, ANOTACAO FROM BANCO_DADOS WHERE NOME LIKE ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, database);
        ResultSet rs = ps.executeQuery();
        
        BancoDados bd = new BancoDados();
        rs.next();
        bd.setId(rs.getInt("ID"));
        bd.setNome(rs.getString("NOME"));
        bd.setAnotacao(rs.getString("ANOTACAO"));
        
        rs.close();
        ps.close();
        
        //Buscando informacoes das tabelas
        sql = "SELECT ID, NOME, ANOTACAO FROM TABELA WHERE BANCO_DADOS_ID = ?";
        ps = con.prepareStatement(sql);
        ps.setInt(1, bd.getId());
        rs = ps.executeQuery();
        
        HashMap ids = new HashMap(); //Combina o ID da chave estrangeira com o id da coluna
        HashMap idColuna = new HashMap(); //Combina o ID da coluna com o objeto que representa esta coluna
        while (rs.next())
        {
            Tabela t = new Tabela();
            t.setId(rs.getInt("ID"));
            t.setNome(rs.getString("NOME"));
            t.setAnotacao(rs.getString("ANOTACAO"));
            
            bd.adicionarTabela(t);
            
            String sqlColuna = "SELECT ID, NOME, ANOTACAO, TIPO, TAMANHO, AUTO_INCREMENTO, NULO, CHAVE_ESTRANGEIRA, CHAVE_PRIMARIA FROM COLUNA WHERE TABELA_ID = ?";
            PreparedStatement psColuna = con.prepareStatement(sqlColuna);
            psColuna.setInt(1, t.getId());
            ResultSet rsColuna = psColuna.executeQuery();
            
            while (rsColuna.next())
            {
                Coluna c = new Coluna();
                c.setId(rsColuna.getInt("ID"));
                c.setNome(rsColuna.getString("NOME"));
                c.setAnotacao(rsColuna.getString("ANOTACAO"));
                c.setTipo(rsColuna.getString("TIPO"));
                c.setTamanho(rsColuna.getString("TAMANHO"));
                c.setAutoIncremento(rsColuna.getBoolean("AUTO_INCREMENTO"));
                c.setNulo(rsColuna.getInt("NULO"));
                c.setChavePrimaria(rsColuna.getBoolean("CHAVE_PRIMARIA"));
                
                //Guardando temporariamente estas informacoes para criar os vinculos de chave estrangeira
                //Essa solucao foi tomada para tentar maximizar a performance e diminuir o numero de objetos
                //na memoria
                ids.put(rsColuna.getInt("ID"), rsColuna.getInt("CHAVE_ESTRANGEIRA"));
                idColuna.put(rsColuna.getInt("ID"), c);
                
                t.adicionarColuna(c);
            }
            
            rsColuna.close();
            psColuna.close();
            
        }
        
        rs.close();
        ps.close();
        
        Iterator it = idColuna.values().iterator();
        Integer aux = null;
        
        while (it.hasNext())
        {
            Coluna colP = (Coluna) it.next();            
            aux = (Integer) ids.get(colP.getId());            
            colP.setChaveEstrangeira((Coluna) idColuna.get(aux));
        }
        
        Template template = null;
        
        try
        {
            template = Velocity.getTemplate("bancoPerFile.vm");
        }
        catch( ResourceNotFoundException rnfe )
        {
            // couldn't find the template
        }
        catch( ParseErrorException pee )
        {
            // syntax error: problem parsing the template
        }
        catch( MethodInvocationException mie )
        {
            // something invoked in the template
            // threw an exception
        }
        catch( Exception e )
        {}
        
        VelocityContext context = new VelocityContext();
        
        int auxCont = 0;
        
        File arq = new File(saida);
        arq.mkdir();
        
        for (Tabela tabelaGravar : bd.getTabelas())
        {
            FileWriter outputStream = new FileWriter(saida + "/" + tabelaGravar.getNome() + ".mm");

            context.put("tabela", tabelaGravar);

            template.merge(context, outputStream);

            outputStream.flush();
            outputStream.close();
            System.out.println("Gravando tabela #" + auxCont++);            
        }
    }
    
    /**
     * Exporta a estrutura da base de dados gerada previamente para um modelo
     * que pode ser aberto na ferramenta Freemind. Para cada tabela será gerado
     * um arquivo e todos os arquivos terão links uns para os outros.
     * @param database nome do banco de dados que será exportado.
     * @param saida nome do diretório onde estarão contidos os arquivos de exportação.
     */
    public void exportarPorTabela(String database, String tabela, String saida) throws Exception
    {
        //Properties p = new Properties();
        //p.setProperty("file.resource.loader.path", "pshell/base");
        //Velocity.init(p);
        Velocity.init();
        
        //Buscando informacoes sobre o banco de dados
        String sql = "SELECT ID, NOME, ANOTACAO FROM BANCO_DADOS WHERE NOME LIKE ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, database);
        ResultSet rs = ps.executeQuery();
        
        BancoDados bd = new BancoDados();
        rs.next();
        bd.setId(rs.getInt("ID"));
        bd.setNome(rs.getString("NOME"));
        bd.setAnotacao(rs.getString("ANOTACAO"));
        
        rs.close();
        ps.close();
        
        //Buscando informacoes das tabelas
        sql = "SELECT ID, NOME, ANOTACAO FROM TABELA WHERE BANCO_DADOS_ID = ? AND NOME LIKE ?";
        ps = con.prepareStatement(sql);
        ps.setInt(1, bd.getId());
        ps.setString(2, tabela);
        rs = ps.executeQuery();
        
        HashMap ids = new HashMap(); //Combina o ID da chave estrangeira com o id da coluna
        HashMap idColuna = new HashMap(); //Combina o ID da coluna com o objeto que representa esta coluna
        while (rs.next())
        {
            Tabela t = new Tabela();
            t.setId(rs.getInt("ID"));
            t.setNome(rs.getString("NOME"));
            t.setAnotacao(rs.getString("ANOTACAO"));
            
            bd.adicionarTabela(t);
            
            String sqlColuna = "SELECT ID, NOME, ANOTACAO, TIPO, TAMANHO, AUTO_INCREMENTO, NULO, CHAVE_ESTRANGEIRA, CHAVE_PRIMARIA FROM COLUNA WHERE TABELA_ID = ?";
            PreparedStatement psColuna = con.prepareStatement(sqlColuna);
            psColuna.setInt(1, t.getId());
            ResultSet rsColuna = psColuna.executeQuery();
            
            while (rsColuna.next())
            {
                Coluna c = new Coluna();
                c.setId(rsColuna.getInt("ID"));
                c.setNome(rsColuna.getString("NOME"));
                //c.setAnotacao(converte(rsColuna.getString("ANOTACAO"), saida));
                c.setAnotacao(rsColuna.getString("ANOTACAO"));
                c.setTipo(rsColuna.getString("TIPO"));
                c.setTamanho(rsColuna.getString("TAMANHO"));
                c.setAutoIncremento(rsColuna.getBoolean("AUTO_INCREMENTO"));
                c.setNulo(rsColuna.getInt("NULO"));
                c.setChavePrimaria(rsColuna.getBoolean("CHAVE_PRIMARIA"));
                
                //Guardando temporariamente estas informacoes para criar os vinculos de chave estrangeira
                //Essa solucao foi tomada para tentar maximizar a performance e diminuir o numero de objetos
                //na memoria
                ids.put(rsColuna.getInt("ID"), rsColuna.getInt("CHAVE_ESTRANGEIRA"));
                idColuna.put(rsColuna.getInt("ID"), c);
                
                t.adicionarColuna(c);
            }
            
            rsColuna.close();
            psColuna.close();
            
        }
        
        rs.close();
        ps.close();
        
        Iterator it = idColuna.values().iterator();
        Integer aux = null;
        
        while (it.hasNext())
        {
            Coluna colP = (Coluna) it.next();            
            aux = (Integer) ids.get(colP.getId());            
            colP.setChaveEstrangeira((Coluna) idColuna.get(aux));
        }
        
        Template template = null;
        
        try
        {
            template = Velocity.getTemplate("bancoPerFile.vm");
        }
        catch( ResourceNotFoundException rnfe )
        {
            // couldn't find the template
        }
        catch( ParseErrorException pee )
        {
            // syntax error: problem parsing the template
        }
        catch( MethodInvocationException mie )
        {
            // something invoked in the template
            // threw an exception
        }
        catch( Exception e )
        {}
        
        VelocityContext context = new VelocityContext();
        
        int auxCont = 0;
        
        File arq = new File(saida);
        arq.mkdir();
        
        for (Tabela tabelaGravar : bd.getTabelas())
        {
            FileWriter outputStream = new FileWriter(saida + "/" + tabelaGravar.getNome() + ".mm");

            context.put("tabela", tabelaGravar);

            template.merge(context, outputStream);

            outputStream.flush();
            outputStream.close();
            System.out.println("Gravando tabela #" + auxCont++);            
        }
    }    
    
    
    /**
     * Coloca informações sobre uma determinada coluna na base de dados de documentação
     * de um outro banco de dados. Essa base de documentação deve ter sido criada pelo
     * comando documentar.
     */
    public void inserirInfo(String banco, String tabela, String coluna, String informacao, String charset)
        throws SQLException, UnsupportedEncodingException
    {
        String sql = "SELECT c.id FROM banco_dados as bd, tabela as t, coluna as c " +
                "WHERE bd.nome like ? and bd.id = t.banco_dados_id and " +
                "t.nome like ? and t.id = c.tabela_id and c.nome like ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, banco);
        ps.setString(2, tabela);
        ps.setString(3, coluna);
        ResultSet rs = ps.executeQuery();
        rs.next();
        int colunaID = rs.getInt(1);
        
        sql = "UPDATE COLUNA SET ANOTACAO = ? WHERE ID = ?";
        ps = con.prepareStatement(sql);
        ps.setString(1, converte(informacao, charset));
        ps.setInt(2, colunaID);
        ps.execute();
        
        if (rs != null) rs.close();
        if (ps != null) ps.close();
    }
}

