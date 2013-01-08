package org.deri.voidstats.vvoid;

import java.util.Iterator;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.mem.MemoryManager;
import org.semanticweb.yars.nx.parser.Callback;

public class VoIDOptions {
	
	public static final String DEFAULT_TMP_DIR = "voidStats/";
	public static final int DEFAULT_TICKS = 100000;
	public static final boolean DEFAULT_PARTITION_PROPERTIES = false;
	public static final boolean DEFAULT_PARTITION_CLASSES = false;
	public static final boolean DEFAULT_COUNT_DOCS = false;
	
	public static final int DEFAULT_MAX_IN_MEM_PARTITION = MemoryManager.estimateMaxStatements(10);
	
	private Iterator<Node[]> input;
	private Callback output;
	
	private String dataset;
	private String uriPattern = null;
	
	private boolean countDocs = DEFAULT_COUNT_DOCS;

	private String tmpDir = DEFAULT_TMP_DIR;
	private int ticks = DEFAULT_TICKS;
	
	private int maxInMemPartition = DEFAULT_MAX_IN_MEM_PARTITION;
	
	private boolean partitionProperties = DEFAULT_PARTITION_PROPERTIES;
	private boolean partitionClasses = DEFAULT_PARTITION_CLASSES;
	
	private static final Logger logger = Logger.getLogger(VoIDOptions.class.getName());
	
	static {
		logger.info("Using "+DEFAULT_MAX_IN_MEM_PARTITION+" default in-memory partition size for "+VoIDOptions.class.getName());
	}
	
	public VoIDOptions(Iterator<Node[]> in, Callback out, String dataset){
		this.input = in;
		this.output = out;
		this.dataset = dataset;
	}
	

	public String getDataset() {
		return dataset;
	}
	
	public void setUriPattern(String uriPattern) {
		this.uriPattern = uriPattern;
	}

	public String getUriPattern() {
		return uriPattern;
	}
	
	public void setTmpDir(String dir){
		this.tmpDir = dir;
	}
	
	public String getTmpDir(){
		return tmpDir;
	}

	public int getTicks() {
		return ticks;
	}

	public void setTicks(int ticks) {
		this.ticks = ticks;
	}

	public boolean doPartitionProperties() {
		return partitionProperties;
	}

	public void setPartitionProperties(boolean partitionProperties) {
		this.partitionProperties = partitionProperties;
	}

	public boolean doPartitionClasses() {
		return partitionClasses;
	}

	public void setPartitionClasses(boolean partitionClasses) {
		this.partitionClasses = partitionClasses;
	}
	
	public boolean doCountDocs() {
		return countDocs;
	}

	public void setCountDocs(boolean countDocs) {
		this.countDocs = countDocs;
	}

	public Iterator<Node[]> getInput() {
		return input;
	}

	public Callback getOutput() {
		return output;
	}

	public int getMaxInMemPartition() {
		return maxInMemPartition;
	}

	public void setMaxInMemPartition(int maxInMemPartition) {
		this.maxInMemPartition = maxInMemPartition;
	}
}
