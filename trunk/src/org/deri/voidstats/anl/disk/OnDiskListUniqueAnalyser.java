package org.deri.voidstats.anl.disk;

import java.io.IOException;
import java.util.Iterator;

import org.deri.voidstats.anl.CountAnalyser;
import org.deri.voidstats.anl.ListAnalyser;
import org.semanticweb.yars.nx.Node;

public class OnDiskListUniqueAnalyser implements CountAnalyser, ListAnalyser{

	OnDiskListUnique odcu;
	
	public OnDiskListUniqueAnalyser(String filename) throws IOException{
		odcu = new OnDiskListUnique(filename);
	}
	
	public void analyse(Node... in) throws AnalyseException {
		try{
			odcu.add(in);
		} catch(IOException e){
			throw new AnalyseException(e);
		}
	}
	
	/**
	 * Once off call!
	 * @return iterator over unique list
	 * @throws AnalyseException
	 */
	public Iterator<Node[]> list() throws AnalyseException{
		try{
			return odcu.closeAndList();
		} catch(Exception e){
			throw new AnalyseException(e);
		}
	}

	public void stats() {
		;
	}

	/**
	 * Once off call!
	 * @return unique count
	 * @throws AnalyseException
	 */
	public long count() throws AnalyseException {
		Iterator<Node[]> unique = list();
		
		long count = 0;
		while(unique.hasNext()){
			unique.next();
			count++;
		}
		return count;
	}
}
