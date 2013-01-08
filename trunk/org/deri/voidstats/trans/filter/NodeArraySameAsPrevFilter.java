package org.deri.voidstats.trans.filter;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;

public class NodeArraySameAsPrevFilter implements NodeArrayFilter {
	private Node[] prev = null;

	public boolean filter(Node... na) {
		if(prev==null || !NodeComparator.NC.equals(prev,na)){
			prev = na;
			return false;
		}
		return true;
	};
	
}
