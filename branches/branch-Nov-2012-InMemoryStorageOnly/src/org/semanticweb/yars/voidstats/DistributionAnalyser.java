package org.semanticweb.yars.voidstats;

import java.util.Iterator;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.filter.NodeFilter;

public class DistributionAnalyser<E> extends DefaultAnalyser{
	protected Count<E> _n;
	protected NodeTransformer<E> _np;
	
	public DistributionAnalyser(Iterator<Node[]> in){
		super(in);
	}
	
	public DistributionAnalyser(Analyser in, NodeTransformer<E> np ){
		this(in, null, null, np, true);
	}
	
	public DistributionAnalyser(Analyser in, NodeFilter[] key, NodeTransformer<E> np){
		this(in, key, null, np, true);
	}
	
	public DistributionAnalyser(Analyser in, int[] element, NodeTransformer<E> np){
		this(in, null, element, np, true);
	}
	
	public DistributionAnalyser(Analyser in, NodeFilter[] key, int[] element, NodeTransformer<E> np, boolean tn){
		super(in, key, element, tn);
		_n = new Count<E>();
		_np = np;

	}

	@Override
	public void stats() {
		;
	}

	public Map<E, Integer> getStatsMap(){
		return _n;
	}
	
	@Override
	public void analyse(Node[] in){
		for(E e:_np.processNode(in)){
			_n.add(e);
		}
	}
}
