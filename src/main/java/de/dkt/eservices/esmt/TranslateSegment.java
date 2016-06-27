package de.dkt.eservices.esmt;

/**
 * Created by ansr01 on 25/05/16.
 * Class encapsulating command for translating a segment, i.e. invoking shell script for moses from Java
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TranslateSegment {

    public String executeCommand(String inputStr, String srclang, String trglang){

    	
        // hard-coded variables: the location of the moses
        //String pwd = "/Users/ansr01/Software/mosesdecoder-RELEASE-3.0/ankit_toy/4dkt/"; // for local machine
        String pwd = "/usr/local/mt/WS_dkt/"; // for dkt server
        File f = new File(pwd, "tempFil");  // temporary file created in pwd

        // storing the inputStr in a file
        try {
            PrintWriter outf = new PrintWriter(new FileWriter(f));
            outf.println(inputStr);
            outf.close();
        }
        catch (IOException e) {}

        // The actual command to call the shell script with the 2 arguments
        String command = "sh " + pwd + "translate_main.sh -i " + f.getAbsoluteFile() + " -s " + srclang + " -t " + trglang;

        // Executing the command using a Process object
        StringBuffer output = new StringBuffer();
        Process p;

        try{
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public static void main(String[] args) {

    }
}
