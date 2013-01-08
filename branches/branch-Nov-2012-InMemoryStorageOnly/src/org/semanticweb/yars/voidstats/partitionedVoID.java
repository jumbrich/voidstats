package org.semanticweb.yars.voidstats;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class partitionedVoID {
	
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
	static final int SUB = 0;
	static final int PRE = 1;
	static final int OBJ = 2;	
	
	protected static void runAnalysis(File f){
		try {
			nxp = new NxParser(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
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
		
		//the analyser objects created below are similar to the ones created in the VoID class, except
		//this time we do not need to create any property or class data set buffers
		int[] classes = { PRE, OBJ };
		NodeFilter[] classFilter = { new EqualsFilter(RDF.TYPE.toString()), new ClassFilter(Resource.class) };		
		distClasses = new DistributionAnalyser<Node>(nodeTypes, classFilter, classes, nt, true);

		// distinct resources in subject position
		int [] resSubNode = {SUB};
		NodeFilter[] resSubFilter = {new ClassFilter(Resource.class)};	
		distResSubjects =  new DistributionAnalyser<Node>(distClasses, resSubFilter, resSubNode, nt, true);
		
		// distinct bNodes in subject position
		int [] bnoSubNode = {SUB};
		NodeFilter[] bnoSubFilter = {new ClassFilter(BNode.class)};	
		distBNodeSubjects =  new DistributionAnalyser<Node>(distResSubjects, bnoSubFilter, bnoSubNode, nt, true);		
		
		// distinct predicates (properties)
		int [] predicateNode = {PRE};
		NodeFilter[] predicateFilter = {new ClassFilter(Resource.class)};	
		distProperties =  new DistributionAnalyser<Node>(distBNodeSubjects, predicateFilter, predicateNode, nt, true);		

		// distinct resources in the objects position
		int [] resObjNode = {OBJ};
		NodeFilter[] resObjFilter = {new ClassFilter(Resource.class)};	
		distResObjects =  new DistributionAnalyser<Node>(distProperties, resObjFilter, resObjNode, nt, true);
		
		// distinct literals in the objects position
		int [] litObjNode = {OBJ};
		NodeFilter[] litObjFilter = {new ClassFilter(Literal.class)};	
		distLitObjects =  new DistributionAnalyser<Node>(distResObjects, litObjFilter, litObjNode, nt, true);
		
		DefaultAnalyser run = distLitObjects;
		while (run.hasNext()) {
			run.next();
		}
		
		outPutPartitionedVoId();
	}	
	
	private static void outPutPartitionedVoId(){
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.TRIPLES, new Literal("\""+stmt.getStmt()+"\"", XSD.INTEGER)});
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.CLASSES, new Literal("\""+distClasses.getStatsMap().size()+"\"", XSD.INTEGER)});
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.PROPERTIES, new Literal("\""+distProperties.getStatsMap().size()+"\"", XSD.INTEGER)});
		
		int distSubjects = distResSubjects.getStatsMap().size() + distBNodeSubjects.getStatsMap().size();
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.DISTINCTSUBJECTS, new Literal("\""+distSubjects+"\"", XSD.INTEGER)});
		
		int distObjects = distResObjects.getStatsMap().size() + distLitObjects.getStatsMap().size();
		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.DISTINCTOBJECTS, new Literal("\""+distObjects+"\"", XSD.INTEGER)});

//		int distinctEntities = nodeTypes.getResourceMap().size() + nodeTypes.getBNodeMap().size() + nodeTypes.getLiteralMap().size();
//		VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.ENTITIES, new Literal("\""+distinctEntities+"\"", XSD.INTEGER)});

		for(Entry<String, Integer> ent: nodeTypes.vocab.vocabMap.entrySet()){
			VoID.cb.processStatement(new Node[]{VoID.dataSet, VOID.VOCABULARY, new Resource(ent.getKey())});
		}
		nodeTypes.vocab.vocabMap.clear();		
	}
}
