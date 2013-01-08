package org.deri.voidstats.vvoid.partition;

import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;

public class PropertyPartitionAnalyser extends PartitionAnalyser {
	static Logger logger = Logger.getLogger(PropertyPartitionAnalyser.class.getName());
	
	public PropertyPartitionAnalyser(String directory) {
		super(directory);
	}
	
	public void stats() {
		;
	}

	public void analyse(Node... na) throws AnalyseException {
		try{
			Partition p = getOrCreatePartition(na[1]);
			p.processStatement(na);
			done++;
			if(ticks>0 && done%ticks==0){
				logger.info("... property partitions ... written "+done+".");
			}
		} catch(Exception e){
			throw new AnalyseException(e);
		}
	}
}
