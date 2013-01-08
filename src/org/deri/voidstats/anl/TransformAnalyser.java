package org.deri.voidstats.anl;

import org.deri.voidstats.trans.NodeArrayTransformer;
import org.semanticweb.yars.nx.Node;


public class TransformAnalyser implements Analyser {
	Analyser a;
	NodeArrayTransformer[] nnta;
	
	public TransformAnalyser(Analyser a, NodeArrayTransformer... nnta){
		this.a = a;
		this.nnta = nnta;
	}
	
	public void analyse(Node... in) throws AnalyseException {
		for(NodeArrayTransformer nnt:nnta){
			Node[] na = nnt.transform(in);
			if(na!=null && na.length!=0)
				a.analyse(na);
		}
	}

	public void stats() {
		a.stats();
	}

}
