package org.deri.voidstats.anl.mem;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.deri.voidstats.anl.CountAnalyser;
import org.deri.voidstats.anl.ListAnalyser;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;

public class InMemoryListUniqueAnalyser implements CountAnalyser, ListAnalyser{
	Set<Nodes> set;
	
	public InMemoryListUniqueAnalyser(){
		this(new HashSet<Nodes>());
	}
	
	public InMemoryListUniqueAnalyser(Set<Nodes> set){
		this.set = set;
	}
	
	public void analyse(Node... in) throws AnalyseException {
		set.add(new Nodes(in));
	}

	public void stats() {
		;
	}

	public long count() throws AnalyseException {
		return set.size();
	}

	public Iterator<Node[]> list() throws AnalyseException {
		return new NodesToNodeArrayIterator(set.iterator());
	}
	
	public static class NodesToNodeArrayIterator implements Iterator<Node[]>{
		Iterator<Nodes> in;
		
		public NodesToNodeArrayIterator(Iterator<Nodes> in){
			this.in = in;
		}

		public boolean hasNext() {
			return in.hasNext();
		}

		public Node[] next() {
			return in.next().getNodes();
		}

		public void remove() {
			in.remove();
		}
	}
}