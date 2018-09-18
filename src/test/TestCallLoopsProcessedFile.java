package test;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import utils.WholeGenomeAnalysis;

public class TestCallLoopsProcessedFile {

	/**	
	 * Main function to run all the process, can be run with gui or in command line.
	 * With command line with 1 or less than 5 parameter => run only the help
	 * With zero parameter only java -jar noname.jar  => gui
	 * With more than 5 paramter => command line mode
	 * 
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		//String input = "/home/plop/Bureau/DataSetImageHiC/GM12878/subsample/GM12878_100mil/";
		//String output= "/home/plop/Bureau/DataSetImageHiC/GM12878/subsample/100mil_test/";
		//String output= "/home/plop/Bureau/DataSetImageHiC/Hichip_H3k4me1_test";
		//String input= "/home/plop/Bureau/DataSetImageHiC/Hichip_H3k4me1_test";
		///home/plop/Bureau/DataSetImageHiC/Hichip_H3k4me1_test
		//String output= "/home/plop/Bureau/DataSetImageHiC/GM12878/chr2_fullData";
		//String input = "/home/plop/Bureau/DataSetImageHiC/GM12878/subsample/GM12878_full/";
		String input = "/home/plop/Bureau/test/";
		String output = "/home/plop/Bureau/test/";
		int matrixSize = 2000;
		int resolution = 5000;
		int diagSize = 6;
		double gauss = 1;
		int thresholdMax = 3250;
		boolean isObserved = true;
		int nbZero = 6;
		double min =1.5;
		double max =1.5;
		double saturatedPixel =0.05;
		ArrayList<Integer> factor = new ArrayList<Integer>();
		factor.add(1);
		factor.add(2);
		factor.add(5);
		System.out.println("input "+input+"\n"
			+ "output "+output+"\n"
			+ "gauss "+gauss+"\n"
			+ "min "+min+"\n"
			+ "max "+max+"\n"
			+ "matrix size "+matrixSize+"\n"
			+ "diag size "+diagSize+"\n"
			+ "resolution "+resolution+"\n"
			+ "saturated pixel "+saturatedPixel+"\n"
			+ "threshold "+thresholdMax+"\n"
			+ "isObserved "+isObserved+"\n");
	
		File file = new File(output);
		if (file.exists()==false) 
			file.mkdir();
		///home/plop/Documents/Genome/HumanGenomeHg19/hg19_withoutChr.sizes
		///home/plop/Documents/Genome/hg38.chr2.sizes
		WholeGenomeAnalysis wga = new WholeGenomeAnalysis(output, readChrSizeFile("/home/plop/Documents/Genome/HumanGenomeHg19/chr20.size"), gauss, min, max, 
				resolution, saturatedPixel, thresholdMax, diagSize, matrixSize,nbZero, factor);

		if(isObserved) wga.run("o",input);
		else wga.run("oMe",input);
						
		System.out.println("End");
	}
			
	/**
	 * 
	 * @param chrSizeFile
	 * @return 
	 * @throws IOException
	 */
	private static HashMap<String, Integer> readChrSizeFile( String chrSizeFile) throws IOException{
		HashMap<String,Integer> chrSize =  new HashMap<String,Integer>();
		BufferedReader br = new BufferedReader(new FileReader(chrSizeFile));
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();
		while (line != null){
			sb.append(line);
			String[] parts = line.split("\\t");				
			String chr = parts[0]; 
			int size = Integer.parseInt(parts[1]);
			
			chrSize.put(chr, size);
			sb.append(System.lineSeparator());
			line = br.readLine();
		}
		br.close();
		return  chrSize;
	} 
}	
