package org.semanticweb.yars.voidstats;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.filter.NodeFilter;
import org.semanticweb.yars.nx.parser.ParseException;

public class DistributionAnalyser<E> extends DefaultAnalyser{
	protected Count<E> _n;
	protected NodeTransformer<E> _np;
	private OnDiskCount _diskCounter;
	private boolean _usesDiskSpace;
	
	public DistributionAnalyser(Iterator<Node[]> in){
		super(in);
	}
	
	public DistributionAnalyser(Analyser in, NodeTransformer<E> np, String filename) throws IOException{
		this(in, null, null, np, true, filename, false);
	}
	
	public DistributionAnalyser(Analyser in, NodeFilter[] key, NodeTransformer<E> np, String filename) throws IOException{
		this(in, key, null, np, true, filename, false);
	}
	
	public DistributionAnalyser(Analyser in, int[] element, NodeTransformer<E> np, String filename) throws IOException{
		this(in, null, element, np, true, filename, false);
	}
	
	public DistributionAnalyser(Analyser in, NodeFilter[] key, int[] element, NodeTransformer<E> np, boolean tn, String filename, boolean usesDiskSpace) throws IOException{
		super(in, key, element, tn);
		_n = new Count<E>();
		_np = np;
		_diskCounter = new OnDiskCount(filename);
		_usesDiskSpace = usesDiskSpace;
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
		if (!_usesDiskSpace){
			for(E e:_np.processNode(in)){
				_n.add(e);
			}
		}else{
			addToDisk(in);
		}
	}
	
	private void addToDisk(Node[] in){
		_np.processNode(in);
		for (int i=0; i<in.length; i++){
			_diskCounter.add(in[i]);
		}		
	}
	
	public int closeAndCount() throws IOException, ParseException{
		return _diskCounter.closeAndCount();
	}
}
