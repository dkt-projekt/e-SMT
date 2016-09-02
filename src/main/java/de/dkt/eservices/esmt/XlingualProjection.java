package de.dkt.eservices.esmt;

/**
 * Created by ansr01 on 22/07/16.
 * Class encapsulating command for configuring alignment points between source and target segments
 * This class will return a NIF model
 */

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import de.dkt.common.niftools.NIF;
import de.dkt.common.niftools.NIFReader;
import de.dkt.common.niftools.NIFWriter;


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
		
		String patternString = "\\s\\|(\\d+)\\-(\\d+)\\|";  // pattern for alignment points, the indexes (ref to source phrase) are grouped
		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(moses);
		
		int count=0; // how many times are we matching the pattern
		int istart=0; // index of input string
		String[] sourceWords = src.split(" ");
		int src_start = 0;
		int src_end = 0;
		String src_phr = new String();
		
		// Traverse through each of the matches
		// the numbers inside the matched pattern will give us the index of the source phrases
		// the string preceding he matched pattern will give us the corresponding target string translation
		while(matcher.find()) {
			count++;
			//System.out.println("found: " + count + " : " + matcher.start() + " - " + matcher.end() + " for " + matcher.group());
			
			src_start = new Integer(matcher.group(1)).intValue();
			src_end = new Integer(matcher.group(2)).intValue();
			//System.out.println("Srcphr: " + matcher.group(1) + " to " + matcher.group(2) + sourceWords[src_start] + " " + sourceWords[src_end]);
			
			//alignPoints.add(moses.substring(istart,matcher.start()) + " ||| " + istart + " ||| " + matcher.start());
			src_phr = new String(sourceWords[src_start]);
			for(int i=src_start+1; i<=src_end; i++){ // get the source phrases referenced by the alignment points
				src_phr += " " + sourceWords[i];
			}
			alignPoints.add(src_phr + " ||| " + moses.substring(istart,matcher.start())); // add the source phrase and the corresponding target string translation separated by |||
			istart = matcher.end() + 1;
		}
		//System.out.println("The number of times we match patterns is " + count);
		
		return alignPoints;
	}
	
	
	/**
	 * Method to take the output of the ExtractAlignments and input a list of source phrases, i.e. first part of each entry
	 * pre: 
	 * post:
	 * @param alignments
	 * @return source phrases
	 */
	public String[] getSrcPhrases(ArrayList<String> alignments){
		String[] phrases = new String[alignments.size()];
		
		String patternString = "\\s\\|\\|\\|";  // pattern for alignment points
		
		for (int i=0; i<alignments.size(); i++){
			String[] items = alignments.get(i).split(patternString);
			phrases[i] = items[0]; // get source phrase
			System.out.println(phrases[i]);
			
		}
		
		return phrases;
	}
	
	/**
	 * Method to take the output of the ExtractAlignments and input a list of target phrases, i.e. second part of each entry
	 * pre: 
	 * post:
	 * @param alignments
	 * @return target phrases
	 */
	public String[] getTrgPhrases(ArrayList<String> alignments){
		String[] phrases = new String[alignments.size()];
		
		String patternString = "\\s\\|\\|\\|";  // pattern for alignment points
		
		for (int i=0; i<alignments.size(); i++){
			String[] items = alignments.get(i).split(patternString);
			phrases[i] = items[1]; // get source phrase
			System.out.println(phrases[i]);
			
		}
		
		return phrases;
	}
	
	public static void main(String[] args) {
		//System.out.println(new XlingualProjection().getTarget(new String("This |0-0| is a |1-2| string . |3-4|")));
		//System.out.println("Done.");
		
		//String s = "Click the left button of the mouse .";
		//String t = "Klicken die linke taste die maus .";
		//String m = "Klicken |0-0| die linke |1-2| taste |3-4| die |5-5| maus . |6-7|";
		String s = "this is a small house .";
		String t = "das ist ein kleines haus .";
		String m = "das ist |0-1| ein kleines |2-3| haus . |4-5|";
		ArrayList<String> x = new XlingualProjection().ExtractAlignments(s,t,m);
		for (int i=0; i<x.size(); i++){
			System.out.println(x.get(i));
		}
	
		String[] y = new XlingualProjection().getSrcPhrases(x);
		int start = 0;
		int end = 0;
		
		for(int i=0; i < y.length; i++){ // Traverse through each phrase
        	end = start + y[i].length();
        	System.out.println(y[i] + " " + start + " " + end);
    		
    		start = end+1;
        }

    }

}
