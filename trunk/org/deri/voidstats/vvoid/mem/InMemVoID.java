package org.deri.voidstats.vvoid.mem;

import java.io.IOException;

import org.deri.voidstats.anl.mem.InMemoryListUniqueAnalyser;
import org.deri.voidstats.vvoid.VoID;
import org.deri.voidstats.vvoid.VoIDOptions;
import org.deri.voidstats.vvoid.partition.PartitionAnalyser.Partition;
import org.semanticweb.yars.nx.Resource;

public class InMemVoID extends VoID {
	
	public InMemVoID(VoIDOptions opts) throws IOException{
		super(opts);
	}
	
	protected void initialiseLists(){
		l_subjs = new InMemoryListUniqueAnalyser();
		l_objs = new InMemoryListUniqueAnalyser();
		l_props = new InMemoryListUniqueAnalyser();
		l_types = new InMemoryListUniqueAnalyser();
		l_trips = new InMemoryListUniqueAnalyser();
		l_vocabs = new InMemoryListUniqueAnalyser();
		if(opts.doCountDocs())
			l_docs = new InMemoryListUniqueAnalyser();
		if(opts.getUriPattern()!=null)
			l_entities = new InMemoryListUniqueAnalyser();
	}

	protected void handlePartition(Partition p, Resource dataset, String dir) throws Exception {
		VoIDOptions vo = createVoIDOpts(p, dataset, dir);
		InMemVoID imvoid = new InMemVoID(vo);
		imvoid.run();
	}

}
