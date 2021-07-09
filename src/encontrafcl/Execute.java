/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encontrafcl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lucasrc
 */
public class Execute implements Runnable {

    private String raClass;
    private int seed;
    private int calls;
    private double load;
    private String xml;
    private String dir;

    public Execute(String raClass, int seed, double load, int calls, String xml, String dir) {
        this.raClass = raClass;
        this.seed = seed;
        this.load = load;
        this.calls = calls;
        this.xml = xml;
        this.dir = dir;
        Runtime r = Runtime.getRuntime();
        Process process;
        try {
            
            String os = System.getProperty("os.name").toLowerCase();
            if (os.indexOf("win") >= 0) {
                // Windows Commands
                File f = new File(dir);
                if (f.exists() && f.isDirectory()) {
                    process = r.exec("bash.exe -c \"rm -rf " + dir + "\"");
                }
                process = r.exec("bash.exe -c \"mkdir -p " + dir + "/" + raClass + "_seeds" + "\"");
            } else {
                // Linux Commands
                process = r.exec("rm -rf " + dir);
                process = r.exec("mkdir -p " + dir + "/" + raClass + "_seeds");
            }
        } catch (IOException ex) {
            Logger.getLogger(Execute.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try {
            Runtime r = Runtime.getRuntime();
            Process process;
            BufferedReader br;
            String saida;
            File file;
            
            String command = "java -jar ons.jar -f " + this.xml + " -s " + seed + " -ra " + raClass + " -c " + calls + " -l " + load + " -json";
            process = r.exec(command);
            
            /*
            try {
                //Espera bizarra pois reclamava que o arquivo nao existia.
                Thread.sleep(0);
            } catch (InterruptedException ex) {
                Logger.getLogger(Execute.class.getName()).log(Level.SEVERE, null, ex);
            }
            */

            file = new File(dir + "/" + raClass + "_seeds" + "/" + raClass + "_seed_" + seed + "_" + load + ".json");
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);

            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            saida = null;
            while ((saida = br.readLine()) != null) {
                bw.write(saida);
                bw.newLine();
            }
            br.close();
            bw.close();
            fw.close();
            
            //process = r.exec(new String[]{"sh", "-c", "mv "+ ExecuteONS.dir + "/"+ raClass +"_seed* "+ ExecuteONS.dir + "/"+raClass+"_seeds"});
            
            //System.out.println("\nEnd of execution - Class: "+raClass+ " Seed: " + seed + " Load: " + load);

        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }
}
