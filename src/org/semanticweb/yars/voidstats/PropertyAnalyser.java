package org.semanticweb.yars.voidstats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.filter.NodeFilter;
import org.semanticweb.yars.nx.namespace.RDF;
import org.semanticweb.yars.nx.namespace.VOID;

public class PropertyAnalyser<E> extends DefaultAnalyser{
	protected Count<E> _n;
	protected NodeTransformer<E> _np;
	private static BufferedWriter buffWriter;
	private static Map<Node, File> fileRefMap = new HashMap<Node, File>();
	protected static Map<Node, BufferedWriter> bufferMap = new HashMap<Node, BufferedWriter>();
	
	public PropertyAnalyser(Iterator<Node[]> in){
		super(in);
	}
	
	public PropertyAnalyser(Analyser in, NodeTransformer<E> np, String ds){
		this(in, null, null, np, false);
	}
	
	public PropertyAnalyser(Analyser in, NodeFilter[] key, NodeTransformer<E> np, String ds){
		this(in, key, null, np, false);
	}
	
	public PropertyAnalyser(Analyser in, int[] element, NodeTransformer<E> np, String ds){
		this(in, null, element, np, false);
	}
	
	public PropertyAnalyser(Analyser in, NodeFilter[] key, int[] element, NodeTransformer<E> np, boolean tn){
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
	
	private static void setBufferWriter(Node n){
		if (bufferMap.get(n)==null){
			addBuffer(n);
		}
		buffWriter = bufferMap.get(n);
	}
	
	public static void addBuffer(Node n){
		String s = n.toString().replaceAll("/", "");
		if (s.length() > 132)
			s = s.substring(0, 132);
		File f;
		try {
			if (!bufferMap.containsKey(n)){
				f = File.createTempFile(s, ".tmp");
				f.deleteOnExit();
				bufferMap.put(n, new BufferedWriter(new FileWriter(f)));
				fileRefMap.put(n, f);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	@Override
	public void analyse(Node[] in){		
		setBufferWriter(in[1]);
		//for (int i=0; i<in.length; i++){
		for (int i=0; i<3; i++){
			String s = in[i].toN3();
			s = s.replaceAll(System.getProperty("line.separator"),"");
			s = s.replaceAll("\"","");
			s = s.replaceAll("\\\\","");
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
	
	protected void writeToBuff(String s){
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
	
	protected static void analyseTempPropertyDatasets(){
		for(Entry<Node, File> ent: fileRefMap.entrySet()){
			VoID.setDataSetName(ent.getKey().toString(), true);
			VoID.cb.processStatement(new Node[]{VoID.originalDataSet, VOID.PROPERTYPARTITION, VoID.dataSet});
			VoID.cb.processStatement(new Node[]{VoID.dataSet, RDF.TYPE, VOID.DATASET});
			VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.PROPERTY, new Resource(ent.getKey().toString())});
			partitionedVoID.runAnalysis(ent.getValue());			
		}
	}	
}
