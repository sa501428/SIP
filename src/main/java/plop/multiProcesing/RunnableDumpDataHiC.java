package plop.multiProcesing;
import plop.process.DumpData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Runnable class dumping the raw data set via juicer tool.
 * The data are dumped by chr and by windows size, with user's parameters choose.
 * 
 * two sorts of file file will be created one for the raw value with the observed minus expected value and also with the diatnce normalized value.
 * Then the "norm" vector is also dump in an other deirectory.
 * 
 * @author axel poulet
 *
 */
public class RunnableDumpDataHiC extends Thread implements Runnable{
	/**String: path where save the dump data  */
	private String _outdir ="";
	/**String: name of the chr*/
	private String _chrName = "";
	/**int: chr size */
	private int _chrSize = 0;
	/** DumpData object run juicertoolbox.jar*/
	private DumpData _dumpData;
	/**int: bin resolution*/
	private int _res = 0;
	/**int: image Size */
	private int _matrixSize = 0;
	/**int: size of the step to run a chr */
	private int _step = 0;
	/** */
	private ArrayList<Integer> _listFactor;
	
	
	/**
	 * Constructor, initialize the variables of interest
	 *  
	 * @param outdir
	 * @param chrName
	 * @param chrSize
	 * @param dumpData
	 * @param res
	 * @param matrixSize
	 * @param step
	 */
	public RunnableDumpDataHiC (String outdir, String chrName, int chrSize, DumpData dumpData,int res, int matrixSize, int step, ArrayList<Integer> listFactor){
		this._outdir = outdir;
		this._chrName = chrName;
		this._chrSize = chrSize;
		this._res = res;
		this._matrixSize = matrixSize;
		this._step = step;
		this._dumpData = dumpData;
		this._listFactor = listFactor;
	}
	
	/**
	 * Dump teh data by chr
	 */
	public void run(){
		boolean juicerTools;
		for(int indexFact = 0; indexFact < this._listFactor.size(); ++indexFact) {
			int res = _res*this._listFactor.get(indexFact);
			int matrixSize = _matrixSize/this._listFactor.get(indexFact);
			_step = matrixSize/2;
			String nameRes = String.valueOf(res);
			nameRes = nameRes.replace("000", "");
			nameRes = nameRes+"kb";
			String outdir = this._outdir+nameRes+File.separator+this._chrName+File.separator;
			File file = new File(outdir);
			if (file.exists()==false) file.mkdirs();
			int step = this._step*res;
			int j = matrixSize*res;
			String test = this._chrName+":0:"+j;
			String name = outdir+this._chrName+"_0_"+j+".txt";
			this._dumpData.getExpected(test,name,res);
			String normOutput = this._outdir+nameRes+File.separator+"normVector";
			file = new File(normOutput);
			if (file.exists()==false) file.mkdir();
			try {
				this._dumpData.getNormVector(this._chrName,normOutput+File.separator+this._chrName+".norm",res);
				System.out.println("start dump "+this._chrName+" size "+this._chrSize+" res "+ nameRes);
				if(j > this._chrSize) j = this._chrSize;
				for(int i = 0 ; j-1 <= this._chrSize; i+=step,j+=step){
					int end =j-1;
					String dump = this._chrName+":"+i+":"+end;
					name = outdir+this._chrName+"_"+i+"_"+end+".txt";
					System.out.println("start dump "+this._chrName+" size "+this._chrSize+" dump "+dump+" res "+ nameRes);
					juicerTools = this._dumpData.dumpObservedMExpected(dump,name,res);
				if(j+step > this._chrSize && j < this._chrSize){
					j= this._chrSize;
					i+=step;
					dump = this._chrName+":"+i+":"+j;
					name = outdir+this._chrName+"_"+i+"_"+j+".txt";
					System.out.println("start dump "+this._chrName+" size "+this._chrSize+" dump "+dump+" res "+ nameRes);
					juicerTools = this._dumpData.dumpObservedMExpected(dump,name,res);

				}
			}
			System.out.println("##### End dump "+this._chrName+" "+nameRes);
			} catch (IOException | InterruptedException e) { e.printStackTrace(); }
		}
		System.gc();
	}
}