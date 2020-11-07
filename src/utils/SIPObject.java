package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


/**
 * Analyse and detect a whole genome HiC with .hic file or already processed data.
 * The class is used for the observed and oMe method.
 * 
 * MorpholibJ method used
 * 
 * Collection of mathematical morphology methods and plugins for ImageJ, created at the INRA-IJPB Modeling and Digital Imaging lab.
 * David Legland, Ignacio Arganda-Carreras, Philippe Andrey; MorphoLibJ: integrated library and plugins for mathematical morphology with ImageJ.
 * Bioinformatics 2016; 32 (22): 3532-3534. doi: 10.1093/bioinformatics/btw413
 * 
 * @author axel poulet
 *
 */
public class SIPObject {
	/** String path of the input data*/
	private String _input;
	/** Path of the output file*/
	private String _output;
	/** Strength of the gaussian filter*/
	private double _gauss;
	/** Strength of the min filter*/
	private double _min;
	/** Strength of the max filter*/
	private double _max;
	/** % of staurated pixel after enhance contrast*/
	private double _saturatedPixel;
	/** Image size*/
	private int _matrixSize = 0;
	/** Resolution of the bin dump in base*/
	private int _resolution;
	/** Threshold for the maxima detection*/
	private int _thresholdMaxima;
	/** HashMap of the chr size, the key = chr name, value = size of chr*/
	private HashMap<String,Integer> _chrSize =  new HashMap<String,Integer>();
	/** Diage size to removed maxima close to diagonal*/
	private int _diagonalSize;
	/** Size of the step to process each chr (step = matrixSize/2)*/
	private int _step;
	/** Number of pixel = 0 allowed around the loop*/
	private int _nbZero = -1;
	/** list of the image resolution to find loop*/
	private ArrayList<Integer> _listFactor = new ArrayList<Integer>();
	/** fdr value */
	private double _fdr;
	/** is processed booelan*/
	private boolean _isProcessed = false;
	/** if is gui analysis*/
	private boolean _isGui = false;
	/** if true regional fdr will be not used*/
	private boolean _isDroso = false;
	/** median of loop AP score */
	private double _medianAP = 0;
	/** median of loop regional AP score */
	private double _medianAPReg = 0;
	/** true is mcool input*/
	private boolean _isCooler = false;


	/**
	 *
	 */
	public SIPObject() { }

	/**
	 * SIPObject constructor for hic or mcool file
	 * 
	 *
	 * @param output path file with the file created by the first step of SIP
	 * @param chrSize hashMap name chr => chr size
	 * @param gauss gaussian filter strength
	 * @param min minimum filter strength
	 * @param max maximum filter strength
	 * @param resolution bin size
	 * @param saturatedPixel percentage of saturated pixel for image histogram normalization
	 * @param thresholdMax threshold for loop detection with findMaxima
	 * @param diagonalSize size of the diagonal, where the value will be not use
	 * @param matrixSize size of the image
	 * @param nbZero number of zero allowed around loops
	 * @param listFactor multi resolution calling loops used this list of factor
	 * @param fdr fdr value for final loops filtering
	 * @param isProcessed true if processed SIP data input else false
	 * @param rFDR false if it isn't drosophila input
	 */
	public SIPObject(String output, HashMap<String, Integer> chrSize, double gauss, double min,
			double max, int resolution, double saturatedPixel, int thresholdMax,
			int diagonalSize, int matrixSize, int nbZero,ArrayList<Integer> listFactor,
			double fdr, boolean isProcessed, boolean rFDR) {
		if(!output.endsWith(File.separator))
			output = output+File.separator;
		this._output = output;
		this._input = output;
		this._chrSize = chrSize;
		this._gauss = gauss;
		this._min = min;
		this._max = max;
		this._matrixSize = matrixSize;
		this._resolution = resolution;
		this._saturatedPixel = saturatedPixel;
		this._thresholdMaxima = thresholdMax;
		this._diagonalSize = diagonalSize;
		this._step = matrixSize/2;
		this._nbZero = nbZero;
		this._listFactor = listFactor;
		this._fdr = fdr;
		this._isProcessed = isProcessed;
		this._isDroso = rFDR;
	}

	/**
	 * SIPObject constructor for processed SIP data
	 *
	 * @param input path file with the file created by the first step of SIP
	 * @param output  path file for the results
	 * @param chrSize hashMap name chr => chr size
	 * @param gauss gaussian filter strength
	 * @param min minimum filter strength
	 * @param max maximum filter strength
	 * @param resolution bin size
	 * @param saturatedPixel percentage of saturated pixel for image histogram normalization
	 * @param thresholdMax threshold for loop detection with findMaxima
	 * @param diagonalSize size of the diagonal, where the value will be not use
	 * @param matrixSize size of the image
	 * @param nbZero number of zero allowed around loops
	 * @param listFactor multi resolution calling loops used this list of factor
	 * @param fdr fdr value for final loops filtering
	 * @param isProcessed true if processed SIP data input else false
	 * @param rFDR false if it isn't drosophila input
	 */
	public SIPObject(String input, String output, HashMap<String, Integer> chrSize, double gauss, double min,
			double max, int resolution, double saturatedPixel, int thresholdMax,
			int diagonalSize, int matrixSize, int nbZero,ArrayList<Integer> listFactor,
			double fdr, boolean isProcessed, boolean rFDR) {
		if(!output.endsWith(File.separator))
			output = output+File.separator;
		if(!input.endsWith(File.separator))
			input = input+File.separator;
		this._output = output;
		this._input = input;
		this._chrSize = chrSize;
		this._gauss = gauss;
		this._min = min;
		this._max = max;
		this._matrixSize = matrixSize;
		this._resolution = resolution;
		this._saturatedPixel = saturatedPixel;
		this._thresholdMaxima = thresholdMax;
		this._diagonalSize = diagonalSize;
		this._step = matrixSize/2;
		this._nbZero = nbZero;
		this._listFactor = listFactor;
		this._fdr = fdr;
		this._isProcessed = isProcessed;
		this._isDroso = rFDR;
	}
	
	
	/**
	 * Save the result file in tabulated file
	 * 
	 * @param pathFile String path for the results file
	 * @param first boolean to know idf it is teh first chromo
	 * @param data hashMap loop name => Loop object
	 * @throws IOException exception
	 */
	public void saveFile(String pathFile, HashMap<String,Loop> data, boolean first) throws IOException{
		FDR fdrDetection = new FDR (this._fdr, data);
		fdrDetection.run();
		double RFDRcutoff = fdrDetection.getRFDRCutoff();
		double FDRcutoff = fdrDetection.getFDRCutoff();
		boolean supToTen = false;
		if(this._isDroso){ 
			median(data,FDRcutoff);
			System.out.println("Filtering value at "+this._fdr+" FDR is "+FDRcutoff+" APscore ");
			if(_medianAPReg > 10){
				supToTen = true;
				 _medianAPReg = _medianAPReg/4;
				 _medianAP = _medianAP/10;
			}
		}
		else 
			System.out.println("Filtering value at "+this._fdr+" FDR is "+FDRcutoff+" APscore and "+RFDRcutoff+" RegionalAPscore\n");
		BufferedWriter writer;
		if(first) writer = new BufferedWriter(new FileWriter(new File(pathFile), true));
		else{
			writer = new BufferedWriter(new FileWriter(new File(pathFile)));
			writer.write("chromosome1\tx1\tx2\tchromosome2\ty1\ty2\tcolor\tAPScoreAvg\tProbabilityofEnrichment\tRegAPScoreAvg\tAvg_diffMaxNeihgboor_1\tAvg_diffMaxNeihgboor_2\tavg\tstd\tvalue\n");
		}
		
		if(data.size()>0){
			Set<String> key = data.keySet();
			Iterator<String> it = key.iterator();
			while (it.hasNext()){
				String name = it.next();
				Loop loop = data.get(name);
				ArrayList<Integer> coord = loop.getCoordinates();
				if(this._isDroso){
					if(loop.getPaScoreAvg() > FDRcutoff && loop.getPaScoreAvgdev() > .9 && (loop.getNeigbhoord1() > 1 || loop.getNeigbhoord2() > 1)){
						if(supToTen){
							if(loop.getRegionalPaScoreAvg() >= (_medianAPReg-_medianAPReg*0.7) && loop.getRegionalPaScoreAvg() <= (_medianAPReg*2)&& loop.getPaScoreAvg() <= (_medianAP*2)){
								writer.write(loop.getChr()+"\t"+coord.get(2)+"\t"+coord.get(3)+"\t"+loop.getChr()+"\t"+coord.get(0)+"\t"+coord.get(1)+"\t0,0,0"
									+"\t"+loop.getPaScoreAvg()+"\t"+loop.getPaScoreAvgdev()+"\t"+loop.getRegionalPaScoreAvg()+"\t"
									+loop.getNeigbhoord1()+"\t"+loop.getNeigbhoord2()+"\t"+loop.getAvg()+"\t"
									+loop.getStd()+"\t"+loop.getValue()+"\n");
							}
						}else{
							if( loop.getRegionalPaScoreAvg() >= (_medianAPReg-_medianAPReg*0.5) && loop.getRegionalPaScoreAvg() <= (_medianAPReg*2)&& loop.getPaScoreAvg() <= (_medianAP*2)){
								writer.write(loop.getChr()+"\t"+coord.get(2)+"\t"+coord.get(3)+"\t"+loop.getChr()+"\t"+coord.get(0)+"\t"+coord.get(1)+"\t0,0,0"
								+"\t"+loop.getPaScoreAvg()+"\t"+loop.getPaScoreAvgdev()+"\t"+loop.getRegionalPaScoreAvg()+"\t"
								+loop.getNeigbhoord1()+"\t"+loop.getNeigbhoord2()+"\t"+loop.getAvg()+"\t"
								+loop.getStd()+"\t"+loop.getValue()+"\n");
							}
						}
					}
				}else{
					if(loop.getPaScoreAvg() > FDRcutoff && loop.getRegionalPaScoreAvg() > RFDRcutoff && loop.getPaScoreAvgdev() > .9){
						writer.write(loop.getChr()+"\t"+coord.get(2)+"\t"+coord.get(3)+"\t"+loop.getChr()+"\t"+coord.get(0)+"\t"+coord.get(1)+"\t0,0,0"
							+"\t"+loop.getPaScoreAvg()+"\t"+loop.getPaScoreAvgdev()+"\t"+loop.getRegionalPaScoreAvg()+"\t"
							+loop.getNeigbhoord1()+"\t"+loop.getNeigbhoord2()+"\t"+loop.getAvg()+"\t"
							+loop.getStd()+"\t"+loop.getValue()+"\n");
					}
				}
			}
		writer.close();
		}
	}


	/**
	 * Test the normalized vector by chromosome and return a hashMap with biased coordinate.
	 * if loops is detected around this region in theis hashMap the loop will be deleted
	 *
	 * @param normFile normalized file
	 */
	public HashMap<Integer, String> getNormValueFilter(String normFile){
		BufferedReader br;
		int lineNumber = 0;
		HashMap<Integer, String> vector = new HashMap<Integer, String>();
		try {
			br = new BufferedReader(new FileReader(normFile));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null){
				sb.append(line);
				if((line.equals("NaN")|| line.equals("NAN") || line.equals("nan") || line.equals("na")  || Double.parseDouble(line) < 0.30)){
					vector.put(lineNumber*this._resolution, "plop");
				}
				++lineNumber;
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) { e.printStackTrace();}
		return vector;
	}


	/**
	 * compute median of pa score and regional pa score for a set of loops
	 *
	 * @param data hashMap loops name=> Loop object
	 * @param fdrCutoff fdr cutoff
	 */
	private void median(HashMap<String,Loop> data, double fdrCutoff){
		Set<String> key = data.keySet();
		Iterator<String> it = key.iterator();
		ArrayList<Float> n1 = new ArrayList<Float> ();
		ArrayList<Float> n2 = new ArrayList<Float> ();
		int nb = 0;
		while (it.hasNext()){
			String name = it.next();
			Loop loop = data.get(name);
			if(loop.getPaScoreAvg() > fdrCutoff && loop.getPaScoreAvgdev() > .9){
				n1.add(loop.getPaScoreAvg());
				n2.add(loop.getRegionalPaScoreAvg());
				nb++;
			}
		}
		if(nb>0){
			n1.sort(Comparator.naturalOrder());
			n2.sort(Comparator.naturalOrder());
			double pos1 = Math.floor((n1.size() - 1.0) / 2.0);
			double pos2 = Math.ceil((n1.size() - 1.0) / 2.0);
			if (pos1 == pos2 ) 	_medianAP = n1.get((int)pos1);
			else _medianAP = (n1.get((int)pos1) + n1.get((int)pos2)) / 2.0 ;
			pos1 = Math.floor((n2.size() - 1.0) / 2.0);
			pos2 = Math.ceil((n2.size() - 1.0) / 2.0);
			if (pos1 == pos2 ) 	_medianAPReg = n2.get((int)pos1);
			else _medianAPReg = (n2.get((int)pos1) + n2.get((int)pos2)) / 2.0 ;
			System.out.println("AP\t"+_medianAP+"\nAPREG\t"+_medianAPReg);
		}
	}

	/**
	 * getter of fdr parameter
	 * @return fdr value
	 */
	public double getFdr() { return	this._fdr; }

	/**
	 * setter of fdr value
	 * @param fdr new fdr value
	 *
	 */
	public void setFdr(double fdr) { this._fdr = fdr; }
	/**
	 * Getter of the input dir
	 * @return path of the input dir
	 */
	public String getInputDir(){ return this._input; }
	
	/**
	 * Getter of the matrix size
	 * @return the size of the image
	 */
	public int getMatrixSize(){ return this._matrixSize; }

	
	/**
	 * Getter of step 
	 * @return the step
	 */
	public int getStep(){ return this._step;}
	
	/**
	 * Setter of the path of the input directory
	 * @param inputDir String of the input directory
	 */
	public void setInputDir(String inputDir){ this._input = inputDir; }

	/**
	 * Getter of the path of the output directory
	 * @return path 
	 */
	public String getOutputDir(){ return this._output; }

	/**
	 * Setter of the path of the output directory
	 * @param outputDir path of output directory
	 */
	public void setOutputDir(String outputDir){	this._output = outputDir;}

	/**
	 * Getter of the gaussian blur strength
	 * @return double gaussian filter strength
	 */
	public double getGauss(){ return this._gauss; }
	
	/**
	 * Setter of the gaussian blur strength
	 * @param gauss new gaussian filter strength
	 */
	public void setGauss(double gauss){ this._gauss = gauss; }
	
	/**
	 * Getter of diagonalSize
	 * @return integer size of diagonal
	 */
	public int getDiagonalSize(){ return this._diagonalSize;}
	/**
	 * Setter of the diagonal size
	 * @param diagonalSize int of the size of the diagonal
	 */
	public void setDiagonalSize(int diagonalSize){ 	this._diagonalSize = diagonalSize; }
	
	/**
	 * Getter of the min filter strength
	 * @return double strength of the min filter
	 */
	public double getMin(){ return this._min;}

	/**
	 * Setter of the min filter strength
	 * @param min new filter min strength
	 */
	public void setMin(double min){ this._min = min;}

	/**
	 * Getter of the max filter strength
	 * @return double max filter
	 */
	public double getMax(){	return this._max; }
	
	/**
	 * Setter of the min filter strength
	 * @param max double max filter
	 */
	public void setMax(double max){	this._max = max;}

	/**
	 * Getter % of saturated pixel for the contrast enhancement
	 * @return double percentage of saturated pixel
	 */
	public double getSaturatedPixel(){ return this._saturatedPixel; }

	/**
	 * Setter % of saturated pixel for the contrast enhancement
	 * @param saturatedPixel double percentage of saturated pixel
	 */
	public void setSaturatedPixel(double saturatedPixel){ this._saturatedPixel = saturatedPixel; }

	/**
	 * Getter of resolution of the bin 
	 * @return bin size
	 */
	public int getResolution(){	return this._resolution;}
	
	/**
	 * Setter of resolution of the bin 
	 * @param resolution bin size
	 */
	public void setResolution(int resolution){	this._resolution = resolution;}

	/**
	 * Setter of size of the matrix 
	 * @param size image size
	 */
	public void setMatrixSize(int size){ this._matrixSize = size; }
	
	/**
	 * setter step between image 
	 * @param step int step
	 */
	public void setStep(int step){ this._step = step;}
	
	/**
	 * Getter of threshold for the loop detection
	 * @return threshold
	 */
	public int getThresholdMaxima(){ return _thresholdMaxima;}
	/**
	 * Setter of threshold for the detection of the maxima
	 * @param thresholdMaxima threshold
	 */
	public void setThresholdMaxima(int thresholdMaxima) { this._thresholdMaxima = thresholdMaxima;}


	/**
	 * Getter of getNbZero 
	 * @return int nb of zero allowed around the loops
	 */
	public int getNbZero(){ return this._nbZero;}

	/**
	 * setter of nb of zero
	 * @param nbZero int nb of zero allowed around the loops
	 */
	public void setNbZero(int nbZero){ this._nbZero = nbZero;}
	
	/**
	 * Getter of list of integer for multi resolution loop calling
	 * @return list of integer
	 */
	public ArrayList<Integer> getListFactor() {return this._listFactor;}

	/**
	 * boolean isDroso
	 * @return boolean
	 */
	public boolean isDroso(){return this._isDroso;}

	/**
	 * setter
	 * @param droso boolean
	 */
	public void setIsDroso(boolean droso){	this._isDroso = droso;}

	/**
	 * getter of chrSize hashMap
	 * @return hashMap chr name => chr size
	 */
	public HashMap<String,Integer> getChrSizeHashMap(){return this._chrSize;}

	/**
	 * setter
	 * @param chrSize  hashMap chr name => chr size
	 */
	public void setChrSizeHashMap(HashMap<String,Integer> chrSize){this._chrSize = chrSize;}
	
	/**
	 * getter boolean isProcessed
	 * true input is SIP processed dat
	 * @return boolean
	 */
	public boolean isProcessed() { return _isProcessed;}

	/**
	 * setter boolean isProcessed
	 * @param isProcessed boolean
	 */
	public void setIsProcessed(boolean isProcessed) { this._isProcessed = isProcessed;}
	
	/**
	 * getter isCooler
	 * true: input is mcool dataset
	 * @return boolean
	 */
	public boolean isCooler() { return _isCooler;}

	/**
	 * setter isCooler
	 * @param cool boolean
	 */
	public void setIsCooler(boolean cool) { this._isCooler = cool;}

	/**
	 * getter isGui
	 * true: program run with GUI
	 * @return boolean
	 */
	public boolean isGui() { return _isGui;}

	/**
	 * setter isGui
	 * @param _isGui boolean
	 */

	public void setIsGui(boolean _isGui) { this._isGui = _isGui;}
}