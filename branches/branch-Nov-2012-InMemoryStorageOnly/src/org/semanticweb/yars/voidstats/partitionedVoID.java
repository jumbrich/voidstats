package org.semanticweb.yars.voidstats;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

public class partitionedVoID {
	
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
	static final int SUB = 0;
	static final int PRE = 1;
	static final int OBJ = 2;	
	

	protected static void runAnalysis(File f) throws IOException, ParseException{

		nxp = new NxParser(new FileInputStream(f));

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
				Vocabs.createVocab(n[0]);
				return n;
			}
		};		
		
		//the analyser objects created below are similar to the ones created in the VoID class, except
		//this time we do not need to create any property or class data set buffers
		int[] classes = { PRE, OBJ };
		String filename = "distClassesA.nt.tmp";
		NodeFilter[] classFilter = { new EqualsFilter(RDF.TYPE.toString()), new ClassFilter(Resource.class) };		
		distClasses = new DistributionAnalyser<Node>(stmt, classFilter, classes, nt, true, filename, false);


		// distinct resources in subject position
		int [] resSubNode = {SUB};
		String filename1 = "distResSubjectsA.nt.tmp";
		NodeFilter[] resSubFilter = {new ClassFilter(Resource.class)};	
		distResSubjects =  new DistributionAnalyser<Node>(distClasses, resSubFilter, resSubNode, nt, true, filename1, true);

		
		// distinct bNodes in subject position
		int [] bnoSubNode = {SUB};
		String filename2 = "distBNodeSubjectsA.nt.tmp";
		NodeFilter[] bnoSubFilter = {new ClassFilter(BNode.class)};	
		distBNodeSubjects =  new DistributionAnalyser<Node>(distResSubjects, bnoSubFilter, bnoSubNode, nt, true, filename2, false);		
		
		// distinct predicates (properties)
		int [] predicateNode = {PRE};
		String filename3 = "distPropertiesA.nt.tmp";
		NodeFilter[] predicateFilter = {new ClassFilter(Resource.class)};	
		distProperties =  new DistributionAnalyser<Node>(distBNodeSubjects, predicateFilter, predicateNode, ntA, true, filename3, false);		

		// distinct resources in the objects position
		int [] resObjNode = {OBJ};
		String filename4 = "distResObjectsA.nt.tmp";
		NodeFilter[] resObjFilter = {new ClassFilter(Resource.class)};	
		distResObjects =  new DistributionAnalyser<Node>(distProperties, resObjFilter, resObjNode, nt, true, filename4, false);
		
		// distinct literals in the objects position
		int [] litObjNode = {OBJ};
		String filename5 = "distLitObjectsA.nt.tmp";
		NodeFilter[] litObjFilter = {new ClassFilter(Literal.class)};	
		distLitObjects =  new DistributionAnalyser<Node>(distResObjects, litObjFilter, litObjNode, nt, true, filename5, false);
		
		DefaultAnalyser run = distLitObjects;
		while (run.hasNext()) {
			run.next();
		}
		outPutUsingDiskAndMapStorage();
	}	
	

	private static void outPutUsingDiskStorage() throws IOException, ParseException{
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.TRIPLES, new Literal("\""+stmt.getStmt()+"\"", XSD.INTEGER)});
		
		int dc = distClasses.closeAndCount();
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.CLASSES, new Literal("\""+dc+"\"", XSD.INTEGER)});
		
		int dp = distProperties.closeAndCount();
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.PROPERTIES, new Literal("\""+dp+"\"", XSD.INTEGER)});
		
		int ds = distResSubjects.closeAndCount() + distBNodeSubjects.closeAndCount();
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.DISTINCTSUBJECTS, new Literal("\""+ds+"\"", XSD.INTEGER)});
		
		int dio = distResObjects.closeAndCount() + distLitObjects.closeAndCount();
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.DISTINCTOBJECTS, new Literal("\""+dio+"\"", XSD.INTEGER)});

//		int distinctEntities = nodeTypes.getResourceMap().size() + nodeTypes.getBNodeMap().size() + nodeTypes.getLiteralMap().size();
//		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.ENTITIES, new Literal("\""+distinctEntities+"\"", XSD.INTEGER)});

		for(Entry<String, Integer> ent: Vocabs.vocabMap.entrySet()){
			VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.VOCABULARY, new Resource(ent.getKey())});
		}
		Vocabs.vocabMap.clear();		
	}
	
	public static void outPutUsingMapStorage() throws IOException, ParseException {

		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.TRIPLES, new Literal("\""+stmt.getStmt()+"\"", XSD.INTEGER)});
		
		int drs = distClasses.getStatsMap().size();
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.CLASSES, new Literal("\""+drs+"\"", XSD.INTEGER)});
		
		int dp = distProperties.getStatsMap().size();
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.PROPERTIES, new Literal("\""+dp+"\"", XSD.INTEGER)});
		
		int ds = distResSubjects.getStatsMap().size() + distBNodeSubjects.getStatsMap().size();
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.DISTINCTSUBJECTS, new Literal("\""+ds+"\"", XSD.INTEGER)});
		
		int dio = distResObjects.getStatsMap().size() + distLitObjects.getStatsMap().size();
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.DISTINCTOBJECTS, new Literal("\""+dio+"\"", XSD.INTEGER)});

//		int distinctEntities = nodeTypes.getResourceMap().size() + nodeTypes.getBNodeMap().size() + nodeTypes.getLiteralMap().size();
//		cb.processStatement(new Node[]{dataSet, VOID.ENTITIES, new Literal("\""+distinctEntities+"\"", XSD.INTEGER)});

		for(Entry<String, Integer> ent: Vocabs.vocabMap.entrySet()){
			VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.VOCABULARY, new Resource(ent.getKey())});
		}
		Vocabs.vocabMap.clear();
	}
	
	public static void outPutUsingDiskAndMapStorage() throws IOException, ParseException {

		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.TRIPLES, new Literal("\""+stmt.getStmt()+"\"", XSD.INTEGER)});
		
		int drs = distClasses.getStatsMap().size();
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.CLASSES, new Literal("\""+drs+"\"", XSD.INTEGER)});
		
		int dp = distProperties.getStatsMap().size();
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.PROPERTIES, new Literal("\""+dp+"\"", XSD.INTEGER)});
		
		int ds = distResSubjects.closeAndCount() + distBNodeSubjects.getStatsMap().size();
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.DISTINCTSUBJECTS, new Literal("\""+ds+"\"", XSD.INTEGER)});
		
		int dio = distResObjects.getStatsMap().size() + distLitObjects.getStatsMap().size();
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.DISTINCTOBJECTS, new Literal("\""+dio+"\"", XSD.INTEGER)});

//		int distinctEntities = nodeTypes.getResourceMap().size() + nodeTypes.getBNodeMap().size() + nodeTypes.getLiteralMap().size();
//		cb.processStatement(new Node[]{dataSet, VOID.ENTITIES, new Literal("\""+distinctEntities+"\"", XSD.INTEGER)});

		for(Entry<String, Integer> ent: Vocabs.vocabMap.entrySet()){
			VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.VOCABULARY, new Resource(ent.getKey())});
		}
		Vocabs.vocabMap.clear();
	}
	
}
