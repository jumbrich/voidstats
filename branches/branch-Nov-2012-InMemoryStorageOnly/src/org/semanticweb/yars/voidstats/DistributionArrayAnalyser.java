package org.semanticweb.yars.voidstats;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.filter.NodeFilter;
import org.semanticweb.yars.util.Array;

public class DistributionArrayAnalyser<E extends Comparable<E>> extends DistributionAnalyser<E>{
	
	private Count<Array<E>> _n;
	
	public DistributionArrayAnalyser(Iterator<Node[]> in, String filename) throws IOException{
		super(in);
	}
	
	public DistributionArrayAnalyser(Analyser in, NodeTransformer<E> np, String filename) throws IOException{
		this(in, null, null, np, filename);
	}
	
	public DistributionArrayAnalyser(Analyser in, NodeFilter[] key, NodeTransformer<E> np, String filename) throws IOException{
		this(in, key, null, np, filename);
	}
	
	public DistributionArrayAnalyser(Analyser in, int[] element, NodeTransformer<E> np, String filename) throws IOException{
		this(in, null, element, np, filename);
	}
	
	public DistributionArrayAnalyser(Analyser in, NodeFilter[] key, int[] element, NodeTransformer<E> np, String filename) throws IOException{
		super(in, key, element, np, true, filename, false);
		_n = new Count<Array<E>>();
	}

	@Override
	public void stats() {
		;
	}

	public Map<Array<E>, Integer> getStats(){return _n;}
	
	@Override
	public void analyse(Node[] in){
		_n.add(new Array<E>(_np.processNode(in)));
	}
	
	public Map<Array<E>,Integer> getStatsArrayMap(){
		return _n;
	}
}
