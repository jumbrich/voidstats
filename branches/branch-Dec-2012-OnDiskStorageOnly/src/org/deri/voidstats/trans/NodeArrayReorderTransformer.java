package org.deri.voidstats.trans;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.reorder.ReorderIterator;

public class NodeArrayReorderTransformer implements NodeArrayTransformer {
	public static final NodeArrayReorderTransformer S = new NodeArrayReorderTransformer(0);
	public static final NodeArrayReorderTransformer P = new NodeArrayReorderTransformer(1);
	public static final NodeArrayReorderTransformer O = new NodeArrayReorderTransformer(2);
	public static final NodeArrayReorderTransformer C = new NodeArrayReorderTransformer(3);
	
	public static final NodeArrayReorderTransformer SPO = new NodeArrayReorderTransformer(0,1,2);
	
	private int[] reorder;
	
	
	
	public NodeArrayReorderTransformer(int... reorder){
		this.reorder = reorder;
	}

	public Node[] transform(Node... n) {
		return ReorderIterator.reorder(n, reorder);
	}
}
