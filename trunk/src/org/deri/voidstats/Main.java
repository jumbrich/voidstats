package org.deri.voidstats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.deri.voidstats.vvoid.VoID;
import org.deri.voidstats.vvoid.VoIDOptions;
import org.deri.voidstats.vvoid.disk.OnDiskVoID;
import org.deri.voidstats.vvoid.mem.SortedInMemVoID;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.sort.SortIterator;
import org.semanticweb.yars.util.CallbackNxBufferedWriter;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class.getName());
	
	//decrease for OutOfMemoryError 
	public static double IN_MEM_LOAD_FACTOR = 1;

	public static void main(String[] args) throws Exception{
		// create the command line parser
		CommandLineParser parser = new PosixParser();

		// create the Options
		Options options = new Options();
		options.addOption( "i", true, "n-triple input file  (sorted by s-p-o; context optional)" );
		options.addOption( "s", true, "pre-sort input file (if not sorted by s-p-o) and output to file name given." );
		options.addOption( "o", true, "n-triple output file" );
		options.addOption( "d", true, "dataset name to use for analysis" );
		options.addOption( "u", true, "URI pattern to include in analysis" );
		options.addOption( "h", false, "print this help message" );
		options.addOption( "igz", false, "if input is g-zipped" );
		options.addOption( "ogz", false, "if output should be g-zipped" );
		options.addOption( "pp", false, "run property partitions" );
		options.addOption( "cp", false, "run class partitions" );
		options.addOption( "doc", false, "count docs (requires quads)" );
		options.addOption( "im", false, "use in-memory storage, on-disk storage is used as default" );


		File inputFile = null;
		File outputFile = null;
		String dataset = null;
		String uriPattern = null;
		boolean inputGz = false;
		boolean outputGz = false;
		boolean propPart = false;
		boolean clasPart = false;
		boolean docs = false;
		
		if (args.length < 1) {
			showHelpAndExit(options);
		}

		// parse the command line arguments
		CommandLine line = parser.parse( options, args );

		if( line.hasOption( "i" ) ) {
			if( line.hasOption( "s" ) ) {
				File rawFile = new File(line.getOptionValue( "i" ));
				InputStream is = new FileInputStream(rawFile);
				if( line.hasOption( "igz" ) ) {
					inputGz = true;
					is = new GZIPInputStream(is);
				}
				NxParser nxp = new NxParser(is);
				SortIterator si = new SortIterator(nxp);
				
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(line.getOptionValue("s")))));
				CallbackNxBufferedWriter cnbw = new CallbackNxBufferedWriter(bw);
				
				while(si.hasNext()){
					cnbw.processStatement(nxp.next());
				}
				bw.close();
				is.close();
				
				inputGz = true;
				inputFile = new File(line.getOptionValue( "s" ));
			}else{
				inputFile = new File(line.getOptionValue( "i" ));
			}
		} else{
			showHelpAndExit(options);
		}
		
		
		
		if( line.hasOption( "o" ) ) {
			outputFile = new File(line.getOptionValue( "o" ));
		}else{
			showHelpAndExit(options);
		}
		
		if( line.hasOption( "d" ) ) {
			dataset = line.getOptionValue( "d" );
		}else{
			showHelpAndExit(options);
		}
		
		if( line.hasOption( "u" ) ) {
			uriPattern = line.getOptionValue( "u" );
		}
		
		if( line.hasOption( "h" ) ) {
			showHelpAndExit(options);
		}
		
		if( line.hasOption( "igz" ) ) {
			inputGz = true;
		}
		
		if( line.hasOption( "ogz" ) ) {
			outputGz = true;
		}
		
		if( line.hasOption("pp") ){
			propPart = true;
		}
		
		if( line.hasOption("cp") ){
			clasPart = true;
		}
		
		if(line.hasOption("doc")){
			docs = true;
		}
		
		OutputStream os = new FileOutputStream(outputFile);
		if(outputGz){
			os = new GZIPOutputStream(os);
		}
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
		Callback cb = new CallbackNxBufferedWriter(bw);

		InputStream is = new FileInputStream(inputFile);
		if(inputGz){
			is = new GZIPInputStream(is);
		}
		Iterator<Node[]> in = new NxParser(is);

		VoIDOptions opts = new VoIDOptions(in,cb,dataset);
		opts.setUriPattern(uriPattern);
		opts.setPartitionClasses(clasPart);
		opts.setPartitionProperties(propPart);
		opts.setTmpDir(org.semanticweb.yars.nx.cli.Main.getTempSubDir());
		opts.setCountDocs(docs);
		
		long startTime = System.currentTimeMillis();
		if ( line.hasOption( "im" ) ){
			VoID v = new SortedInMemVoID(opts);
			v.run();
		}else{
			VoID v = new OnDiskVoID(opts);
			v.run();
		}
		
		long endTime = System.currentTimeMillis();
		
		logger.info("Finished in "+(endTime-startTime)+" ms");

		bw.close();
		is.close();
	}

	public static void showHelpAndExit(Options options){
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "voidstats", options );
		System.exit(-1);
	}
}














