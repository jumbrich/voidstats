package org.deri.voidstats.vvoid.partition;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import org.deri.voidstats.Main;
import org.deri.voidstats.anl.disk.OnDiskListUnique;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.mem.MemoryManager;
import org.semanticweb.yars.nx.namespace.RDF;
import org.semanticweb.yars.nx.parser.ParseException;

public class SortedClassPartitionAnalyser extends PartitionAnalyser {
	public static String TMP_BUFFER_FN = "buffer.nx";
	public static final int DEFAULT_BUF_SIZE = (int)(MemoryManager.estimateMaxStatements(10)*Main.IN_MEM_LOAD_FACTOR);
	
	private static final Logger logger = Logger.getLogger(SortedClassPartitionAnalyser.class.getName());
	
	static {
		logger.info("Using "+DEFAULT_BUF_SIZE+" default buffer size for "+SortedClassPartitionAnalyser.class.getName());
	}
	
	OnDiskListUnique buffer;
	int bufSize = DEFAULT_BUF_SIZE;
	String bufFileName;
	
	HashSet<Node> classes;
	
	Node[] prev = null;
	
	public SortedClassPartitionAnalyser(String directory) throws IOException {
		this(directory, DEFAULT_BUF_SIZE);
	}

	public SortedClassPartitionAnalyser(String directory, int bufSize) throws IOException {
		super(directory);
		bufFileName = directory+"/"+TMP_BUFFER_FN;
		initBuffer();
		classes = new HashSet<Node>(); 
	}
	
	private void initBuffer() throws IOException{
		buffer = new OnDiskListUnique(bufFileName);
	}

	private void partitionAndClearBuffer() throws FileNotFoundException, IOException, ParseException{
		Iterator<Node[]> buf = buffer.closeAndList();
		ArrayList<Partition> ps = new ArrayList<Partition>();
		for(Node c:classes){
			ps.add(getOrCreatePartition(c));
		}
		
		while(buf.hasNext()){
			Node[] next = buf.next();
			for(Partition p:ps){
				p.processStatement(next);
				done++;
				if(ticks>0 && done%ticks==0){
					logger.info("... class partitions ... written "+done+".");
				}
			}
		}
		
		classes.clear();
	}

	public void stats() {
		;
	}
	
	public void closeWrite() throws IOException{
		//need to clean up last buffer
		try{
			partitionAndClearBuffer();
		} catch(ParseException e){
			throw new IOException(e);
		}
		super.closeWrite();
	}

	public void analyse(Node... na) throws AnalyseException {
		try{
			if(prev!=null && !prev[0].equals(na[0]) && !classes.isEmpty()){
				partitionAndClearBuffer();
				initBuffer();
			}
			
			if(na[1].equals(RDF.TYPE) && na[2] instanceof Resource){
				classes.add(na[2]);
			}
			buffer.add(na);
		} catch(Exception e){
			throw new RuntimeException(e); //not easily recoverable
		}
		
		prev = na;
	}
}
