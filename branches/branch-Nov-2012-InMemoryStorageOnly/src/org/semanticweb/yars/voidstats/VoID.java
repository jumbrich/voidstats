package org.semanticweb.yars.voidstats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;

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
import org.semanticweb.yars.util.CallbackNxBufferedWriter;

public class VoID {

	private static NxParser nxp;
	private static InputAnalyser ia;
	private static CountStmtAnalyser stmt;
	private static CountNodeTypeAnalyser nodeTypes;
	private static DistributionAnalyser<Node> distClasses;
	private static DistributionAnalyser<Node> distResSubjects;
	private static DistributionAnalyser<Node> distBNodeSubjects;
	private static DistributionAnalyser<Node> distProperties;
	private static DistributionAnalyser<Node> distResObjects;
	private static DistributionAnalyser<Node> distLitObjects;
	private static PropertyAnalyser<Node> propPartition;
	private static ClassAnalyser<Node> classPartition;
//	private static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
//	private static Callback cb = new CallbackNxBufferedWriter(bw);
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


	protected static void setCallBack(String s){
		File f = new File(s);
		try {
			bw = new BufferedWriter(new FileWriter(f));
			cb = new CallbackNxBufferedWriter(bw);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected static void runDefaultAnalysis(File f){
		try {
			nxp = new NxParser(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ia = new InputAnalyser(nxp);		
		
		// triple counter
		stmt = new CountStmtAnalyser(ia);		
		
		//count number of different node types
		nodeTypes = new CountNodeTypeAnalyser(stmt);		

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
				if (URIPattern.isEmpty())
					return n;
				else
					checkURIPattern(n[0]);
				return n;
			}
		};		
		
		// distinct classes
		int[] classes = { PRE, OBJ };
		NodeFilter[] classFilter = { new EqualsFilter(RDF.TYPE.toString()), new ClassFilter(Resource.class) };		
		distClasses = new DistributionAnalyser<Node>(nodeTypes, classFilter, classes, nt, true);

		// distinct resources in subject position
		int [] resSubNode = {SUB};
		NodeFilter[] resSubFilter = {new ClassFilter(Resource.class)};	
		distResSubjects =  new DistributionAnalyser<Node>(distClasses, resSubFilter, resSubNode, ntA, true);
		
		// distinct bNodes in subject position
		int [] bnoSubNode = {SUB};
		NodeFilter[] bnoSubFilter = {new ClassFilter(BNode.class)};	
		distBNodeSubjects =  new DistributionAnalyser<Node>(distResSubjects, bnoSubFilter, bnoSubNode, nt, true);		
		
		// distinct predicates (properties)
		int [] predicateNode = {PRE};
		NodeFilter[] predicateFilter = {new ClassFilter(Resource.class)};	
		distProperties =  new DistributionAnalyser<Node>(distBNodeSubjects, predicateFilter, predicateNode, ntA, true);		

		// distinct resources in the objects position
		int [] resObjNode = {OBJ};
		NodeFilter[] resObjFilter = {new ClassFilter(Resource.class)};	
		distResObjects =  new DistributionAnalyser<Node>(distProperties, resObjFilter, resObjNode, ntA, true);
		
		// distinct literals in the objects position
		int [] litObjNode = {OBJ};
		NodeFilter[] litObjFilter = {new ClassFilter(Literal.class)};	
		distLitObjects =  new DistributionAnalyser<Node>(distResObjects, litObjFilter, litObjNode, nt, true);
		
		// create property partitions
		int [] propPartNode = {PRE};
		NodeFilter[] propPartFilter = {new ClassFilter(Resource.class)};
		propPartition = new PropertyAnalyser<Node>(distLitObjects, propPartFilter, propPartNode, nt, false);
		
		// do first part of class partition 
		int [] classPartNode = {PRE, OBJ};
		NodeFilter[] classPartFilter = {new EqualsFilter(RDF.TYPE.toString()), new ClassFilter(Resource.class)};
		classPartition = new ClassAnalyser<Node>(propPartition, classPartFilter, classPartNode, nt, false);
		
		DefaultAnalyser run = classPartition;
		while (run.hasNext()) {
			run.next();
		}	
		PropertyAnalyser.closeBuffers();
		outPutDefaultVoId();
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
			if (node.contains(URIPattern))
				entities++;
	}
	
	public static void outPutDefaultVoId() {
		cb.processStatement(new Node[]{originalDataSet, RDF.TYPE, VOID.DATASET});
		cb.processStatement(new Node[]{dataSet, VOID.TRIPLES, new Literal("\""+stmt.getStmt()+"\"", XSD.INTEGER)});
		cb.processStatement(new Node[]{dataSet, VOID.CLASSES, new Literal("\""+distClasses.getStatsMap().size()+"\"", XSD.INTEGER)});
		cb.processStatement(new Node[]{dataSet, VOID.PROPERTIES, new Literal("\""+distProperties.getStatsMap().size()+"\"", XSD.INTEGER)});
		
		int distSubjects = distResSubjects.getStatsMap().size() + distBNodeSubjects.getStatsMap().size();
		cb.processStatement(new Node[]{dataSet, VOID.DISTINCTSUBJECTS, new Literal("\""+distSubjects+"\"", XSD.INTEGER)});
		
		int distObjects = distResObjects.getStatsMap().size() + distLitObjects.getStatsMap().size();
		cb.processStatement(new Node[]{dataSet, VOID.DISTINCTOBJECTS, new Literal("\""+distObjects+"\"", XSD.INTEGER)});

//		int distinctEntities = nodeTypes.getResourceMap().size() + nodeTypes.getBNodeMap().size() + nodeTypes.getLiteralMap().size();
//		cb.processStatement(new Node[]{dataSet, VOID.ENTITIES, new Literal("\""+distinctEntities+"\"", XSD.INTEGER)});
		
		cb.processStatement(new Node[]{dataSet, VOID.ENTITIES, new Literal("\""+entities+"\"", XSD.INTEGER)});

		for(Entry<String, Integer> ent: nodeTypes.vocab.vocabMap.entrySet()){
			cb.processStatement(new Node[]{dataSet, VOID.VOCABULARY, new Resource(ent.getKey())});
		}
		nodeTypes.vocab.vocabMap.clear();
	}	
	
}
