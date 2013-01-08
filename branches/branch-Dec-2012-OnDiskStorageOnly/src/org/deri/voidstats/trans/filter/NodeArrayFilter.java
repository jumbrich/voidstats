package org.deri.voidstats.trans.filter;

import org.semanticweb.yars.nx.Node;

public interface NodeArrayFilter {
	public static NodeArrayFilterArray ALLOW_TYPE = new NodeArrayFilterArray(NodeFilter.ALLOW, NodeFilter.ALLOW_TYPE);
	public static NodeArrayFilterArray FILTER_TYPE = new NodeArrayFilterArray(NodeFilter.ALLOW, NodeFilter.FILTER_TYPE);
	public static NodeArraySameAsPrevFilter FILTER_SAME_AS_PREV = new NodeArraySameAsPrevFilter();
	
	
	public boolean filter(Node... na);
	
	public class NegateNodeArrayFilter implements NodeArrayFilter{
		NodeArrayFilter naf;
		public NegateNodeArrayFilter(NodeArrayFilter naf){
			this.naf = naf;
		}

		public boolean filter(Node... na) {
			return !naf.filter(na);
		}
	}
}
