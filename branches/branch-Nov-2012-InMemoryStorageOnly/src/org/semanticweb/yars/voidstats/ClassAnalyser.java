package org.semanticweb.yars.voidstats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.filter.NodeFilter;
import org.semanticweb.yars.nx.namespace.RDF;
import org.semanticweb.yars.nx.namespace.VOID;
import org.semanticweb.yars.nx.namespace.XSD;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;



public class ClassAnalyser<E> extends DefaultAnalyser{
	protected Count<E> _n;
	protected NodeTransformer<E> _np;
	private static BufferedWriter buffWriter;
	private static Map<Node, BufferedWriter> bufferMap = new HashMap<Node, BufferedWriter>();
	private static Map<Node, File> fileRefMap = new HashMap<Node, File>();
	
	private static NxParser nxp;
	private static Map<Node, Set<Node>> i2c = new HashMap<Node, Set<Node>>();
	
	public ClassAnalyser(Iterator<Node[]> in){
		super(in);
	}
	
	public ClassAnalyser(Analyser in, NodeTransformer<E> np, String ds){
		this(in, null, null, np, false);
	}
	
	public ClassAnalyser(Analyser in, NodeFilter[] key, NodeTransformer<E> np, String ds){
		this(in, key, null, np, false);
	}
	
	public ClassAnalyser(Analyser in, int[] element, NodeTransformer<E> np, String ds){
		this(in, null, element, np, false);
	}
	
	public ClassAnalyser(Analyser in, NodeFilter[] key, int[] element, NodeTransformer<E> np, boolean tn){
		super(in, key, element, tn);
		_n = new Count<E>();
		_np = np;
	}

	@Override
	public void stats() {
		;
	}

	public Map<E, Integer> getStatsMap(){
		return _n;
	}
	
	@Override
	public void analyse(Node[] n){
//		for(E e:_np.processNode(n)){
//			_n.add(e);
//		}		
		if (n[1].equals(RDF.TYPE)){
			Set<Node> classes = i2c.get(n[0]);
			if (classes == null){
				classes = new HashSet<Node>();
				i2c.put(n[0], classes);		
			}
			classes.add(n[2]);;
		}
	}
	
	protected static void writeToFiles(File inputFile){
		try {
			nxp = new NxParser(new FileInputStream(inputFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while (nxp.hasNext()){
			Node[] n = nxp.next();
			Set<Node> classes = i2c.get(n[0]);
			if(classes != null) for (Node c:classes){
				setBufferedWriter(c);
				writeToFiles(n);
			}
		}
		closeBuffers();
	}
	
	public static void setBufferedWriter(Node c){
		buffWriter = bufferMap.get(c);
		if (buffWriter == null){
			File f;
			try {					
				f = File.createTempFile(c.toString().replaceAll("/", ""), ".tmp");
				f.deleteOnExit();
				bufferMap.put(c, new BufferedWriter(new FileWriter(f)));
				fileRefMap.put(c, f);
				buffWriter = bufferMap.get(c);
			} catch (IOException e) {
				e.printStackTrace();
			}					
		}		
	}	
	
	/*
	 *  Each statement is written to the appropriate class buffer
	 */		
	public static void writeToFiles(Node[] in){
		for (int i=0; i<3; i++){
			String s = in[i].toN3();
//			s = s.replaceAll(System.getProperty("line.separator"),"");
//			s = s.replaceAll("\"","");
//			s = s.replaceAll("\\\\","");
			if(in[i] instanceof Resource){
				Resource r = new Resource(s);
				writeToBuff(r.toN3()+" ");
			} else if(in[i] instanceof BNode){
				BNode b = new BNode(s);
				writeToBuff(b.toN3()+" ");
			} else if(in[i] instanceof Literal){
				Literal l = new Literal(s);
				writeToBuff(l.toN3()+" ");				
			}
		}
		try {
			buffWriter.write(".");
			buffWriter.newLine();				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}		
	
	protected static void writeToBuff(String s){
		try {
			buffWriter.write(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	public static void closeBuffers(){
		for(Entry<Node, BufferedWriter> ent: bufferMap.entrySet()){
			buffWriter = ent.getValue(); 		
	    	try {
	    		if (buffWriter != null) {
	    			buffWriter.flush();
	    			buffWriter.close();
	    		}
	    	} catch (IOException ex) {
	    		ex.printStackTrace();
	    	}
		}
	}
	
	protected static void analyseTempClassDatasets() throws IOException, ParseException{
		VoID.cb.processStatement(new Node[]{VoID.originalDataSet, VOID.CLASSES, new Literal("\""+fileRefMap.size()+"\"", XSD.INTEGER)});
		for(Entry<Node, File> ent: fileRefMap.entrySet()){
			VoID.setDataSetName(ent.getKey().toString(), true);
			VoID.cb.processStatement(new Node[]{VoID.originalDataSet, VOID.CLASSPARTITION, VoID.dataSet});
			VoID.cb.processStatement(new Node[]{VoID.dataSet, RDF.TYPE, VOID.DATASET});
			VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.CLASS, new Resource(ent.getKey().toString())});
			partitionedVoID.runAnalysis(ent.getValue());
		}
	}	
	
}
