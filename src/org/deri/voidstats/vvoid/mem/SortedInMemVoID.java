package org.deri.voidstats.vvoid.mem;

import java.io.IOException;
import java.util.logging.Logger;

import org.deri.voidstats.Main;
import org.deri.voidstats.anl.CountRawAnalyser;
import org.deri.voidstats.anl.TransformAnalyser;
import org.deri.voidstats.trans.NodeArrayFilterTransformer;
import org.deri.voidstats.trans.NodeArrayReorderTransformer;
import org.deri.voidstats.trans.PipedNodeArrayTransformer;
import org.deri.voidstats.trans.filter.NodeArrayFilter;
import org.deri.voidstats.vvoid.VoIDOptions;
import org.deri.voidstats.vvoid.partition.PartitionAnalyser.Partition;
import org.semanticweb.yars.nx.Resource;

public class SortedInMemVoID extends InMemVoID{
	
	private static final Logger logger = Logger.getLogger(SortedInMemVoID.class.getName());
	
	public SortedInMemVoID(VoIDOptions opts) throws IOException{
		super(opts);
		logger.info("Creating SortedInMemVoid object...");
	}
	
	protected void initialiseLists(){
		//instead of listing triples, count input sorted tuples
		super.initialiseLists();
		l_trips = null;
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
		SortedInMemVoID simvoid = new SortedInMemVoID(vo);
		simvoid.run();
	}
}
