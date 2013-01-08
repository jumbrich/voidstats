package org.deri.voidstats.trans.filter;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.namespace.RDF;

public interface NodeFilter{
	public static final NodeFilter ALLOW = new FalseNodeFilter();
	public static final NodeFilter FILTER = new NegateNodeFilter(new FalseNodeFilter());
	
	public static final NodeFilter FILTER_BNODE = new NodeTypeFilter(BNode.class);
	public static final NodeFilter FILTER_URI = new NodeTypeFilter(Resource.class);
	public static final NodeFilter FILTER_LITERAL = new NodeTypeFilter(Literal.class);
	
	public static final NodeFilter ALLOW_ONLY_BNODE = new NegateNodeFilter(FILTER_BNODE);
	public static final NodeFilter ALLOW_ONLY_URI = new NegateNodeFilter(FILTER_BNODE);
	public static final NodeFilter ALLOW_ONLY_LITERAL = new NegateNodeFilter(FILTER_URI);
	
	public static final NodeFilter FILTER_TYPE = new NodeEqualsFilter(RDF.TYPE);
	public static final NodeFilter ALLOW_TYPE = new NegateNodeFilter(FILTER_TYPE);
	
	public boolean filter(Node n);
	
	public static class FalseNodeFilter implements NodeFilter{
		
		public boolean filter(Node n){
			return false;
		}
	}

	public static class NegateNodeFilter implements NodeFilter{
		NodeFilter nf;
		
		public NegateNodeFilter(NodeFilter nf){
			this.nf = nf;
		}
		
		public boolean filter(Node n){
			return !nf.filter(n);
		}
	}

	public static class AndNodeFilter implements NodeFilter{
		NodeFilter[] nfs;
		
		public AndNodeFilter(NodeFilter... nfs){
			this.nfs = nfs;
		}
		
		public boolean filter(Node n){
			for(NodeFilter nf:nfs){
				if(!nf.filter(n)){
					return false;
				}
			}
			return true;
		}
	}

	public static class OrNodeFilter implements NodeFilter{
		NodeFilter[] nfs;
		
		public OrNodeFilter(NodeFilter... nfs){
			this.nfs = nfs;
		}
		
		public boolean filter(Node n){
			for(NodeFilter nf:nfs){
				if(nf.filter(n)){
					return true;
				}
			}
			return false;
		}
	}

	public static class NodeTypeFilter implements NodeFilter{
		Class<? extends Node> claz;
		
		public NodeTypeFilter(Class<? extends Node> claz){
			this.claz = claz;
		}
		
		public boolean filter(Node n){
			return claz.isInstance(n);
		}
	}
	
	public static class NodeEqualsFilter implements NodeFilter{
		Node n;
		
		public NodeEqualsFilter(Node n){
			this.n = n;
		}
		
		public boolean filter(Node n){
			return this.n.equals(n);
		}
	}

	public static class NodeContainsFilter implements NodeFilter{
		String s;
		
		/**
		 * @param s should be Nx escaped since check is on Node.toN3(.)
		 */
		public NodeContainsFilter(String s){
			this.s = s;
		}
		
		public boolean filter(Node n){
			return n.toN3().contains(s);
		}
	}
}


