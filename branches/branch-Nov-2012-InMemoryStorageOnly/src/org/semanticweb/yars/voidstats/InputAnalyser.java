package org.semanticweb.yars.voidstats;

import java.util.Iterator;

import org.semanticweb.yars.nx.Node;

public class InputAnalyser implements Analyser {
	private final Iterator<Node[]> _in;
	
	public InputAnalyser(Iterator<Node[]> in){
		_in = in;
	}
	
	@Override
	public boolean hasNext() {
		return _in.hasNext();
	}

	@Override
	public Node[] next() {
		return _in.next();
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void analyse(Node[] in) {
		;
	}

	@Override
	public void stats() {
		;
	}
}
