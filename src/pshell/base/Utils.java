package pshell.base;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author lourival
 */
public class Utils {

    public static String lerArquivo(String fileName) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        
        } finally {
           br.close();
        }
    }
    
    public static String toCamelCase(String s)
    {
        String[] parts = s.toLowerCase().split("_");
        String camelCaseString = "";
        for (String part : parts){
            camelCaseString += part.substring(0, 1).toUpperCase() + part.substring(1);
        }
        return camelCaseString;
    }
    
    public static String toLowerCamelCase(String s)
    {
        String[] parts = s.toLowerCase().split("_");
        String camelCaseString = "";
        for (String part : parts){
            camelCaseString += part.substring(0, 1).toUpperCase() + part.substring(1);
        }
        
        camelCaseString = camelCaseString.substring(0, 1).toLowerCase() + camelCaseString.substring(1);
        
        return camelCaseString;
    }
    
    public static String transformarNomeMetodo(String s)
    {
        String[] parts = s.toLowerCase().split("-");
        String camelCaseString = "";
        for (String part : parts){
            camelCaseString += part.substring(0, 1).toUpperCase() + part.substring(1);
        }
        
        camelCaseString = camelCaseString.substring(0, 1).toLowerCase() + camelCaseString.substring(1);
        
        return camelCaseString;
    }
}

