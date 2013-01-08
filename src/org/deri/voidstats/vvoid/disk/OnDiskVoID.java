package org.deri.voidstats.vvoid.disk;

import java.io.IOException;

import org.deri.voidstats.anl.CountRawAnalyser;
import org.deri.voidstats.anl.TransformAnalyser;
import org.deri.voidstats.anl.disk.OnDiskListUniqueAnalyser;
import org.deri.voidstats.trans.NodeArrayFilterTransformer;
import org.deri.voidstats.trans.NodeArrayReorderTransformer;
import org.deri.voidstats.trans.PipedNodeArrayTransformer;
import org.deri.voidstats.trans.filter.NodeArrayFilter;
import org.deri.voidstats.vvoid.VoID;
import org.deri.voidstats.vvoid.VoIDOptions;
import org.deri.voidstats.vvoid.mem.SortedInMemVoID;
import org.deri.voidstats.vvoid.partition.PartitionAnalyser.Partition;
import org.semanticweb.yars.nx.Resource;

public class OnDiskVoID extends VoID {
	public static final String BUFFER_SUFFIX = ".nx.gz";
	public static final String SUBJ_BUFFER = "subjs"+BUFFER_SUFFIX;
	public static final String OBJ_BUFFER = "objs"+BUFFER_SUFFIX;
	public static final String PROPS_BUFFER = "props"+BUFFER_SUFFIX;
	public static final String CLASSES_BUFFER = "classes"+BUFFER_SUFFIX;
	public static final String TRIPLES_BUFFER = "triples"+BUFFER_SUFFIX;
	public static final String VOCABS_BUFFER = "vocabs"+BUFFER_SUFFIX;
	public static final String DOCS_BUFFER = "docs"+BUFFER_SUFFIX;
	public static final String ENTITIES_BUFFER = "entities"+BUFFER_SUFFIX;
	
	public OnDiskVoID(VoIDOptions opts) throws IOException{
		super(opts);
	}
	
	protected void initialiseLists() throws IOException{
		String dir = opts.getTmpDir();
		l_subjs = new OnDiskListUniqueAnalyser(dir+"/"+SUBJ_BUFFER);
		l_objs = new OnDiskListUniqueAnalyser(dir+"/"+OBJ_BUFFER);
		l_props = new OnDiskListUniqueAnalyser(dir+"/"+PROPS_BUFFER);
		l_types = new OnDiskListUniqueAnalyser(dir+"/"+CLASSES_BUFFER);
//		l_trips = new OnDiskListUniqueAnalyser(dir+"/"+TRIPLES_BUFFER);
		l_vocabs = new OnDiskListUniqueAnalyser(dir+"/"+VOCABS_BUFFER);
		if(opts.doCountDocs())
			l_docs = new OnDiskListUniqueAnalyser(dir+"/"+DOCS_BUFFER);
		if(opts.getUriPattern()!=null)
			l_entities = new OnDiskListUniqueAnalyser(dir+"/"+ENTITIES_BUFFER);
	}
	
	protected void initialiseAnalysers() throws IOException{
		super.initialiseAnalysers();
		
		TransformAnalyser ta = new TransformAnalyser(
		          l_trips = new CountRawAnalyser(),
		          			new PipedNodeArrayTransformer(
		          					NodeArrayReorderTransformer.SPO,
		          					new NodeArrayFilterTransformer(
		          							NodeArrayFilter.FILTER_SAME_AS_PREV
		          					)
							)
					);
		
		toRun.add(ta);
	}

	protected void handlePartition(Partition p, Resource dataset, String dir) throws Exception {
		VoIDOptions vo = createVoIDOpts(p, dataset, dir);
		VoID voiD = null;
		if(p.count()>opts.getMaxInMemPartition()){
			voiD = new OnDiskVoID(vo);
		} else{
			voiD = new SortedInMemVoID(vo);
		}
		voiD.run();
	}
}
