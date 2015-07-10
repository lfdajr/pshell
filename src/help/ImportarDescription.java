/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package help;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author 99282895491
 */
public class ImportarDescription
{

    private Connection con, con2;

    public void conectar() throws ClassNotFoundException, SQLException
    {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        Class.forName("com.mysql.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:sqlserver://10.200.111.6:1034;databaseName=sig2000;", "decta", "decta09");
        con2 = DriverManager.getConnection("jdbc:mysql://localhost/docs", "root", "");
    }
    
    public void desconectar() throws SQLException
    {
        con.close();
        con2.close();
    }
    
    public void imprimir() throws SQLException
    {
        String sql = "SELECT        o.Name AS TABELA, " +
                    "            c.name AS COLUNA, " +
                    "            convert(varchar(200), ep.value) AS VALOR " +                    
                    "FROM        sys.objects o INNER JOIN sys.extended_properties ep " +
                    "            ON o.object_id = ep.major_id " +
                    "            INNER JOIN sys.schemas s " +
                    "            ON o.schema_id = s.schema_id " +
                    "            LEFT JOIN syscolumns c " +
                    "            ON ep.minor_id = c.colid " +
                    "            AND ep.major_id = c.id " +
                    "WHERE        o.type IN ('V', 'U', 'P') " +
                    "ORDER BY    o.Name";

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        int id = 0;
        int contador = 0;
        while (rs.next())
        {
            if (rs.getString("COLUNA") == null)
            {
                try
                {
                    id = buscarTabelaID(rs.getString("TABELA"), con2);
                    String updateSql = "UPDATE TABELA SET ANOTACAO = ? WHERE ID = ?";
                    PreparedStatement ps = con2.prepareStatement(updateSql);
                    ps.setString(1, rs.getString("VALOR"));
                    ps.setInt(2, id);
                    ps.execute();
                    ps.close();
                }
                catch (Exception e)
                {
                }
            }
            else
            {
                try
                {
                    id = buscarColunaID(rs.getString("TABELA"), rs.getString("COLUNA"), con2);
                    String updateSql = "UPDATE COLUNA SET ANOTACAO = ? WHERE ID = ?";
                    PreparedStatement ps = con2.prepareStatement(updateSql);
                    ps.setString(1, rs.getString("VALOR"));
                    ps.setInt(2, id);
                    ps.execute();
                    ps.close();
                }
                catch (Exception e)
                {
                }                
            }
            System.out.println("Cont.: " + contador++);
        }
        
        rs.close();
        stmt.close();
        
    }
    
    public void imprimir2() throws SQLException
    {
        ResultSet rs = con.getMetaData().getColumns(null, null, null, null);
        
        int id = 0;
        int contador = 0;
        while (rs.next())
        {
            if (contador > 100) break;
            
            System.out.print(rs.getString("TABLE_NAME") + " - ");
            System.out.print(rs.getString("COLUMN_NAME") + " - ");
            System.out.print(rs.getString("TYPE_NAME") + " - ");     
            System.out.print(rs.getString("COLUMN_SIZE") + " - ");                 
            System.out.println("");
            
            contador++;
        }
        
        rs.close();
        
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
        int id = rs.getInt(1);
        
        rs.close();
        ps.close();
        
        return id;
    }    
    
    private int buscarTabelaID(String tabela, Connection conexao) throws SQLException
    {
        String sql = "SELECT T.ID FROM TABELA as T WHERE T.NOME like ?";
        PreparedStatement ps = conexao.prepareStatement(sql);
        ps.setString(1, tabela);
        ResultSet rs = ps.executeQuery();
        rs.next();        
        int id = rs.getInt(1);
        
        rs.close();
        ps.close();
        
        return id;
    }    
    
    public static void main(String arg[]) throws ClassNotFoundException, SQLException
    {
        ImportarDescription id = new ImportarDescription();
        id.conectar();
        
        id.imprimir();
        
        id.desconectar();
    }
}
