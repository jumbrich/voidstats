package org.semanticweb.yars.voidstats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars.nx.sort.SortIterator;
import org.semanticweb.yars.util.CallbackNxBufferedWriter;
import org.semanticweb.yars.util.LRUMapCache;

public class OnDiskCount {
	static int DEFAULT_CACHE_SIZE = 1000;
	static String DEFAULT_FILENAME = "count.nq.gz";
	
	CallbackNxBufferedWriter _cb;
	BufferedWriter _bw;
	File _filename;
	
	LRUMapCache<Nodes,Nodes> _lrum;
	
	private static final Logger logger = Logger.getLogger(OnDiskCount.class.getName());
	private static long fileCount = 0;
	
	public OnDiskCount() throws IOException{
		//this(Main.getTempSubDir()+"/"+DEFAULT_FILENAME);
	}
	
	public OnDiskCount(String filename) throws IOException{
		_filename = File.createTempFile(filename, ".tmp");
		_filename.deleteOnExit();
		OutputStream os = new GZIPOutputStream(new FileOutputStream(_filename));
		_bw = new BufferedWriter(new OutputStreamWriter(os));
		_cb = new CallbackNxBufferedWriter(_bw);
		_lrum = new LRUMapCache<Nodes,Nodes>(DEFAULT_CACHE_SIZE);
//		fileCount++;
//		logger.info("Created OnDiskCount file: "+fileCount);		
	}
	
	public boolean add(Node... na){
		Nodes ns = new Nodes(na);
		if(_lrum.put(ns, ns)==null){
			_cb.processStatement(na);
			return true;
		}
		return false;
	}
	
	public int closeAndCount() throws IOException, ParseException{
		_bw.close();
		
		InputStream is = new FileInputStream(_filename);
		is = new GZIPInputStream(is);
		NxParser nxp = new NxParser(is);
		
		SortIterator si = new SortIterator(nxp);
		
		int count = 0;
		while(si.hasNext()){
			//System.err.println(Nodes.toN3(si.next()));
			si.next();
			count++;
		}
		
		is.close();
		return count;
	}
	
	public static void main(String[] args) throws IOException, ParseException{
		OnDiskCount odc = new OnDiskCount("test.nq.gz");
		odc.add(new Resource("http://1.com"));
		odc.add(new Resource("http://2.com"));
		odc.add(new Node[] { new Resource("http://1.com"), new BNode("2")});
		odc.add(new Resource("http://2.com"));
		odc.add(new Resource("http://1.com"));
		odc.add(new Resource("http://3.com"));
		odc.add(new Resource("http://4.com"));
		odc.add(new Node[] { new Resource("http://1.com"), new BNode("2")});
		
		System.err.println(odc.closeAndCount());
	}
}

