package org.semanticweb.yars.voidstats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.filter.NodeFilter;
import org.semanticweb.yars.nx.namespace.RDF;
import org.semanticweb.yars.nx.namespace.VOID;

import org.semanticweb.yars.nx.namespace.XSD;
import org.semanticweb.yars.nx.parser.ParseException;



public class PropertyAnalyser<E> extends DefaultAnalyser{
	protected Count<E> _n;
	protected NodeTransformer<E> _np;
	private static BufferedWriter buffWriter;
	private static Map<String, File> fileRefMap = new HashMap<String, File>();
	protected static Map<String, BufferedWriter> bufferMap = new HashMap<String, BufferedWriter>();
	
	private static final Logger logger = Logger.getLogger(PropertyAnalyser.class.getName());
	
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
	
	private static void setBufferWriter(String s){
		if (bufferMap.get(s)==null){
			addBuffer(s);
		}
		buffWriter = bufferMap.get(s);
	}
	
	public static void addBuffer(String s){
		File f;
		try {
			if (!bufferMap.containsKey(s)){
				f = File.createTempFile(s, ".tmp");
				f.deleteOnExit();
				bufferMap.put(s, new BufferedWriter(new FileWriter(f)));
				fileRefMap.put(s, f);;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	@Override
	public void analyse(Node[] in){
		String n = in[1].toString().replaceAll("/", "");
		if (n.length() > 100)
			n = n.substring(0, 100);		
		setBufferWriter(n);
//		//for (int i=0; i<in.length; i++){
//		for (int i=0; i<3; i++){
//			String s = in[i].toN3();
//			s = s.replaceAll(System.getProperty("line.separator"),"");
//			s = s.replaceAll("\"","");
//			s = s.replaceAll("\\\\","");
//			if(in[i] instanceof Resource){
//				Resource r = new Resource(s);
//				writeToBuff(r.toN3()+" ");
//			} else if(in[i] instanceof BNode){
//				BNode b = new BNode(s);
//				writeToBuff(b.toN3()+" ");
//			} else if(in[i] instanceof Literal){
//				Literal l = new Literal(s);
//				writeToBuff(l.toN3()+" ");				
//			}
//		}		
		try {
			buffWriter.write(Nodes.toN3(in));
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
		for(Entry<String, BufferedWriter> ent: bufferMap.entrySet()){
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
	

	protected static void analyseTempPropertyDatasets() throws IOException, ParseException{
		VoID.cb.processStatement(new Node[]{VoID.originalDataSet, VOID.PROPERTIES, new Literal("\""+fileRefMap.size()+"\"", XSD.INTEGER)});
		for(Entry<String, File> ent: fileRefMap.entrySet()){
			VoID.setDataSetName(ent.getKey().toString(), true);
			VoID.cb.processStatement(new Node[]{VoID.originalDataSet, VOID.PROPERTYPARTITION, VoID.dataSet});
			VoID.cb.processStatement(new Node[]{VoID.dataSet, RDF.TYPE, VOID.DATASET});
			VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.PROPERTY, new Resource(ent.getKey().toString())});
			partitionedVoID.runAnalysis(ent.getValue());			
		}
	}

}
