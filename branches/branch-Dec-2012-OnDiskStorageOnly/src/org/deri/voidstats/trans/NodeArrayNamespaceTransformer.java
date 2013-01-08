package org.deri.voidstats.trans;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.util.NxUtil;

public class NodeArrayNamespaceTransformer implements NodeArrayTransformer {

	public Node[] transform(Node... na) {
		Node[] sch = new Node[na.length];
		for(int i=0; i<na.length; i++){
			if(na[i] instanceof Resource){
				String ns = getNamespace(na[i].toString());
				if(ns != null)
					sch[i] = new Resource(NxUtil.escapeForNx(ns));
				else
					sch[i] = na[i];
			} else{
				sch[i] = na[i];
			}
		}
		
		return sch;
	}
	
	public static String getNamespace(String url){
		int hash, slash;
		hash = url.lastIndexOf("#");
		slash = url.lastIndexOf("/");
		
		if(hash<0 && slash<0){
			return null;
		} else if(hash>slash){
			return url.substring(0,hash);
		} else{
			return url.substring(0,slash+1);
		}
	}
}
