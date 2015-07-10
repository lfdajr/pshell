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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Contém alguns métodos que facilitam a criação de classes utilitárias
 * e com funcionalidades específicas para manipulação de bancos de dados.
 * @author Lourival Almeida
 * @version 1.7
 */
public class ShellDBBaseClass extends ShellBaseClass
{

    /**
     *
     */
    protected Connection con = null;
    private Properties info;
    
    /**
     *
     */
    public ShellDBBaseClass()
    {
        cleanHelpBuffer();
        montarHelpBuffer();
        inicializarPalavras();
        
        try 
        {
            cleanCodeCompletionRules();
            initiateCodeCompletionRules();
        } 
        catch (Exception ex) {
        }
    }
    
    @Override
    protected void montarHelpBuffer() {
        super.montarHelpBuffer(); //To change body of generated methods, choose Tools | Templates.
        getHelpBuffer().put("atualizar","atualizar <tabela> <coluna> <colunaFiltro>\n");
        getHelpBuffer().put("buscar","buscar <tabela> <coluna> <valor> <colExibir>\n" +
                "buscar <tabela> <coluna> <valor>\n" +
                "buscar <tabela> <numero linhas>\n" +   
                "buscar <tabela>\n");
        getHelpBuffer().put("chaves-exportadas","chaves-exportadas <tabela>\n" +
                "chaves-exportadas <tabela> <texto>\n");
        getHelpBuffer().put("desconectar","desconectar\n");                        
        getHelpBuffer().put("conectar","conectar <propriedade>\n");
        getHelpBuffer().put("dependencia","dependencia <tabela>\n");
        getHelpBuffer().put("dependencias","dependencias <tabela> <texto>\n");
        getHelpBuffer().put("describe","describe <tabela>\n");
        getHelpBuffer().put("find","find <coluna> <tabelaPattern>\n");
        getHelpBuffer().put("inserir","inserir <tabela> <coluna>\n" +   
                "inserir <tabela>\n");
        getHelpBuffer().put("show","show <tabela>\n" +
                "show\n");
        getHelpBuffer().put("show-procs","show-procs\n" +
                "show-procs <proc pattern>\n");
    }

    /**
     * Abre uma conexão com uma base de dados.
     * @param info objeto contendo as propriedades de conexão para a base de dados desejada.
     */
    public void conectar(Properties info)
    {
        if (con != null)
        {
            desconectar();
        }
        this.info = info;

        try
        {
            Class.forName(info.getProperty("driverName"));
            con = DriverManager.getConnection(info.getProperty("connectionUrl"), info.getProperty("userName"), info.getProperty("password"));
            System.out.println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("conexao.ok"));
            System.out.println(info.getProperty("connectionUrl"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Abre uma conexão com um banco de dados. As informações da conexão como username,
     * password, drivername e connectionUrl (segundo padrao JDBC) devem ser fornecidas
     * em um arquivo de propriedades com o seguinte formato.<br>
     * arg1.drivername=com.microsoft.sqlserver.jdbc.SQLServerDriver<br>
     * arg1.connectionUrl=jdbc:sqlserver://localhost:1433;databaseName=usuarios;<br>
     * arg1.username=root<br>
     * arg1.password=root<br>
     * @param propriedade parâmetro a ser chamado com o método conectar. Corresponde ao "arg1" do arquivo
     * de propriedade.
     * @throws java.lang.Exception
     */
    public void conectar(String propriedade)
            throws Exception
    {
        ResourceBundle rb = getConfigFile();
        Properties prop = new Properties();

        prop.put("userName", rb.getString(propriedade + ".username"));
        prop.put("password", rb.getString(propriedade + ".password"));
        prop.put("driverName", rb.getString(propriedade + ".drivername"));
        prop.put("connectionUrl", rb.getString(propriedade + ".connectionUrl"));

        conectar(prop);
    }

    /**
     * Desconecta da base de dados em utilização.
     */
    public void desconectar()
    {
        if (con != null)
        {
            try
            {
                con.close();
                con = null;
                info = null;
                System.out.println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("conexao.encerrada"));
                System.out.println(info.getProperty("connectionUrl"));
            }
            catch (Exception e)
            {
            }
        }
    }

    /**
     * Exibe as colunas de uma tabela passada como parâmetro.
     * @param tabela nome da tabela a ser descrita.
     * @throws java.sql.SQLException
     */
    public void describe(String tabela) throws SQLException
    {
        if (con == null)
        {
            System.out.println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("conexao.nenhuma"));
            return;
        }

        Statement stmt = null;
        ResultSet rs = null;

        try
        {
            System.out.println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("saida.lista_colunas"));
            String SQL = "SELECT * FROM " + tabela;
            stmt = con.createStatement();
            stmt.setMaxRows(1);

            rs = stmt.executeQuery(SQL);
            ResultSetMetaData rsmd = rs.getMetaData();
            ResultSet chavesPrimarias = con.getMetaData().getPrimaryKeys(null, null, tabela);
            int cols = rsmd.getColumnCount();

            while (chavesPrimarias.next())
            {
                System.out.print(chavesPrimarias.getString("COLUMN_NAME") + " ");
            }
            System.out.print("<- Chaves primárias.\n");
            
            chavesPrimarias.close();
            
            System.out.println("");
            for (int i = 1; i <= cols; i++)
            {
                if (i < 10)
                {
                    System.out.print("0" + i + ". ");
                }
                else
                {
                    System.out.print(i + ". ");
                }
                
                System.out.println((rsmd.isAutoIncrement(i) ? "*" : "")
                        + rsmd.getColumnName(i) + " - " 
                        + rsmd.getColumnTypeName(i) + " ("
                        + rsmd.getPrecision(i) + ")"
                        + (rsmd.isNullable(i) == 1 ? " - NULL" : ""));
            }

            System.out.println("");
            
            System.out.println(cols + " coluna(s).");
            System.out.println("Legenda: NULL - campo da tabela permite valores nulos.\n* - Campo auto-incremento.");
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
    }

    /**
     * Exibe as tabelas da base de dados a qual o usuáio está conectado.
     * @throws java.sql.SQLException 
     */
    public void show() throws SQLException
    {
        String[] types =
        {
            "TABLE"
        };
        DatabaseMetaData rsmd = con.getMetaData();
        ResultSet rs = rsmd.getTables(null, null, "%", types);
        System.out.println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("saida.lista_tabelas"));
        int contador = 0;
        while (rs.next())
        {
            String table = rs.getString("TABLE_NAME");
            System.out.println(table);
            contador++;
        }
        System.out.println("\n" + contador + " tabela(s) encontrada(s).");
        if (rs != null)
        {
            rs.close();
        }
    }
    
    /**
     * Exibe as procedures da base de dados a qual o usuário está conectado.
     * @throws java.sql.SQLException 
     */
    public void showProcs() throws SQLException
    {
        DatabaseMetaData rsmd = con.getMetaData();
        ResultSet rs = rsmd.getProcedures(null, null, null);
        System.out.println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("saida.lista_procedures"));
        int contador = 0;
        while (rs.next())
        {
            String table = rs.getString("PROCEDURE_NAME");
            System.out.println(table);
            contador++;
        }
        System.out.println("\n" + contador + " procedure(s) encontrada(s).");
        if (rs != null)
        {
            rs.close();
        }
    }    

    /**
     * Exibe as tabelas e/ou procedures que possuem o nome semelhante ao nome passado como parâmetro.
     * @param tabela
     * @throws java.sql.SQLException 
     */
    public void show(String tabela) throws SQLException
    {
        String[] types =
        {
            "TABLE"
        };
        DatabaseMetaData rsmd = con.getMetaData();
        ResultSet rs = rsmd.getTables(null, null, "%", types);
        System.out.println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("saida.lista_tabelas"));
        int contador = 0;
        
        while (rs.next())
        {
            String table = rs.getString("TABLE_NAME").toLowerCase();

            if (table.indexOf(tabela.toLowerCase()) >= 0)
            {
                System.out.println(table);
                contador ++;
            }
        }
        System.out.println("\n" + contador + " tabela(s) encontrada(s).\n");
        
        if (rs != null)
        {
            rs.close();
        }
    }
    
    /**
     *
     * @param pattern
     * @throws SQLException
     */
    public void showProcs(String pattern) throws SQLException
    {
        int contador = 0;
        DatabaseMetaData rsmd = con.getMetaData();
        System.out.println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("saida.lista_procedures"));
        ResultSet rs = rsmd.getProcedures(null, null, null);        
        while (rs.next())
        {
            String table = rs.getString("PROCEDURE_NAME").toLowerCase();

            if (table.indexOf(pattern.toLowerCase()) >= 0)
            {
                System.out.println(table);
                contador ++;
            }
        }
        System.out.println("\n" + contador + " procedure(s) encontrada(s).");        
    }

    /**
     * Método padrão para inserção de valores em uma tabela.
     * Quando for acionado este comando, o pshell requisitará uma entrada de dados para TODAS as colunas existentes na tabela.
     * @param tabela nome da tabela onde será feita a inserção de dados.
     */
    public void inserir(String tabela)
    {
        try
        {
            String sql = "SELECT * FROM " + tabela;
            Statement stmt = con.createStatement();
            stmt.setMaxRows(1);
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();

            int cols = rsmd.getColumnCount();
            int i = 0;
            StringBuffer colunas = new StringBuffer();

            for (i = 1; i <= cols; i++)
            {
                colunas.append(", ?");
            }

            sql = "INSERT INTO " + tabela + " VALUES (" + colunas.substring(1, colunas.length()) + ")";
            PreparedStatement cs = con.prepareStatement(sql);

            for (i = 1; i <= cols; i++)
            {
                String teste = lerTeclado("Informe valor para coluna " + rsmd.getColumnName(i) + " (" + rsmd.getColumnTypeName(i) + "): ");

                realizarInsercao(cs, rsmd.getColumnType(i), i, teste);
            }

            //System.out.println(cs.toString());
            cs.execute();

            if (rs != null)
            {
                rs.close();
            }
            if (cs != null)
            {
                cs.close();
            }
            if (stmt != null)
            {
                stmt.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Buscar em uma tabela por uma determinada string.<br>
     * Ex.: buscar aluno nome marcos - buscar na tabela aluno e na coluna nome o aluno marcos<br>
     * Ex.: buscar aluno 1 marcos - buscar na tabela aluno e na coluna que está no primeiro índice o aluno marcos
     * 
     * @param tabela nome da tabela.
     * @param col coluna de pesquisa. A coluna pode ser o nome exato da coluna da tabela ou o número da coluna. Este número pode ser visualizado 
     * com o comando {@link pshell.base.ShellDBBaseClass#show show nome-tabela}.
     * @param valor valor a ser buscado na tabela.
     * @throws java.sql.SQLException
     */
    public void buscar(String tabela, String col, String valor)
            throws SQLException
    {
        String colunaBusca = col;
        ResultSet rs = null;
        Statement stmt = null;
        PreparedStatement cs = null;

        try
        {
            int coluna = Integer.parseInt(colunaBusca);

            String SQL = "SELECT * FROM " + tabela;
            stmt = con.createStatement();
            stmt.setMaxRows(1);
            rs = stmt.executeQuery(SQL);
            ResultSetMetaData rsmd = rs.getMetaData();
            colunaBusca = rsmd.getColumnName(coluna);
        }
        catch (Exception ex)
        {
            //ex.printStackTrace();
        }

        String like = " like ?";
        Object val = valor;

        try
        {
            val = new Integer(valor);
            like = " = ?";
        }
        catch (Exception ex)
        {
            //ex.printStackTrace();
        }

        String sql = "SELECT * FROM " + tabela + " WHERE " + colunaBusca + like;
        cs = con.prepareStatement(sql);
        if (val instanceof Integer)
        {
            cs.setObject(1, val);
        }
        else
        {
            cs.setObject(1, "%" + val + "%");
        }
        rs = cs.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();


        int i = 0,  contador = 0;
        while (rs.next())
        {
            for (i = 1; i <= rsmd.getColumnCount(); i++)
            {
                System.out.print(rs.getObject(rsmd.getColumnName(i)) + " [" + rsmd.getColumnName(i).toLowerCase() + "]; ");
            }
            System.out.println("\n---");
            contador++;
        }

        System.out.println("\n" + contador + " registro(s) encontrado(s).");

        if (rs != null)
        {
            rs.close();
        }
        if (cs != null)
        {
            cs.close();
        }
        if (stmt != null)
        {
            stmt.close();
        }
    }

    /**
     * Buscar em uma tabela por uma determinada string. Para o resultado serão
     * exibidas apenas as colunas informadas.<br>
     * Ex.: buscar aluno nome marcos - buscar na tabela aluno e na coluna nome o aluno marcos<br>
     * Ex.: buscar aluno 1 marcos - buscar na tabela aluno e na coluna que está no primeiro índice o aluno marcos
     * @param tabela nome da tabela onde sera procurado o item desejado.
     * @param col coluna da tabela onde sera procurado o item desejado.
     * @param valor valor a ser buscado na tabela.
     * @param colExibir colunas separadas por "."<br>
     * @throws java.sql.SQLException 
     */
    public void buscar(String tabela, String col, String valor, String colExibir)
            throws SQLException
    {
        int i = 0;
        int x = 0;

        Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement cs = null;

        StringTokenizer sto = new StringTokenizer(colExibir, ".");
        String colunasExibir[] = new String[sto.countTokens()];

        while (sto.hasMoreTokens())
        {
            colunasExibir[i] = sto.nextToken();
            i++;
        }

        String colunaBusca = col;
        try
        {
            int coluna = Integer.parseInt(colunaBusca);

            String SQL = "SELECT * FROM " + tabela;
            stmt = con.createStatement();
            stmt.setMaxRows(1);
            rs = stmt.executeQuery(SQL);
            ResultSetMetaData rsmd = rs.getMetaData();
            colunaBusca = rsmd.getColumnName(coluna);
        }
        catch (Exception ex)
        {
            //ex.printStackTrace();
        }

        String like = " like ?";
        Object val = valor;

        try
        {
            val = new Integer(valor);
            like = " = ?";
        }
        catch (Exception ex)
        {
            //ex.printStackTrace();
        }

        String sql = "SELECT * FROM " + tabela + " WHERE " + colunaBusca + like;
        cs = con.prepareStatement(sql);
        if (val instanceof Integer)
        {
            cs.setObject(1, val);
        }
        else
        {
            cs.setObject(1, "%" + val + "%");
        }
        rs = cs.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();

        int contador = 0;

        while (rs.next())
        {
            for (i = 1; i <= rsmd.getColumnCount(); i++)
            {
                for (x = 0; x < colunasExibir.length; x++)
                {
                    if (colunasExibir[x].toLowerCase().equals(rsmd.getColumnName(i).toLowerCase()))
                    {
                        System.out.print(rs.getObject(rsmd.getColumnName(i)) + " [" + rsmd.getColumnName(i).toLowerCase() + "]; ");
                    }
                    try
                    {
                        if (Integer.parseInt(colunasExibir[x]) == i)
                        {
                            System.out.print(rs.getObject(rsmd.getColumnName(i)) + " [" + rsmd.getColumnName(i).toLowerCase() + "]; ");
                        }
                    }
                    catch (Exception ex)
                    {
                    }

                }
            }
            System.out.println("\n---");
            contador++;
        }

        System.out.println("\n" + contador + " registro(s) encontrado(s).");

        if (rs != null)
        {
            rs.close();
        }
        if (stmt != null)
        {
            stmt.close();
        }
        if (cs != null)
        {
            cs.close();
        }
    }

    /**
     * Sair da aplicacao.
     * @throws java.lang.Exception
     */
    public void sair() throws Exception
    {
        desconectar();
        super.sair();
    }

    /**
     * Método padrão para inserção de valores em uma tabela.
     * O usuário deverá informar os campos que pretende incluir informações. 
     * Quando for acionado este comando, o pshell requisitará uma entrada de dados para cada coluna informada.<br>
     * Ex.: <b>inserir usuario nome,telefone,email</b> (inserir na tabela usuario apenas nos campos nome, telefone e email)
     * @param tabela nome da tabela onde serão incluídos os valores.
     * @param colExibir
     * @throws java.sql.SQLException
     */
    public void inserir(String tabela, String colExibir) 
        throws SQLException
    {
        Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement cs = null;
        int i = 0;
        String camposInserir = null;
        StringBuffer sb = new StringBuffer();
        
        if (colExibir.indexOf(".") >= 0)
        {
            String sql = "SELECT * FROM " + tabela;
            stmt = con.createStatement();
            stmt.setMaxRows(1);
            rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            
            StringTokenizer sto = new StringTokenizer(colExibir, ".");
            int itemIndex = 0;

            while (sto.hasMoreTokens())
            {
                itemIndex = Integer.parseInt(sto.nextToken());
                sb.append(rsmd.getColumnName(itemIndex) + ", ");
            }
            
            camposInserir = sb.substring(0, sb.length() - 2);
            
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            
        }
        else
            camposInserir = colExibir;
        
        System.out.println(camposInserir);

        try
        {
            String sql = "SELECT " + camposInserir + " FROM " + tabela;
            stmt = con.createStatement();
            stmt.setMaxRows(1);
            rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();

            int cols = rsmd.getColumnCount();
            StringBuffer valores = new StringBuffer();

            for (i = 1; i <= cols; i++)
            {
                valores.append(", ?");
            }

            sql = "INSERT INTO " + tabela + " (" + camposInserir + ") VALUES (" + valores.substring(1, valores.length()) + ")";
            cs = con.prepareStatement(sql);

            for (i = 1; i <= cols; i++)
            {
                String teste = lerTeclado("Informe valor para coluna " + rsmd.getColumnName(i) + " (" + rsmd.getColumnTypeName(i) + "): ");

                realizarInsercao(cs, rsmd.getColumnType(i), i, teste);
            }

            //System.out.println(cs.toString());
            cs.execute();

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
        if (cs != null)
        {
            cs.close();
        }
    }

    private void realizarInsercao(PreparedStatement cs, int tipo, int index, String valor)
            throws SQLException
    {
        switch (tipo)
        {
            case java.sql.Types.VARCHAR:
                cs.setString(index, valor);
                break;

            case java.sql.Types.INTEGER:
                cs.setInt(index, Integer.parseInt(valor));
                break;

            case java.sql.Types.TIMESTAMP:
                cs.setTimestamp(index, new java.sql.Timestamp(new Date().getTime()));
                break;
                
            case java.sql.Types.BOOLEAN:
                cs.setBoolean(index, Boolean.getBoolean(valor));
                break;                
                
            case java.sql.Types.BIT:
                cs.setBoolean(index, Boolean.getBoolean(valor));
                break;                
                
            case java.sql.Types.TINYINT:
                cs.setInt(index, Integer.parseInt(valor));
                break;                                
        }

    }

    /**
     * Verifica as dependências da tabela especificada.
     * @param tabela nome da tabela a ser verificadas as dependências.
     * @throws java.sql.SQLException 
     */
    public void dependencia(String tabela) throws SQLException
    {
        DatabaseMetaData rsmd = con.getMetaData();
        ResultSet rs = rsmd.getImportedKeys(null, null, tabela);
        
        
        System.out.println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("saida.lista_dependencias") + " TABELA: " + tabela.toUpperCase());

        while (rs.next())
        {
            System.out.print(rs.getString("PKTABLE_NAME") + "." + rs.getString("PKCOLUMN_NAME"));
            System.out.print(" -> ");
            System.out.println(rs.getString("FKTABLE_NAME") + "." + rs.getString("FKCOLUMN_NAME"));
        }

        if (rs != null)
        {
            rs.close();
        }
    }
    
    /**
     * Verifica quais as chaves que são exportadas da tabela passada como parâmetro.
     * Basicamente o contrário do comando dependencias.
     * @param tabela nome da tabela a ser verificadas as exportações.
     * @throws java.sql.SQLException 
     */
    public void chavesExportadas(String tabela) throws SQLException
    {
        DatabaseMetaData rsmd = con.getMetaData();
        ResultSet rs = rsmd.getExportedKeys(null, null, tabela);
        
        
        System.out.println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("saida.lista_dependencias") + " TABELA: " + tabela.toUpperCase());

        while (rs.next())
        {
            System.out.print(rs.getString("PKTABLE_NAME") + "." + rs.getString("PKCOLUMN_NAME"));
            System.out.print(" -> ");
            System.out.println(rs.getString("FKTABLE_NAME") + "." + rs.getString("FKCOLUMN_NAME"));
        }

        if (rs != null)
        {
            rs.close();
        }
    }    

    /**
     * Verifica todas as dependências nos vários níveis de uma tabela.
     * @param tabela nome da tabela a ser verificadas as dependências.
     * @param texto separador textual que será exibido para o usuário.
     * @throws java.sql.SQLException 
     */
    public void dependencias(String tabela, String texto) throws SQLException
    {
        DatabaseMetaData rsmd = con.getMetaData();
        ResultSet rs = rsmd.getImportedKeys(null, null, tabela);

        Set<String> set = new HashSet();

        while (rs.next())
        {
            set.add(rs.getString("PKTABLE_NAME"));
        }

        for (String t : set)
        {
            if (!t.equals(tabela))
            {
                System.out.println(texto + t);
                this.dependencias(t, texto + texto);
            }

        }

        if (rs != null)
        {
            rs.close();
        }
    }
    
    /**
     * Verifica quais as chaves que s�o exportadas da tabela passada como par�metro.
     * Basicamente o contrário do comando dependencias.
     * @param tabela nome da tabela a ser verificadas as exportações.
     * @param texto separador textual que será exibido para o usuário.
     * @throws java.sql.SQLException 
     */
    public void chavesExportadas(String tabela, String texto) throws SQLException
    {
        DatabaseMetaData rsmd = con.getMetaData();
        ResultSet rs = rsmd.getExportedKeys(null, null, tabela);

        Set<String> set = new HashSet();

        while (rs.next())
        {
            set.add(rs.getString("FKTABLE_NAME"));
        }

        for (String t : set)
        {
            if (!t.equals(tabela))
            {
                System.out.println(texto + t);
                this.chavesExportadas(t, texto + texto);
            }

        }

        if (rs != null)
        {
            rs.close();
        }
        //System.out.println("");
    }    
    
    /**
     * Procura uma coluna específica dentro das tabelas da base de dados.
     * Exibe a coluna e a tabela que a contém.
     * @param coluna
     * @param tabelaPattern
     * @throws java.sql.SQLException
     */
    public void find(String coluna, String tabelaPattern) 
            throws SQLException
    {
        if (con == null)
        {
            System.out.println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("conexao.nenhuma"));
            return;
        }
        
        //Buscando as tabelas
        String[] types =
        {
            "TABLE"
        };
        DatabaseMetaData rsmdTabelas = con.getMetaData();
        ResultSet rsTabelas = rsmdTabelas.getTables(null, null, "%", types);

        while (rsTabelas.next())
        {
            String tabela = rsTabelas.getString("TABLE_NAME");
            
            if (tabela.toLowerCase().indexOf(tabelaPattern.toLowerCase()) < 0)
                continue;
            
            Statement stmt = null;
            ResultSet rs = null;
            ResultSetMetaData rsmd = null;

            try
            {
                String SQL = "SELECT * FROM " + tabela;
                stmt = con.createStatement();
                stmt.setMaxRows(1);
                rs = stmt.executeQuery(SQL);
                rsmd = rs.getMetaData();
                int cols = rsmd.getColumnCount();

                for (int i = 1; i <= cols; i++)
                {
                    if (rsmd.getColumnName(i).toLowerCase().indexOf(coluna.toLowerCase()) >= 0)
                        System.out.println(rsmd.getColumnName(i).toUpperCase() + " - " + tabela.toLowerCase());
                }
            }
            catch (Exception e)
            {
                //e.printStackTrace();
            }
            
            if (rs != null)
            {
                rs.close();
            }
            if (stmt != null)
            {
                stmt.close();
            }            

        }

        if (rsTabelas != null)
        {
            rsTabelas.close();
        }            
 
    }    
    
    /**
     * Atualiza o valor de uma coluna dentro de uma tabela. Equivalente ao comando 
     * UPDATE em SQL.
     * O comando em SQL: UPDATE FROM USUARIO SET NOME = 'TESTE' WHERE CODIGO = 1
     * seria substituido no pshell por:
     * atualizar USUARIO [NOME = 'TESTE'] [CODIGO = 1]
     * 
     * @param tabela nome da tabela.
     * @param coluna representa o nome da coluna e o valor a ser setado. Ex.: nome = 'teste'
     * @param colunaFiltro representa o nome da coluna e o valor a ser filtrado. Ex.: codigo = 3
     * @throws java.sql.SQLException
     */
    public void atualizar(String tabela, String coluna, String colunaFiltro) 
            throws SQLException
    {
        Statement ps = con.createStatement();
        ps.execute("UPDATE " + tabela + " SET " + coluna + " where " + colunaFiltro);
        ps.close();        
    }
    
    /**
     * Busca 5 linhas de uma tabela passada como parâmetro.
     * @param tabela
     * @throws java.sql.SQLException
     * @tabela nome da tabela onde serâo buscadas as informações.
     */
    public void buscar(String tabela) throws SQLException
    {
        buscar(tabela, "5");
    }
    
    /**
     * Busca numRows linhas de uma tabela passada como parâmetro.
     * @param tabela
     * @param numRows
     * @throws java.sql.SQLException
     * @tabela nome da tabela onde serão buscadas as informações.
     * @numRows número de linhas que serão trazidas.
     */
    public void buscar(String tabela, String numRows) throws SQLException
    {
        int i = 0, contador = 0;
        String sql = "SELECT * FROM " + tabela;
        Statement st = con.createStatement();
        st.setMaxRows(Integer.parseInt(numRows));
        ResultSet rs = st.executeQuery(sql);
        ResultSetMetaData rsmd = rs.getMetaData();
        
        while (rs.next())
        {
            for (i = 1; i <= rsmd.getColumnCount(); i++)
            {
                System.out.print(rs.getObject(rsmd.getColumnName(i)) + " [" + rsmd.getColumnName(i).toLowerCase() + "]; ");
            }
            System.out.println("\n---");
            contador++;
        }
    }
    
    /**
     *
     * @throws SQLException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws Exception
     */
    public void converterBase() throws SQLException, UnsupportedEncodingException, IOException, ClassNotFoundException, Exception
    {
        int contador = 0;
        String charset = "windows-1252";
        char charsetIso[] = new char[1000];
        char charsetOutro[] = new char[1000];
        int charContador = 0, x = 0, y = 0;
        
        String sql = "SELECT ID, ANOTACAO FROM COLUNA WHERE ANOTACAO IS NOT NULL";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        
        String update = "UPDATE COLUNA SET ANOTACAO = ? WHERE ID = ?";
        PreparedStatement ps = con.prepareStatement(update);
        
        while (rs.next())
        {
            String convertido = converte(rs.getString("ANOTACAO"), charset);
            System.out.println("--- " + new String(charsetOutro));
            System.out.println(convertido);
            System.out.println("--- " + new String(charsetIso));
            boolean temNovoChar = false;
            
            while (!temNovoChar)
            {
                for (x = 0; x < convertido.length(); x++)
                {
                    for (y = 0; y < charContador; y++)
                    {
                        if (convertido.charAt(x) == charsetOutro[y])
                        {
                            break;
                        }
                    }
                    if (y >= charContador) //significa que nao existe o caracter no array
                    {
                        temNovoChar = true;
                        break;
                    }
                }
                
                if (x >= convertido.length())
                    break;
            }
            
            if (temNovoChar)
            {
                String entrada = converte(lerTeclado("Nova String: "), charset);
                
                while (entrada.length() != convertido.length())
                    entrada = converte(lerTeclado("String com tamanho diferente. Digite novamente: "), charset);

                for (x = 0; x < convertido.length(); x++)
                {
                    for (y = 0; y < charContador; y++)
                    {
                        if (convertido.charAt(x) == charsetOutro[y])
                        {
                            break;
                        }
                    }
                    if (y >= charContador) //significa que n�o existe o caracter no array
                    {
                        charsetOutro[charContador] = convertido.charAt(x);
                        charsetIso[charContador] = entrada.charAt(x);
                        charContador++;
                    }
                }            
            }
            
            for (x = 0; x < charContador; x++)
            {
                convertido = convertido.replace(charsetOutro[x], charsetIso[x]);
            }
            
            ps.setString(1, convertido);
            ps.setInt(2, rs.getInt("ID"));
            ps.execute();
            
            System.out.println(contador++);
            
        }
        
        ps.close();
        rs.close();
        st.close();
        
    }
    
    /**
     * Abre uma linha de comando para execução de instruções SQL que serão
     * enviadas diretamente para a base a qual o usuário está conectado.
     * @throws java.sql.SQLException
     * @throws java.lang.Exception
     */
    public void sql() throws SQLException, Exception
    {
        if (con == null)
        {
            System.out.println(ResourceBundle.getBundle("pshell/base/Mensagens").getString("conexao.nenhuma"));
            return ;
        }
            
        System.out.println("Digite <sair> para concluir a execução de SQL.\n");
        int i = 0, contador = 0;
        String sql = lerTeclado("SQL -> ");
        
        while (!sql.equals("sair"))
        {
            contador = 0;
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();

            while (rs.next())
            {
                for (i = 1; i <= rsmd.getColumnCount(); i++)
                {
                    System.out.print(rs.getObject(rsmd.getColumnName(i)) + " [" + rsmd.getColumnName(i).toLowerCase() + "]; ");
                }
                System.out.println("\n---");
                contador++;
            }
            
            System.out.println("\n" + contador + " linhas encontradas.");
            sql = lerTeclado("SQL -> ");
        }
    }    
    
    /**
     *
     * @param tabela
     * @throws SQLException
     */
    public void contar(String tabela) throws SQLException
    {
        String sql = "SELECT count(*) as Contador from " + tabela;
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        rs.next();
        
        System.out.println(rs.getInt("Contador") + " registros na tabela.");
        
        rs.close();
        stmt.close();
    }
}
