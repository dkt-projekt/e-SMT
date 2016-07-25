package de.dkt.eservices.esmt;

/**
 * Created by ansr01 on 25/05/16.
 * Class encapsulating command for translating a segment, i.e. invoking shell script for moses from Java
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.io.FileWriter;
import java.io.IOException;
import de.dkt.eservices.erattlesnakenlp.modules.Sparqler;

public class TranslateSegment {

    public String executeCommand(String inputStr, String srclang, String trglang, boolean align){

    	
        // hard-coded variables: the location of the moses
        String pwd = "/Users/ansr01/Software/mosesdecoder-RELEASE-3.0/ankit_toy/4dkt/"; // for local machine
        //String pwd = "/usr/local/mt/WS_dkt/"; // for dkt server
        File f = new File(pwd, "tempFile");  // temporary file created in pwd
        String command;

        //System.out.println("I have landed\n");
        // testing sparqler for MT-NER Linking
        //System.out.println(Sparqler.getDBPediaLabelForLanguage(new String("http://dbpedia.org/resource/Paint_(software)"), new String("de")));
        
        
        // storing the inputStr in a file
        try {
            PrintWriter outf = new PrintWriter(new FileWriter(f));
            outf.println(inputStr);
            outf.close();
        }
        catch (IOException e) {}

        // The actual command to call the shell script with the 3 arguments: file source and target
        if(align) { // the output will be the translated segment with alignment points
        	command = "sh " + pwd + "translate_align.sh -i " + f.getAbsoluteFile() + " -s " + srclang + " -t " + trglang;
        }
        else {  // the output will bw the translated segment as in a vanilla translation service
        	command = "sh " + pwd + "translate_main.sh -i " + f.getAbsoluteFile() + " -s " + srclang + " -t " + trglang;
        }
        

        // Executing the command using a Process object
        StringBuffer output = new StringBuffer();
        Process p;

        try{
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            //BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("UTF-8")));
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            //System.out.println("I have received output from translation\n");

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
                //System.out.println("arre:" + line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public static void main(String[] args) {

    }
}
