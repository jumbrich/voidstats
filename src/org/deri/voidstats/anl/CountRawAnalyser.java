package org.deri.voidstats.anl;

import org.semanticweb.yars.nx.Node;

public class CountRawAnalyser implements CountAnalyser {
	private long count = 0;
	
	public void analyse(Node... in) throws AnalyseException {
		if(in!=null)
			count ++;	
	}

	public void stats() {
		;
	}

	public long count() throws AnalyseException {
		return count;
	}

}
