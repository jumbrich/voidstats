package org.deri.voidstats.trans;

import org.semanticweb.yars.nx.Node;

public class PipedNodeArrayTransformer implements NodeArrayTransformer {
	private NodeArrayTransformer[] nnta;
	
	public PipedNodeArrayTransformer(NodeArrayTransformer... nnta){
		this.nnta = nnta;
	}

	public Node[] transform(Node... n) {
		Node[] in = n;
		for(NodeArrayTransformer nnt:nnta){
			in = nnt.transform(in);
			if(in==null)
				return null;
		}
		return in;
	}
}
