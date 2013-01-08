package org.semanticweb.yars.voidstats;

import java.util.HashMap;

import org.semanticweb.yars.nx.Node;

public class Vocabs {
	
	protected static HashMap<String, Integer> vocabMap = new HashMap<String, Integer>();

	protected static void createVocab(Node in){
		createVocabSubstring(in);
	}
	
	private static void createVocabSubstring(Node in){
		//System.out.println("Vocabs: "+Arrays.toString(in));
		String s = in.toString();
		if (s.contains("#")){
			s =  s.substring(0, s.lastIndexOf("#"));
			Integer count = vocabMap.get(s);
			if (count == null)
				vocabMap.put(s, 1);
			else
				vocabMap.put(s, count+1);
			return;
		}
		if (s.contains("/")){
			s =  s.substring(0, s.lastIndexOf("/")+1);
			Integer count = vocabMap.get(s);
			if (count == null)
				vocabMap.put(s, 1);
			else
				vocabMap.put(s, count+1);
			return;
		}
	}	
}