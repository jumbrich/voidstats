package org.deri.voidstats.vvoid;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.deri.voidstats.anl.Analyser;
import org.deri.voidstats.anl.CountAnalyser;
import org.deri.voidstats.anl.CountRawAnalyser;
import org.deri.voidstats.anl.ListAnalyser;
import org.deri.voidstats.anl.MultiAnalyser;
import org.deri.voidstats.anl.TransformAnalyser;
import org.deri.voidstats.trans.NodeArrayFilterTransformer;
import org.deri.voidstats.trans.NodeArrayNamespaceTransformer;
import org.deri.voidstats.trans.NodeArrayProjectClassTransformer;
import org.deri.voidstats.trans.NodeArrayReorderTransformer;
import org.deri.voidstats.trans.PipedNodeArrayTransformer;
import org.deri.voidstats.trans.filter.NodeArrayFilterArray;
import org.deri.voidstats.trans.filter.NodeArraySameAsPrevFilter;
import org.deri.voidstats.trans.filter.NodeFilter.NegateNodeFilter;
import org.deri.voidstats.trans.filter.NodeFilter.NodeContainsFilter;
import org.deri.voidstats.vvoid.partition.PartitionAnalyser;
import org.deri.voidstats.vvoid.partition.PartitionAnalyser.Partition;
import org.deri.voidstats.vvoid.partition.PropertyPartitionAnalyser;
import org.deri.voidstats.vvoid.partition.SortedClassPartitionAnalyser;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.namespace.VOID;
import org.semanticweb.yars.nx.util.NxUtil;
import org.semanticweb.yars.util.TicksIterator;

public abstract class VoID {
	static Logger logger = Logger.getLogger(VoID.class.getName());
	
	public static final String CLASS_PARTITIONS_SUBDIR = "class-parts";
	public static final String PROPERTY_PARTITIONS_SUBDIR = "prop-parts";
	
	public static final String CLASS_PARTITION_ID_PREFIX = "class";
	public static final String PROPERTY_PARTITION_ID_PREFIX = "prop";

	protected VoIDOptions opts;

	//default
	protected CountRawAnalyser quads = new CountRawAnalyser();

	//partitions
	protected PartitionAnalyser propPart = null;
	protected PartitionAnalyser classPart = null;

	//default in-memory lists
	protected CountAnalyser l_subjs = null;
	protected CountAnalyser l_objs = null;
	protected CountAnalyser l_props = null;
	protected CountAnalyser l_types = null;
	protected CountAnalyser l_trips = null;
	protected ListAnalyser l_vocabs = null;
	protected CountAnalyser l_docs = null;
	protected CountAnalyser l_entities = null;

	protected List<Analyser> toRun = new ArrayList<Analyser>();
	
	protected String tmpDirClass = null;
	protected String tmpDirProp = null;

	public VoID(VoIDOptions opts) throws IOException{
		this.opts = opts;
		mkTmpDirs(opts.getTmpDir());
	}
	
	private static boolean mkTmpDirs(String dir) throws IOException{
		File tmpDir = new File(dir);
		if(tmpDir.exists() && !tmpDir.isDirectory()){
			throw new IOException("Cannot create directory '"+dir+"'. It's an existing file.");
		}
		boolean success = tmpDir.mkdirs();
		tmpDir.deleteOnExit();
		return success;
	}
	
	protected abstract void initialiseLists() throws IOException; 

	protected void initialiseAnalysers() throws IOException{
		toRun.add(quads);
		
		//distinct subjects
		if(l_subjs!=null){
			toRun.add(
					new TransformAnalyser(
							l_subjs,
							NodeArrayReorderTransformer.S
					)
			);
		}

		//distinct predicates
		if(l_props!=null){
			toRun.add(
					new TransformAnalyser(
							l_props,
							NodeArrayReorderTransformer.P
					)
			);
		}

		//distinct objects
		if(l_objs!=null){
			toRun.add(
					new TransformAnalyser(
							l_objs,
							NodeArrayReorderTransformer.O
					)
			);
		}

		//distinct types
		if(l_types!=null){
			toRun.add(
					new TransformAnalyser(
							l_types,
							new NodeArrayProjectClassTransformer()
					)
			);
		}

		//distinct triples (assumes S-P-O grouped input)
		if(l_trips!=null){
			toRun.add(
					new TransformAnalyser(
							l_trips,
							new PipedNodeArrayTransformer(
									NodeArrayReorderTransformer.SPO,
									new NodeArrayFilterTransformer(new NodeArraySameAsPrevFilter())
							)
					)
			);
		}

		//vocabs
		if(l_vocabs!=null){
			toRun.add(
					new TransformAnalyser(
							l_vocabs,
							new PipedNodeArrayTransformer(
									new NodeArrayProjectClassTransformer(),
									new NodeArrayNamespaceTransformer()
							),
							new PipedNodeArrayTransformer(
									NodeArrayReorderTransformer.P,
									new NodeArrayNamespaceTransformer()
							)
					)	
			);
		}

		//contexts
		if(opts.doCountDocs() && l_docs!=null){
			toRun.add(
					new TransformAnalyser(
							l_docs,
							NodeArrayReorderTransformer.C
					)
			);
		}


		//entities
		if(opts.getUriPattern()!=null && l_entities!=null){
			NodeArrayFilterTransformer checkPattern = new NodeArrayFilterTransformer(new NodeArrayFilterArray(new NegateNodeFilter(new NodeContainsFilter(opts.getUriPattern()))));
			toRun.add(
					new TransformAnalyser(
							l_entities,
							new PipedNodeArrayTransformer(
									NodeArrayReorderTransformer.S,
									checkPattern
							),
							new PipedNodeArrayTransformer(
									NodeArrayReorderTransformer.O,
									checkPattern
							)
					)
			);
		}
		
		//class partitions
		if(opts.doPartitionClasses()){
			tmpDirClass = opts.getTmpDir()+"/"+CLASS_PARTITIONS_SUBDIR;
			mkTmpDirs(tmpDirClass);
			classPart = new SortedClassPartitionAnalyser(tmpDirClass);
			classPart.setTicks(opts.getTicks());
			toRun.add(classPart);
		}

		//property partitions
		if(opts.doPartitionProperties()){
			tmpDirProp = opts.getTmpDir()+"/"+PROPERTY_PARTITIONS_SUBDIR;
			mkTmpDirs(tmpDirProp);
			propPart = new PropertyPartitionAnalyser(tmpDirProp);
			propPart.setTicks(opts.getTicks());
			toRun.add(propPart);
		}

	}

	public void run() throws Exception{
		initialiseLists();
		initialiseAnalysers();
		
		Resource dataset = new Resource(NxUtil.escapeForNx(opts.getDataset()));
		
		MultiAnalyser ma = new MultiAnalyser(toRun);

		logger.info("Analysing main data ["+dataset+"] ...");
		Iterator<Node[]> in = opts.getInput();
		in = new TicksIterator(in, opts.getTicks());
		while(in.hasNext()){
			ma.analyse(in.next());
		}
		logger.info("... done.  ["+dataset+"] ...");
		
		//clear references as we go
		ma = null;
		toRun = null;
		
		if(classPart!=null){
			classPart.closeWrite();
		}
		
		if(propPart!=null){
			propPart.closeWrite();
		}

		
		VoIDCallback vdcb = new VoIDCallback(opts.getOutput(),dataset);

		if(l_subjs!=null){
			logger.info("... counting subjects ["+dataset+"] ...");
			vdcb.writeDistinctSubjectCount(l_subjs.count());
			l_subjs = null;
		}
		if(l_objs!=null){
			logger.info("... counting objects ["+dataset+"] ...");
			vdcb.writeDistinctObjectCount(l_objs.count());
			l_objs = null;
		}
		if(l_types!=null){
			logger.info("... counting classes ["+dataset+"] ...");
			vdcb.writeClassCount(l_types.count());
			l_types = null;
		}
		if(l_props!=null){
			logger.info("... counting properties ["+dataset+"] ...");
			vdcb.writePropertyCount(l_props.count());
			l_props = null;
		}
		if(l_trips!=null){
			logger.info("... counting triples ["+dataset+"] ...");
			vdcb.writeTripleCount(l_trips.count());
			l_trips = null;
		}
		if(l_vocabs!=null){
			logger.info("... listing vocabs ["+dataset+"] ...");
			vdcb.writeVocabularies(l_vocabs.list());
			l_vocabs = null;
		}
		if(l_docs!=null){
			logger.info("... counting documents ["+dataset+"] ...");
			vdcb.writeDocumentCount(l_docs.count());
			l_docs = null;
		}
		if(l_entities!=null){
			logger.info("... counting entities ["+dataset+"] ...");
			vdcb.writeEntityCount(l_entities.count());
			l_entities = null;
		}

		boolean hash = dataset.toN3().contains("#");
		
		if(classPart!=null && opts.doPartitionClasses()){
			Map<Node, Partition> partitions = classPart.getPartitions();
			logger.info("... analysing "+partitions.size()+" class partitions ["+dataset+"] ...");
			int partCount = 0;
			for(Map.Entry<Node, Partition> n2p : partitions.entrySet()) {
				Resource partitionDataset = createPartitionURI(dataset,CLASS_PARTITION_ID_PREFIX,hash,partCount++);
				vdcb.writeClassPartitionLink(partitionDataset);
				opts.getOutput().processStatement(new Node[]{partitionDataset, VOID.CLASS, n2p.getKey()});
				
				logger.info("... analysing "+partitions.size()+" class partition ["+dataset+"##"+partitionDataset+"] ("+partCount+" of "+partitions.size()+") ...");
				handlePartition(n2p.getValue(), partitionDataset, tmpDirClass);
			}
			classPart=null;
		}
		if(propPart!=null && opts.doPartitionProperties()){
			Map<Node, Partition> partitions = propPart.getPartitions();
			logger.info("... analysing "+partitions.size()+" property partitions ["+dataset+"] ...");
			int partCount = 0;
			for(Map.Entry<Node, Partition> n2p : partitions.entrySet()) {
				Resource partitionDataset = createPartitionURI(dataset,PROPERTY_PARTITION_ID_PREFIX,hash,partCount++);
				vdcb.writePropertyPartitionLink(partitionDataset);
				opts.getOutput().processStatement(new Node[]{partitionDataset, VOID.PROPERTY, n2p.getKey()});
				
				logger.info("... analysing "+partitions.size()+" property partition ["+dataset+"##"+partitionDataset+"] ("+partCount+" of "+partitions.size()+") ...");
				handlePartition(n2p.getValue(), partitionDataset, tmpDirProp);
			}
			propPart=null;
		}
		
		logger.info("... finished analysing ["+dataset+"].");
	}
	
	private static Resource createPartitionURI(Resource dataset, String prefix, boolean hash, int index){
		String partId = prefix+index;
		if(hash){
			return new Resource(NxUtil.escapeForNx(dataset.toString()+"_"+partId));
		} else{
			return new Resource(NxUtil.escapeForNx(dataset.toString()+"#"+partId));
		}
	}
	
	protected VoIDOptions createVoIDOpts(Partition p, Resource dataset, String dir) throws IOException{
		VoIDOptions vo = new VoIDOptions(p.openIterator(), opts.getOutput(), dataset.toString());
		vo.setPartitionClasses(false);
		vo.setPartitionProperties(false);
		vo.setCountDocs(opts.doCountDocs());
		vo.setTicks(opts.getTicks());
		vo.setTmpDir(dir);
		vo.setUriPattern(opts.getUriPattern());
		return vo;
	}
	
	protected abstract void handlePartition(Partition p, Resource dataset, String dir) throws Exception;
}
