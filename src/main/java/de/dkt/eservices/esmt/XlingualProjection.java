package de.dkt.eservices.esmt;

/**
 * Created by ansr01 on 22/07/16.
 * Class encapsulating command for configuring alignment points between source and target segments
 * This class will return a NIF model
 */


public class XlingualProjection {
	
	
	/**
	 * Method to take the output of the moses trace option and return the target string without the integer indexes
	 * pre: \w+ |\d-\d| \w+ |\d-\d|
	 * post: \w+ \w+ \w+
	 * @param inputS
	 * @return onputS without the numeric integers indicating alignment points
	 */
	public String getTarget(String inputS){
		String outputS = new String();
		
		
		return outputS;
	}
	
	// Method to take as input source, target, output of moses -t and output the alignment points
	// Format of the alignment points
	public String ExtractAlignments(String src, String trg, String moses){
		String alignPoints = new String();
		
		return alignPoints;
	}
	
	public static void main(String[] args) {

    }

}
