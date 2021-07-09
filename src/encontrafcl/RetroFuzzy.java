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
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author lucas
 */
public class RetroFuzzy {

    static int times;
    
    static int linhaArquivo = 58; //linha que comeca as regras no arquivo fcl
    
    // estruturas de dados para manter as regras
    static String[] index, gordura, qot;

    //Parametros do ONS
    static String ra;
    static String xml;
    static int seed;
    static int calls;
    static int load;
    static String diretory;

    //Metrica que sera lida!
    static String metric;

    //Arquivo .fcl
    static String FCL_file;

    //O melhor e o minimo ou o maior (min ou max)
    static String best;
    
    public RetroFuzzy(String ra, String xml, int seed, int calls, int load, String diretory, String metric, String FCL_file, String best) {
        //Inincializa o cenário
        this.ra = ra;
        this.xml = xml;
        this.seed = seed;
        this.calls = calls;
        this.load = load;
        this.diretory = diretory;
        this.metric = metric;
        this.FCL_file = FCL_file;
        this.best = best;
    }
    
    /**
     * Metodo para dar start no retrofuzificacao
     */
    public void start() {
        // inicializa os termos, criando as combinacoes para
        // index e gordura, e setando qot todo como pessimo
        this.index = startIndex();
        this.gordura = startGordura();
        this.qot = startQot();
        this.times = 1;

        // executa 3 simulacoes dentro de 'getBestQoTResult'
        // e retorna o termo de menor bloqueio para qot[i].
        for (int i = 0; i < index.length; i++) {
            qot[i] = getBestQoTResult(i, index, gordura, qot, best);
            //Gravar no final pra garantir a ultima atualizacao do valor
            writeRules(index, gordura, qot);
        }
        printRules(index, gordura, qot);
        /*
        try {
            //Espera bizarra pois reclamava que o arquivo nao existia.
            Thread.sleep(0);
        } catch (InterruptedException ex) {
            Logger.getLogger(Execute.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
    }

    /**
     * Essa funcao executa uma simulacao para cada termo possivel de 'qot' e
     * retorna somente o termo que obteve melhor desempenho em termos de BBR.
     *
     * @param index //da rule
     * @param index2
     * @param gordura2
     * @param qot2
     * @return
     */
    private String getBestQoTResult(int i, String[] index, String[] gordura, String[] qot, String best) {
        
        String result = "pessimo";
        double bestValue;
        if(best.equals("max")) {
            bestValue = 0;
        } else {//estamos buscando o minimo
            bestValue = Double.MAX_VALUE;
        }
        
        //Tem que fazer esse laço tres vezez para cada regra
        for (int vezes = 0; vezes < 5; vezes++) {
            
            switch(vezes) {
                case 0:
                    qot[i] = "pessimo";
                    break;
                case 1:
                    qot[i] = "ruim";
                    break;
                case 2:
                    qot[i] = "razoavel";
                    break;
                case 3:
                    qot[i] = "bom";
                    break;
                case 4:
                    qot[i] = "excelente";
                    break;
            }
            
            //Escreve as regras atual
            //Parte de colocar as regras no arquivo 
            //Tem que gravar essas regras no arquivo <FCL_file> na linha linhaArquivo em diante...
            writeRules(index, gordura, qot);

            //Executa o simulador
            //Parte de executar ons em Linux e em Windows
            execute(ra, seed, load, calls, xml, diretory);

            //Ler o resultado
            //Parte de ler o resultado
            double value = jsonReader(metric, diretory, ra, seed, load);
            if (best.equals("max")) {
                if (bestValue <= value) {
                    bestValue = value;
                    result = qot[i];
                }
            } else {//estamos buscando o minimo
                if (bestValue >= value) {
                    bestValue = value;
                    result = qot[i];
                }
            }
        }
        
        System.out.println("Terminando a Regra: " + times++ + " Valor: \"" + result + "\" Valor da Metrica(" + metric + "): " + bestValue);

        return result;
    }

    /**
     * Essa funcao printa no console o resultado formatado do melhor conjunto de
     * regras
     *
     * @param index2
     * @param gordura2
     * @param qot2
     */
    private void printRules(String[] index, String[] gordura, String[] qot) {

        int countRule = 1;

        System.out.println("\n");
        
        for (int c = 0; c < index.length; c++) {
            System.out.println("RULE " + countRule++ + " : IF index IS " + index[c] + " AND gordura IS "
                    + gordura[c] + " THEN qot IS " + qot[c] + ";");
        }
        
        System.out.println("\n");
    }

    /**
     * Essa funcao retornar uma string do conjunto de regras
     *
     * @param index2
     * @param gordura2
     * @param qot2
     */
    private String[] printRulesString(String[] index, String[] gordura, String[] qot) {
        int countRule = 1;
        //São necessariamente 25 regras!
        String[] string = new String[25];
        int i = 0;
        
        for (int c = 0; c < index.length; c++) {
            string[i++] = "\tRULE " + countRule++ + " : IF index IS " + index[c] + " AND gordura IS " + gordura[c] + " THEN qot IS " + qot[c] + ";";
        }
        return string;
    }

    /**
     * Define todos os termos para 'qot' como já estão no arquivo.
     *
     * @return
     */
    private String[] startQot() {

        String[] values = readingRules();
        
        String[] qot = new String[25];
        
        for (int i = 0; i < values.length; i++) {
            qot[i] = values[i];
        }
        return qot;
    }

    /**
     * Define todos os termos para 'gordura'.
     *
     * @return
     */
    private String[] startGordura() {
        String[] gordura = new String[25];

        for (int i = 0; i < 25; i++) {
            if (i < 5) {
                gordura[i] = "p1";
            } else if (i < 10) {
                gordura[i] = "p2";
            } else if (i < 15) {
                gordura[i] = "p3";
            } else if (i < 20) {
                gordura[i] = "p4";
            } else if (i < 25) {
                gordura[i] = "p5";
            }
        }
        return gordura;
    }

    /**
     * Define todos os termos para 'index'.
     *
     * @return
     */
    private String[] startIndex() {

        String[] index = new String[25];

        for (int i = 0; i < 5; i++) {
            index[(i * 5)] = "i1";
            index[(i * 5) + 1] = "i2";
            index[(i * 5) + 2] = "i3";
            index[(i * 5) + 3] = "i4";
            index[(i * 5) + 4] = "i5";
        }

        return index;
    }

    /**
     * Esta função executa o ONS com os parametros informados
     *
     * @param ra
     * @param seed
     * @param load
     * @param calls
     * @param xml
     * @param diretory aonde sera gravado o arquivo json (dentro deste diretório
     * ainda sera criado uma pasta com o nome do <ra>_seeds)
     */
    private static void execute(String ra, int seed, int load, int calls, String xml, String diretory) {
        Execute exec = new Execute(ra, seed, load, calls, xml, diretory);
        exec.run();
    }

    /**
     * Este metodo ler o json criado pelo ONS e retorna o valor da metrica
     * solicitada
     *
     * @param metric metrica que sera solicitada para visualizacao
     * @param dir diretorio pais aonde esta gravado a pasta <ra>_seeds com os
     * arquivos json (Nesse caso especifico so 1)
     * @param ra
     * @param seed
     * @param load
     * @return
     */
    private static double jsonReader(String metric, String dir, String ra, int seed, int load) {
        File file = new File(dir + "/" + ra + "_seeds/" + ra + "_seed_" + seed + "_" + Double.parseDouble(Integer.toString(load)) + ".json");
        JSONObject jsonObject;
        JSONParser parser = new JSONParser();
        try {
            jsonObject = (JSONObject) parser.parse(new FileReader(file));

            if (jsonObject.get(metric) instanceof Double) {
                return (Double) jsonObject.get(metric);
            } else {
                if (jsonObject.get(metric) instanceof Long) {
                    return Double.parseDouble(jsonObject.get(metric).toString());
                } else {
                    throw (new IllegalArgumentException("One of json's results is not a Double  "));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(EncontraFCL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(EncontraFCL.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw (new IllegalArgumentException("This metric: \"" + metric + "\" was not found!!!"));
    }

    /**
     * Metodo que ler as regras no arquivo e retorna uma String com as metricas 
     * @param index
     * @param gordura
     * @param qot
     * @return 
     */
    private String[] readingRules() {
        //São necessariamente 25 regras!
        String[] string = new String[25];
        File file = new File(FCL_file);
        //Salvando todas as linhas do arquivo na memória
        List<String> fclFile;
        try {
            fclFile = Files.readAllLines(Paths.get(FCL_file));
            //pegando informação da linha linhaArquivo
            for (int i = 0; i < string.length; i++) {
                int index= fclFile.get((linhaArquivo-1)+i).lastIndexOf(" ");
                string[i] = fclFile.get((linhaArquivo-1)+i).substring(index+1).replaceAll(";", "");
            }
        } catch (IOException ex) {
            Logger.getLogger(RetroFuzzy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return string;
    }
    
    /**
     * Metodo que grava as regras no arquivo!
     * @param index
     * @param gordura
     * @param qot 
     */
    private void writeRules(String[] index, String[] gordura, String[] qot) {
        File file = new File(FCL_file);

        //Salvando todas as linhas do arquivo na memória
        List<String> fclFile;
        try {
            fclFile = Files.readAllLines(Paths.get(FCL_file));
            //pegando informação da linha linhaArquivo
            //String linhaArquivo = fclFile.get((linhaArquivo-1));
            
            String[] string = printRulesString(index, gordura, qot);
            for (int i = 0; i < string.length; i++) {
                fclFile.set(i+(linhaArquivo-1), string[i]);
            }
            
            //deletando o conteúdo do arquivo
            deletarConteudoTxt(file);

            //Escrevendo o novo conteúdo no arquivo txt.
            PrintWriter writer = new PrintWriter(file);
            for (int i = 0; i < fclFile.size(); i++) {
                writer.println(fclFile.get(i));

            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(RetroFuzzy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Classe que deleta o conteudo de um arquivo
     * @param file
     * @throws FileNotFoundException 
     */
    public static void deletarConteudoTxt(File file) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(file);
        writer.close();
    }

}
