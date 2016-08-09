package de.dkt.eservices.esmt;

/**
 * Created by ansr01 on 22/07/16.
 * Class encapsulating command for configuring alignment points between source and target segments
 * This class will return a NIF model
 */

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class XlingualProjection {
	
	
	/**
	 * Method to take the output of the moses trace option and return the target string without the integer indexes
	 * pre: \w+\s|\d-\d|\s\w+\s|\d-\d|
	 * post: \w+\s\w+\s\w+
	 * @param inputS
	 * @return inputS without the numeric integers indicating alignment points
	 */
	public String getTarget(String inputS){
		String outputS = new String();
		
		String patternString = "\\s\\|\\d+\\-\\d+\\|";  // pattern for alignment points
		
		// split the sentence on the alignment points and then concatenate array elements into the output string
		// calling String.split(regex)
		String[] sentence = inputS.split(patternString);
		for(int i=0; i<sentence.length; i++){
			outputS += sentence[i];
		}
		return outputS;
	}
	
	
	
	/**
	 * Method to take as input source, target, output of moses -t and output the alignment points covering both source and target character spans
	 * Format of each element of the ArrayList: src_begin-src_end=trg_begin-trg_end; where these are character indices in the String
	 * @param src string in the source language
	 * @param trg string in the target language
	 * @param moses output of the moses -t ie target string with source span word alignments
	 * @return an Array of source-target character span alignment points where indexes refer to the corresponding word number in the src and trg
	 */
	public ArrayList<String> ExtractAlignments(String src, String trg, String moses){
		ArrayList<String> alignPoints = new ArrayList<String>();
		
		String patternString = "\\s\\|\\d+\\-\\d+\\|";  // pattern for alignment points
		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(moses);
		
		int count=0; // how many times are we matching the pattern
		int istart=0; // index of input string
		
		while(matcher.find()) {
			count++;
			System.out.println("found: " + count + " : " + matcher.start() + " - " + matcher.end() + " for " + matcher.group());
			alignPoints.add(moses.substring(istart,matcher.start()) + " ||| " + istart + " ||| " + matcher.start());
			istart = matcher.end() + 1;
		}
		System.out.println("The number of times we match patterns is " + count);
		
		return alignPoints;
	}
	
	public static void main(String[] args) {
		//System.out.println(new XlingualProjection().getTarget(new String("This |0-0| is a |1-2| string . |3-4|")));
		//System.out.println("Done.");
		
		/**String s = "Click the left button of the mouse.";
		String t = "Klicken die linke taste die maus.";
		String m = "Klicken |0-0| die linke |1-2| taste |3-4| die |5-5| maus . |6-7|";
		ArrayList<String> x = new XlingualProjection().ExtractAlignments(s,t,m);
		for (int i=0; i<x.size(); i++){
			System.out.println(x.get(i));
		}**/

    }

}
