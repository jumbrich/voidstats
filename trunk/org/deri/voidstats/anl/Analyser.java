package org.deri.voidstats.anl;

import org.semanticweb.yars.nx.Node;

public interface Analyser {
	public void analyse(Node... in) throws AnalyseException;
	public void stats();
	
	public class AnalyseException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public AnalyseException(){
			;
		}
		
		public AnalyseException(Exception e){
			super(e);
		}
		
		public AnalyseException(String msg){
			super(msg);
		}
	}
}
