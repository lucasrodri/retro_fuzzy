/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encontrafcl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author lucas
 */
public class EncontraFCL {

    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) {

        //Parametros do ONS
        String ra = "Fuzzy";
        String xml = "xml/Fuzzy/USA.xml";
        int seed = 1;
        int calls = 1000;
        int load = 500;
        String diretory = "FuzzyResults";

        //Metrica que sera lida!
        String metric = "BBR";

        //Arquivo .fcl
        String FCL_file = "xml/Fuzzy/rotas.fcl";
        
        //O melhor e o minimo ou o maior (min ou max)
        String best = "min";
        
        //Quantidade de vezes que queremos refuzificar
        int vezes = 5;
        
        RetroFuzzy retro = new RetroFuzzy(ra, xml, seed, calls, load, diretory, metric, FCL_file, best);
        for (int i = 0; i < vezes; i++) {
            System.out.println("\nRetroalimentacao No:" + i+1 + "\n");
            retro.start();
        }
    }


    
}
