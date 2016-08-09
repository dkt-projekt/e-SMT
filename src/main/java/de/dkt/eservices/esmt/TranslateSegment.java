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

	// hard-coded public variables: the location of the moses
    //public String pwd = "/Users/ansr01/Software/mosesdecoder-RELEASE-3.0/ankit_toy/4dkt/"; // for local machine
    public String pwd = "/usr/local/mt/WS_dkt/"; // for dkt server
    
	/**
	 * Method to send a shell script execution command translate to the server
	 * @param inputStr string to translate
	 * @param srclang id of the source language, ie language of the inputStr
	 * @param trglang id of the target language to be translated
	 * @param align boolean value to indicate obtaining alignment points while translating 
	 * 
	 * @return the output of the shell command, usually the translated sentence in target language
	 */
	public String executeCommandTranslate(String inputStr, String srclang, String trglang, boolean align){

    	
        
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
	
	/**
	 * Method to send a shell script execution command tokenization to the server
	 * @param inputStr string to translate
	 * @param ilang id of the language, ie language of the inputStr
	 * @param tokenize boolean value to indicate tokenize (true) or detokenize (false)
	 * 
	 * @return the output of the shell command, usually the tokenized or detokenized sentence in language
	 */
	public String executeCommandTokenize(String inputStr, String ilang, boolean tokenize){

    	
        
        File f = new File(pwd, "tempFile");  // temporary file created in pwd
        String command;

        
        // storing the inputStr in a file
        try {
            PrintWriter outf = new PrintWriter(new FileWriter(f));
            outf.println(inputStr);
            outf.close();
        }
        catch (IOException e) {}

        // The actual command to call the shell script with the 2 arguments: file lang
        if(tokenize) { // the output will be the tokenized
        	command = "sh " + pwd + "run_tokenize.sh -i " + f.getAbsoluteFile() + " -l " + ilang;
        }
        else {  // the output will be detokenized
        	command = "sh " + pwd + "run_detokenize.sh -i " + f.getAbsoluteFile() + " -l " + ilang;
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
	
	
	/**
	 * Method to send a shell script execution command casing to the server
	 * @param inputStr string to translate
	 * @param ilang id of the language, ie language of the inputStr
	 * @param case boolean value to indicate lowercasing (true) or recasing (false)
	 * 
	 * @return the output of the shell command, usually the lowercased or recased sentence in language
	 */
	public String executeCommandCase(String inputStr, String ilang, boolean lcase){

    	
        
        File f = new File(pwd, "tempFile");  // temporary file created in pwd
        String command;

        
        // storing the inputStr in a file
        try {
            PrintWriter outf = new PrintWriter(new FileWriter(f));
            outf.println(inputStr);
            outf.close();
        }
        catch (IOException e) {}

        // The actual command to call the shell script with the 2 arguments: file lang
        if(lcase) { // the output will be the lowercased
        	command = "sh " + pwd + "run_lowercase.sh -i " + f.getAbsoluteFile() + " -l " + ilang;
        }
        else {  // the output will be recased
        	command = "sh " + pwd + "run_recase.sh -i " + f.getAbsoluteFile() + " -l " + ilang;
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
	
	
	/**
	 * Method to send a shell script execution command splitting document to the server
	 * @param inputStr string to translate
	 * @param ilang id of the language, ie language of the inputStr
	 * 
	 * @return the output of the shell command, usually the docment slit into one sentence per line
	 */
	public String executeCommandSplit(String inputStr, String ilang){

    	
        
        File f = new File(pwd, "tempFile");  // temporary file created in pwd
        String command;

        
        // storing the inputStr in a file
        try {
            PrintWriter outf = new PrintWriter(new FileWriter(f));
            outf.println(inputStr);
            outf.close();
        }
        catch (IOException e) {}

        // The actual command to call the shell script with the 2 arguments: file lang
       
        command = "sh " + pwd + "run_sentsplit.sh -i " + f.getAbsoluteFile() + " -l " + ilang;
        
        

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
