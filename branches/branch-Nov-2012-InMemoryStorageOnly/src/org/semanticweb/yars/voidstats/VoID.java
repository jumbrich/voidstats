package org.semanticweb.yars.voidstats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.filter.NodeFilter;
import org.semanticweb.yars.nx.filter.NodeFilter.ClassFilter;
import org.semanticweb.yars.nx.filter.NodeFilter.EqualsFilter;
import org.semanticweb.yars.nx.namespace.RDF;
import org.semanticweb.yars.nx.namespace.VOID;
import org.semanticweb.yars.nx.namespace.XSD;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars.util.CallbackNxBufferedWriter;

public class VoID {

	private static NxParser nxp;
	private static InputAnalyser ia;
	private static CountStmtAnalyser stmt;
//	private static CountNodeTypeAnalyser nodeTypes;
	private static DistributionAnalyser<Node> distClasses;
	private static DistributionAnalyser<Node> distResSubjects;
	private static DistributionAnalyser<Node> distBNodeSubjects;
	private static DistributionAnalyser<Node> distProperties;
	private static DistributionAnalyser<Node> distResObjects;
	private static DistributionAnalyser<Node> distLitObjects;
	private static PropertyAnalyser<Node> propPartition;
//	private static ClassAnalyser<Node> classPartition;
	protected static BufferedWriter bw;
	protected static Callback cb;
	
	static final int SUB = 0;
	static final int PRE = 1;
	static final int OBJ = 2;
	protected static Node dataSet;
	protected static Node originalDataSet;
	protected static String URIPattern;
	private static Integer dsRef = 0;
	private static long entities;
	private static long lineCount = 0;
	private static final Logger logger = Logger.getLogger(VoID.class.getName());

	/**
	 * @param s
	 * @throws IOException 
	 */
	protected static void setCallBack(String s) throws IOException{
		File f = new File(s);
		bw = new BufferedWriter(new FileWriter(f));
		cb = new CallbackNxBufferedWriter(bw);
	}
	

	/**
	 * @param f
	 * @throws IOException
	 * @throws ParseException
	 * 
	 */
	protected static void runDefaultAnalysis(File f) throws IOException, ParseException{
		
		if (Main.isInputGzipped()){
			nxp = new NxParser(new GZIPInputStream(new FileInputStream(f)));
		}else{
			nxp = new NxParser(new FileInputStream(f));
		}

		ia = new InputAnalyser(nxp);		
		
		// triple counter
		stmt = new CountStmtAnalyser(ia);		
		
		//count number of different node types
		//nodeTypes = new CountNodeTypeAnalyser(stmt);

		//NodeTransformer accepts the filtered node array and allows you to manipulate it
		//a separate transformer could be created for each analyser object
		NodeTransformer<Node> nt = new NodeTransformer<Node>() {
			@Override
			public Node[] processNode(Node[] n) {
				return n;
			}
		};
		
		NodeTransformer<Node> ntA = new NodeTransformer<Node>() {
			@Override
			public Node[] processNode(Node[] n) {
				checkURIPattern(n[0]);
				Vocabs.createVocab(n[0]);
				return n;
			}
		};
		
		NodeTransformer<Node> ntB = new NodeTransformer<Node>() {
			@Override
			public Node[] processNode(Node[] n) {
				checkURIPattern(n[0]);
				return n;
			}
		};		
		
		// distinct classes
//		int[] classes = { PRE, OBJ };
//		String filename = "distClasses.nt.tmp";
//		NodeFilter[] classFilter = { new EqualsFilter(RDF.TYPE.toString()), new ClassFilter(Resource.class) };		
//		distClasses = new DistributionAnalyser<Node>(stmt, classFilter, classes, nt, true, filename, false);

		// distinct resources in subject position
		int [] resSubNode = {SUB};
		String filename1 = "distResSubjects.nt.tmp";
		NodeFilter[] resSubFilter = {new ClassFilter(Resource.class)};	
		distResSubjects =  new DistributionAnalyser<Node>(stmt, resSubFilter, resSubNode, ntB, true, filename1, true);
		
		// distinct bNodes in subject position
		int [] bnoSubNode = {SUB};
		String filename2 = "distBNodeSubjects.nt.tmp";
		NodeFilter[] bnoSubFilter = {new ClassFilter(BNode.class)};	
		distBNodeSubjects =  new DistributionAnalyser<Node>(distResSubjects, bnoSubFilter, bnoSubNode, nt, true, filename2, true);		
		
		// distinct predicates (properties)
		int [] predicateNode = {PRE};
		String filename3 = "distProperties.nt.tmp";
		NodeFilter[] predicateFilter = {new ClassFilter(Resource.class)};	
		distProperties =  new DistributionAnalyser<Node>(distBNodeSubjects, predicateFilter, predicateNode, ntA, true, filename3, true);		

		// distinct resources in the objects position
		int [] resObjNode = {OBJ};
		String filename4 = "distResObjects.nt.tmp";
		NodeFilter[] resObjFilter = {new ClassFilter(Resource.class)};	
		distResObjects =  new DistributionAnalyser<Node>(distProperties, resObjFilter, resObjNode, ntB, true, filename4, true);
		
		// distinct literals in the objects position
		int [] litObjNode = {OBJ};
		String filename5 = "distLitObjects.nt.tmp";
		NodeFilter[] litObjFilter = {new ClassFilter(Literal.class)};	
		distLitObjects =  new DistributionAnalyser<Node>(distResObjects, litObjFilter, litObjNode, nt, true, filename5, true);

		// create property partitions
		int [] propPartNode = {PRE};
		NodeFilter[] propPartFilter = {new ClassFilter(Resource.class)};
		propPartition = new PropertyAnalyser<Node>(distLitObjects, propPartFilter, propPartNode, nt, false);		
		
		// do first part of class partition 
//		int [] classPartNode = {PRE, OBJ};
//		NodeFilter[] classPartFilter = {new EqualsFilter(RDF.TYPE.toString()), new ClassFilter(Resource.class)};
//		classPartition = new ClassAnalyserA<Node>(propPartition, classPartFilter, classPartNode, nt, false);		
		
		DefaultAnalyser run = propPartition;
		while (run.hasNext()) {
			run.next();
			lineCount++;
			if (lineCount % 10000 == 0)
				logger.info("VoID Stmt no: "+lineCount);
		}
		PropertyAnalyser.closeBuffers();
		outPutUsingDiskStorage();
	}
	
	protected static void setDataSetName(String s, boolean isPartition){
		if (!isPartition){
			dataSet = new Resource(s);
			originalDataSet = new Resource(s);
		}else{
			dsRef++;
			dataSet = new BNode(dsRef.toString());
		}
	}
	
	private static void checkURIPattern(Node n){
			String node = n.toString();
			if (URIPattern.isEmpty())
				return;
			if (node.contains(URIPattern))
				entities++;
	}
	

	public static void outPutUsingMapStorage() throws IOException, ParseException {
		cb.processStatement(new Node[]{originalDataSet, RDF.TYPE, VOID.DATASET});
		cb.processStatement(new Node[]{dataSet, VOID.TRIPLES, new Literal("\""+stmt.getStmt()+"\"", XSD.INTEGER)});		
//		cb.processStatement(new Node[]{dataSet, VOID.CLASSES, new Literal("\""+distClasses.getStatsMap().size()+"\"", XSD.INTEGER)});
//		cb.processStatement(new Node[]{dataSet, VOID.PROPERTIES, new Literal("\""+distProperties.getStatsMap().size()+"\"", XSD.INTEGER)});		
		
		int ds = distResSubjects.getStatsMap().size() + distBNodeSubjects.getStatsMap().size();
		cb.processStatement(new Node[]{dataSet, VOID.DISTINCTSUBJECTS, new Literal("\""+ds+"\"", XSD.INTEGER)});
		
		int dio = distResObjects.getStatsMap().size() + distLitObjects.getStatsMap().size();
		cb.processStatement(new Node[]{dataSet, VOID.DISTINCTOBJECTS, new Literal("\""+dio+"\"", XSD.INTEGER)});
		
		cb.processStatement(new Node[]{dataSet, VOID.ENTITIES, new Literal("\""+entities+"\"", XSD.INTEGER)});		

//		for(Entry<Node, Integer> ent: distClasses.getStatsMap().entrySet()){
//			cb.processStatement(new Node[]{dataSet, VOID.CLASS, new Resource(ent.getKey().toString())});
//		}
		
//		for(Entry<Node, Integer> ent: distProperties.getStatsMap().entrySet()){
//			cb.processStatement(new Node[]{dataSet, VOID.PROPERTY, new Resource(ent.getKey().toString())});
//		}		
		
		for(Entry<String, Integer> ent: Vocabs.vocabMap.entrySet()){
			cb.processStatement(new Node[]{dataSet, VOID.VOCABULARY, new Resource(ent.getKey())});
		}
		Vocabs.vocabMap.clear();
	}
	
	public static void outPutUsingDiskStorage() throws IOException, ParseException {

		cb.processStatement(new Node[]{originalDataSet, RDF.TYPE, VOID.DATASET});
		cb.processStatement(new Node[]{dataSet, VOID.TRIPLES, new Literal("\""+stmt.getStmt()+"\"", XSD.INTEGER)});
		
//		int drs = distClasses.closeAndCount();
//		cb.processStatement(new Node[]{dataSet, VOID.CLASSES, new Literal("\""+drs+"\"", XSD.INTEGER)});
	
//		int dp = distProperties.closeAndCount();
//		cb.processStatement(new Node[]{dataSet, VOID.PROPERTIES, new Literal("\""+dp+"\"", XSD.INTEGER)});		
		
		int ds = distResSubjects.closeAndCount() + distBNodeSubjects.closeAndCount();
		cb.processStatement(new Node[]{dataSet, VOID.DISTINCTSUBJECTS, new Literal("\""+ds+"\"", XSD.INTEGER)});		
		
		int dio = distResObjects.closeAndCount() + distLitObjects.closeAndCount();
		cb.processStatement(new Node[]{dataSet, VOID.DISTINCTOBJECTS, new Literal("\""+dio+"\"", XSD.INTEGER)});
		
		cb.processStatement(new Node[]{dataSet, VOID.ENTITIES, new Literal("\""+entities+"\"", XSD.INTEGER)});
		
		// use this if we are omitting class partition analysis, as this outputs classes only
//		for(Entry<Node, Integer> ent: distClasses.getStatsMap().entrySet()){
//			cb.processStatement(new Node[]{dataSet, VOID.CLASS, new Resource(ent.getKey().toString())});
//		}		

		for(Entry<String, Integer> ent: Vocabs.vocabMap.entrySet()){
			cb.processStatement(new Node[]{dataSet, VOID.VOCABULARY, new Resource(ent.getKey())});
		}
		Vocabs.vocabMap.clear();
		
	}	
	
}
