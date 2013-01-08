package org.deri.voidstats.anl;

import java.util.Iterator;

import org.semanticweb.yars.nx.Node;

public interface ListAnalyser extends Analyser{
	public Iterator<Node[]> list() throws AnalyseException;
}
