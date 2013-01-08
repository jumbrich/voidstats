package org.deri.voidstats.anl;

import java.util.Collection;

import org.semanticweb.yars.nx.Node;

public class MultiAnalyser implements Analyser {
	Analyser[] as;

	public MultiAnalyser(Analyser... aa){
		this.as = aa;
	}
	
	public MultiAnalyser(Collection<? extends Analyser> ac){
		this.as = new Analyser[ac.size()];
		ac.toArray(as);
	}
	
	public void analyse(Node... in) throws AnalyseException {
		for(Analyser a:as){
			a.analyse(in);
		}
	}

	public void stats() {
		for(Analyser a:as){
			a.stats();
		}
	}

}
