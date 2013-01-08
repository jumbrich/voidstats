package org.deri.voidstats.anl;

public interface CountAnalyser extends Analyser{
	public long count() throws AnalyseException;
}
