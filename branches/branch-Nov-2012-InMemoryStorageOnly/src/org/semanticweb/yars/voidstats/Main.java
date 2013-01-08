package org.semanticweb.yars.voidstats;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Main {
	
	private static Options options;
	static File inputFile;
	static File outputFile;
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static boolean isGzipped = false;

      
	public static void main(String[] args) throws IOException, org.semanticweb.yars.nx.parser.ParseException{
		
		// create the command line parser
		CommandLineParser parser = new PosixParser();
		
		// create the Options
		options = new Options();
		options.addOption( "i", true, "n-triple input file" );
		options.addOption( "o", true, "n-triple output file" );
		options.addOption( "d", true, "dataset name to use for analysis" );
		options.addOption( "u", true, "URI pattern to include in analysis" );
		options.addOption( "h", false, "print this message" );
		options.addOption( "igz", false, "if input is zipped" );
		
		try {
			if (args.length < 1) {
				showHelp();
			}
			
		    // parse the command line arguments
		    CommandLine line = parser.parse( options, args );

		    if( line.hasOption( "i" ) ) {
		        inputFile = new File(line.getOptionValue( "i" ));
		    }else{
		    	showHelp();
		    }
		    if( line.hasOption( "o" ) ) {
		    	VoID.setCallBack(line.getOptionValue( "o" ));
		    }else{
		    	VoID.setCallBack("voidstats.output.nt");
		    }
		    if( line.hasOption( "d" ) ) {
		    	VoID.setDataSetName(line.getOptionValue( "d" ), false);
		    }else{
		    	showHelp();
		    }
		    if( line.hasOption( "u" ) ) {
		    	VoID.URIPattern = line.getOptionValue( "u" );
		    }else{
		    	VoID.URIPattern = "";
		    }		    
		    if( line.hasOption( "h" ) ) {
				showHelp();
		    }
		    if( line.hasOption( "igz" ) ) {
				isGzipped = true;
		    }
		}
		catch( ParseException exp ) {
		    System.out.println( "Unexpected exception:" + exp.getMessage() );
		}
		logger.info("Analysing for VoID statistics... ");
		
		VoID.cb.startDocument();
		
		long startTime = System.currentTimeMillis();
		VoID.runDefaultAnalysis(inputFile);
		long stop1 = System.currentTimeMillis();
		
//		ClassAnalyser.writeToFiles(inputFile);
		ClassAnalyserA.analysePerSubject(inputFile);
		long stop2 = System.currentTimeMillis();
		
		PropertyAnalyser.analyseTempPropertyDatasets();
		ClassAnalyserA.analyseTempClassDatasets();
		long stop3 = System.currentTimeMillis();

		VoID.cb.endDocument();
		
		logger.info("Completed initial analysis in "+(stop1 - startTime)+"ms");
		logger.info("Completed writing temp class datasets to files "+(stop2 - stop1)+"ms");
		logger.info("Completed analysis of temp datasets in "+(stop3 - stop2)+"ms");
		logger.info(VoID.cb.toString());
		
	}
	
	public static void showHelp(){
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "voidstats", options );
		System.exit(-1);
	}
	
	protected static boolean isInputGzipped(){
		return isGzipped;
	}
}














