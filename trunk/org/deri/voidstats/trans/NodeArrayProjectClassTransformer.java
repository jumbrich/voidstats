package org.deri.voidstats.trans;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.namespace.RDF;

public class NodeArrayProjectClassTransformer implements NodeArrayTransformer {
	public boolean uriOnly;
	
	public NodeArrayProjectClassTransformer(){
		this(true);
	}
	
	public NodeArrayProjectClassTransformer(boolean uriOnly){
		this.uriOnly = uriOnly;
	}
	
	public Node[] transform(Node... n) {
		if(n[1].equals(RDF.TYPE) && (!uriOnly || n[2] instanceof Resource)){
			return new Node[]{n[2]};
		}
		return null;
	}
}
