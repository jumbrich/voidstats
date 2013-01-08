package org.deri.voidstats.vvoid;

import java.util.Iterator;

import org.deri.voidstats.Main;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.mem.MemoryManager;
import org.semanticweb.yars.nx.namespace.VOID;
import org.semanticweb.yars.nx.namespace.XSD;
import org.semanticweb.yars.nx.parser.Callback;

public class VoIDCallback implements Callback { 
	private Callback cb;
	private Resource dataset;
	
	static int DEFAULT_CACHE_SIZE = (int)(MemoryManager.estimateMaxStatements(8) * Main.IN_MEM_LOAD_FACTOR);
	
	public VoIDCallback(Callback cb, Resource dataset){
		this.cb = cb;
		this.dataset = dataset;
	}

	public void endDocument() {
		cb.endDocument();
	}

	public void processStatement(Node[] arg0) {
		cb.processStatement(arg0);
	}

	public void startDocument() {
		cb.startDocument();	
	}

	public void writeDistinctSubjectCount(long l){
		cb.processStatement(new Node[]{dataset, VOID.DISTINCTSUBJECTS, toIntegerLit(l)});
	}
	
	public void writeDistinctObjectCount(long l){
		cb.processStatement(new Node[]{dataset, VOID.DISTINCTOBJECTS, toIntegerLit(l)});
	}
	
	public void writePropertyCount(long l){
		cb.processStatement(new Node[]{dataset, VOID.PROPERTIES, toIntegerLit(l)});
	}
	
	public void writeClassCount(long l){
		cb.processStatement(new Node[]{dataset, VOID.CLASSES, toIntegerLit(l)});
	}
	
	public void writeTripleCount(long l){
		cb.processStatement(new Node[]{dataset, VOID.TRIPLES, toIntegerLit(l)});
	}
	
	public void writeDocumentCount(long l){
		cb.processStatement(new Node[]{dataset, VOID.DOCUMENTS, toIntegerLit(l)});
	}
	
	public void writeEntityCount(long l){
		cb.processStatement(new Node[]{dataset, VOID.ENTITIES, toIntegerLit(l)});
	}

	public void writeVocabularies(Iterator<Node[]> vocabs){
		while(vocabs.hasNext()){
			cb.processStatement(new Node[]{dataset, VOID.VOCABULARY, vocabs.next()[0]});
		}
	}
	
	public void writePropertyPartitionLink(Resource ppDataset){
		cb.processStatement(new Node[]{dataset, VOID.PROPERTYPARTITION, ppDataset});
	}
	
	public void writeClassPartitionLink(Resource cpDataset){
		cb.processStatement(new Node[]{dataset, VOID.CLASSPARTITION, cpDataset});
	}
	
	public void writePropertyForPartition(Resource prop){
		cb.processStatement(new Node[]{dataset, VOID.PROPERTY, prop});
	}
		
	public void writeClassForPartition(Resource clas){
		cb.processStatement(new Node[]{dataset, VOID.CLASS, clas});
	}
	
	private static final Literal toIntegerLit(long l){
		return new Literal("\""+l+"\"",XSD.INTEGER);
	}
}
