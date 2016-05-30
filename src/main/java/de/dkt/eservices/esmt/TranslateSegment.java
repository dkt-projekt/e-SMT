package de.dkt.eservices.esmt;

/**
 * Created by ansr01 on 25/05/16.
 * Class encapsulating command for translating a segment
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TranslateSegment {

    public String executeCommand(String inputStr, String srclang){

        String command = "sh /Users/ansr01/Software/mosesdecoder-RELEASE-3.0/ankit_toy/4dkt/translate_main.sh -i " + inputStr + " -l " + srclang;
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
