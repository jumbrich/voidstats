package org.semanticweb.yars.voidstats;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.namespace.RDF;
import org.semanticweb.yars.nx.namespace.VOID;

public class PerSubjectIterator implements Iterator<Node[]> {
	Iterator<Node[]> input = null;
	Iterator<Node[]> current = null;
	Node[] last = null;
	HashSet<Node> currentClasses = null;
	ArrayList<Node[]> prev = new ArrayList<Node[]>();
	boolean newSubj = false;
	
	boolean removeDupes = false;
	
	public PerSubjectIterator(Iterator<Node[]> data){
		this(data,false);
	}	
	
	public PerSubjectIterator(Iterator<Node[]> data, boolean removeDupes){
		input = data;
		this.removeDupes = removeDupes;
		load();
	}	
	
	public boolean hasNext() {
		if(current==null){
			return false;
		} else if(!current.hasNext()){
			load();
			return hasNext();
		} else{
			return true;
		}
	}

	public Node[] next() {
		newSubj = false;
		if(hasNext()){
			return current.next();
		}
		else throw new NoSuchElementException();
	}
	
	public boolean newSubject(){
		return newSubj;
	}
	
	public HashSet<Node> getCurrentClasses(){
		return currentClasses;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	private void load(){
		current = null;
		currentClasses = new HashSet<Node>();
		prev = new ArrayList<Node[]>();
		
		if(!input.hasNext()){
			return;
		}
		
		if(last==null){
			last = input.next();
		}
		

		if(last[1].equals(RDF.TYPE)){
			currentClasses.add(last[2]);
		}
		prev.add(last);
		
		while(input.hasNext()){
			Node[] next = input.next();
			//System.out.print("\nnext[0]: "+next[0].toN3());
			if(next[0].equals(last[0])){
				if(!removeDupes || !NodeComparator.NC.equals(next, last)){
					prev.add(next);
					if(next[1].equals(RDF.TYPE)){
						currentClasses.add(next[2]);
					}
					last = next;
				}
			} else{
				last = next;
				break;
			}
		}
		
		newSubj = true;
		current = prev.iterator();
	}
	
	public void outPutClasses(){
		Iterator<Node> i = currentClasses.iterator();
		while (i.hasNext()){
			System.out.print("\ni.next(): "+i.next());
		}
	}
}
