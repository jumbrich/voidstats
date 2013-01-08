package org.deri.voidstats.anl.disk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.deri.voidstats.Main;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.mem.MemoryManager;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars.nx.sort.SortIterator;
import org.semanticweb.yars.util.CallbackNxBufferedWriter;
import org.semanticweb.yars.util.LRUMapCache;
import org.semanticweb.yars.util.NodesToNodeArrayIterator;

public class OnDiskListUnique {
	static int DEFAULT_CACHE_SIZE = (int)(MemoryManager.estimateMaxStatements(20) * Main.IN_MEM_LOAD_FACTOR);
	static String DEFAULT_FILENAME = "count.nq.gz";
	
	CallbackNxBufferedWriter _cb = null;
	BufferedWriter _bw = null;
//	File _filename;
	
	String _filename;
	int cacheSize = DEFAULT_CACHE_SIZE;
	
	LRUMapCache<Nodes,Nodes> _lrum;
	
	private static final Logger logger = Logger.getLogger(OnDiskListUnique.class.getName());
	
	static {
		logger.info("Using "+DEFAULT_CACHE_SIZE+" default cache size for "+OnDiskListUnique.class.getName());
	}
	
	public OnDiskListUnique(String filename) throws IOException{
		this(filename,DEFAULT_CACHE_SIZE);
	}
	
	public OnDiskListUnique(String filename, int cacheSize) throws IOException{
		_filename = filename; 
		_lrum = new LRUMapCache<Nodes,Nodes>(cacheSize);
		this.cacheSize = cacheSize;
	}
	
	private void openFile() throws IOException{
		File f = new File(_filename);
		f.deleteOnExit();
		OutputStream os = new GZIPOutputStream(new FileOutputStream(f));
		_bw = new BufferedWriter(new OutputStreamWriter(os));
		_cb = new CallbackNxBufferedWriter(_bw);
	}
	
	private void buffer(){
		for(Nodes ns:_lrum.keySet())
			_cb.processStatement(ns.getNodes());
	}
	
	public boolean add(Node... na) throws IOException{
		Nodes ns = new Nodes(na);
		boolean cached = _lrum.put(ns, ns)!=null;
		if(!cached && _lrum.size() >= cacheSize-1){
			if(_cb==null){
				openFile();
				buffer();
			}
			_cb.processStatement(na);
		}
		return !cached;
	}
	
	public Iterator<Node[]> closeAndList() throws IOException, ParseException{
		if(_bw==null){
//			logger.info("Uniquing in memory ...");
			return new NodesToNodeArrayIterator(_lrum.keySet().iterator());
		} else{
			_bw.close();
			
			logger.info("Opening "+_filename+" and sorting for uniqueness ...");
			InputStream is = new FileInputStream(_filename);
			is = new GZIPInputStream(is);
			NxParser nxp = new NxParser(is);
			
			SortIterator si = new SortIterator(nxp);
			logger.info("... sorted.");
			return si;
		}
	}
}

