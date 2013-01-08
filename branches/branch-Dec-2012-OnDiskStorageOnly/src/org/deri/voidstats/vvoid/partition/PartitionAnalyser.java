package org.deri.voidstats.vvoid.partition;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.deri.voidstats.anl.Analyser;
import org.deri.voidstats.vvoid.VoIDOptions;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.util.CallbackCount;
import org.semanticweb.yars.util.CallbackNxBufferedWriter;
import org.semanticweb.yars.util.PleaseCloseTheDoorWhenYouLeaveIterator;

public abstract class PartitionAnalyser implements Analyser{
	public static final int DEFAULT_TICKS = VoIDOptions.DEFAULT_TICKS;
	int ticks = DEFAULT_TICKS;
	int done = 0;
	
	static Logger log = Logger.getLogger(PartitionAnalyser.class.getName());
	Map<Node,Partition> partitions;
	String directory;
	
	public PartitionAnalyser(String directory){
		this.directory = directory;
		org.semanticweb.yars.nx.cli.Main.mkdirs(directory);
		partitions = new HashMap<Node,Partition>();
	}
	
	public Map<Node,Partition> getPartitions(){
		return partitions;
	}
	
	public void setTicks(int ticks){
		this.ticks = ticks;
	}
	
	protected Partition getOrCreatePartition(Node n) throws FileNotFoundException, IOException{
		Partition p = partitions.get(n);
		if(p==null){
			String filename = URLEncoder.encode(n.toN3(),"utf-8");
			log.info("Opening partition "+filename);
			filename = directory+"/"+filename;
			p = new Partition(filename);
			partitions.put(n,p);
		}
		return p;
	}
	
	public abstract void analyse(Node... na) throws AnalyseException;
	
	public void closeWrite() throws IOException{
		for(Partition p:partitions.values()){
			p.closeWrite();
		}
	}
	
	public static class Partition {
		private CallbackCount cb;
		private BufferedWriter bw;
		String filename;
		
		Iterator<Node[]> in;
		
		protected Partition(String filename) throws FileNotFoundException, IOException{
			this.filename = filename;
			openPartition(filename);
		}
		
		public String getFilename(){
			return filename;
		}
		
		protected void processStatement(Node... na){
			if(bw==null)
				throw new UnsupportedOperationException("Cannot process statements after call to closeWrite()");
			cb.processStatement(na);
		}
		
		protected void closeWrite() throws IOException{
			if(bw!=null){
				bw.close();
				bw = null;
			}
		}
		
		public int count(){
			return cb.getStmts();
		}
		
		public Iterator<Node[]> openIterator() throws IOException{
			//InputStream is = new GZIPInputStream(new FileInputStream(filename));
			InputStream is = new FileInputStream(filename);
			return new PleaseCloseTheDoorWhenYouLeaveIterator<Node[]>(new NxParser(is), is);
		}
		
		private void openPartition(String filename) throws FileNotFoundException, IOException{
			//OutputStream os = new GZIPOutputStream(new FileOutputStream(filename));
			OutputStream os = new FileOutputStream(filename);
			bw = new BufferedWriter(new OutputStreamWriter(os));
			cb = new CallbackCount(new CallbackNxBufferedWriter(bw));
		}
	}
}
