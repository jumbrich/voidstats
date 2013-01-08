package org.deri.voidstats.trans;

import org.deri.voidstats.trans.filter.NodeArrayFilter;
import org.semanticweb.yars.nx.Node;


public class NodeArrayFilterTransformer implements NodeArrayTransformer{
	public NodeArrayFilter naf;
	
	public NodeArrayFilterTransformer(NodeArrayFilter naf){
		this.naf = naf;
	}

	public Node[] transform(Node... na) {
		if(naf.filter(na))
			return null;
		return na;
	}
}
