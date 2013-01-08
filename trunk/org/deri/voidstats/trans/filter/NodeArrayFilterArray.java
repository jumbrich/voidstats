package org.deri.voidstats.trans.filter;

import org.semanticweb.yars.nx.Node;

public class NodeArrayFilterArray implements NodeArrayFilter {
	private NodeFilter[] nfa;
	
	/**
	 * Apply first filter to first node, etc.
	 * Filter if one node is filtered.
	 * @param nfa
	 */
	public NodeArrayFilterArray(NodeFilter... nfa){
		this.nfa = nfa;
	}
	
	public boolean filter(Node... na){
		for(int i=0; i<na.length && i<nfa.length; i++){
			if(nfa[i].filter(na[i])){
				return true;
			}
		}
		return false;
	}
}
