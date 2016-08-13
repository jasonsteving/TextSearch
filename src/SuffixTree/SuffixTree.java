package SuffixTree;

import java.io.Serializable;
import java.util.Hashtable;

//TODO: Make use of the Serializable interface to actually save the tree that gets built
//for a file so that it doesn't ever need to get built again for the same version of the file.
public class SuffixTree implements Serializable {
	private static final long serialVersionUID = 1L; //added by default to make serializable

	public class Node implements Serializable{
		private static final long serialVersionUID = 1L; //added by default to make serializable
		
		//Using a hashtable instead of an array for this is simply a matter of laziness so that
		//I did not have to read the file to determine what the alphabet should be for this file for the 
		//"nextTable" as I've so nicely named it. Not sure if this is a space bottleneck or not, 
		//but depending on the size of the alphabet I get the feeling that this shouldn't bloat things 
		//toooo much in comparizon to each node holding an array large enough for the whole alphabet
		//to be mapped to it.
		public Hashtable<Character, Node> nextTable = new  Hashtable<>();
		public int lineFoundAt, indexInLine, occurrances;
		
		public Node(int lineFoundAt, int indexInLine){
			this.lineFoundAt = lineFoundAt;
			this.indexInLine = indexInLine;
			this.occurrances = 1;
		}
		
		public Node addChild(char c, int lineFoundAt, int indexInLine){
			Node child = new Node(lineFoundAt, indexInLine); //it receives its location from the start
										//of the string that it was associated with to begin with for
										//the sake of associating this char with the current string
			nextTable.put(c, child);
			return child; //return this new child Node to make keeping track of the next
						  //Node to consider during insertions easy
		}
		
		public Node getChild(char c){
			return nextTable.get(c);
		}
	}
	
	public class FindResults implements Serializable{
		private static final long serialVersionUID = 1L; //added by default to make serializable
		
		public boolean stringFound; //true if the whole string was found
		public int prefixLengthFound;
		public Node prefixLeafNode; //the last node in the tree representing the prefix found
									//this will be root if no part of the searched string was
									//in the tree as the root represents the empty string
		
		public FindResults(boolean found, int prefixLengthFound, Node prefixLeafNode){
			this.stringFound = found;
			this.prefixLengthFound = prefixLengthFound;
			this.prefixLeafNode = prefixLeafNode;
		}
	}
	
	private Node root = new Node(0,0); //the root of the suffix tree representing the empty string
											   //which is obviously trivially found at the beginning of file
	
	public void insert(String s, int lineFoundAt, int indexInLine){
		if(s.length() == 0)
			return;		
		//convert s to lowercase just to make this case insensitive by default for now
		s = s.toLowerCase();		
		//just find the Node at which to append this unless already found
		FindResults findRes = find(s, true);
		if(findRes.stringFound)
			return; //the given string is already in the tree, don't need to do anything
		else{
			//start appending the remaining part of the string not found in the tree to the 
			//returned prefixLeafNode so that the tree will now contain this word
			Node leafNode = findRes.prefixLeafNode;
			for(int i = findRes.prefixLengthFound; i < s.length(); i++){
				//add the current char in s to the current leafNode and update leafNode
				leafNode = leafNode.addChild(s.charAt(i), lineFoundAt, indexInLine);
			}//done
		}
	}
	
	public FindResults find(String s){
		return find(s, false);
	}
	
	public FindResults find(String s, boolean insertion){
		if(s.length() == 0)
			return new FindResults(true, 0, root); //trivially found the empty string at beginning
		else{
			//convert s to lowercase just to make this case insensitive by default for now
			s = s.toLowerCase();
			Node curr = root;
			for(int i = 0; i < s.length(); i++){
				Node next = curr.getChild(s.charAt(i));				
				if( next != null){
					curr = next;
					if(insertion)//incr occurrance count if we're doing a find as part of an insertion
						curr.occurrances++; //increment our count
				}
				else
					return new FindResults(false, i, curr);
			}
			return new FindResults(true, s.length(), curr); //total success! found whole string, s.
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		SuffixTree st = new SuffixTree();
		st.insert("hello", 0, 0);
		FindResults res = st.find("hell");
		System.out.println("hell: " + res.stringFound);
		res = st.find("help");
		System.out.println("help: " + res.stringFound);
		System.out.println("help: " + res.prefixLengthFound);
		st.insert("help", 0, 5);
		res = st.find("help");
		System.out.println("help: " + res.stringFound);
		res = st.find("hellfire");
		System.out.println("hellfire: " + res.stringFound);
		System.out.println("hellfire: " + res.prefixLengthFound);
		//LOOKS LIKE IT ALL WORKS!!!
		res = st.find("hell");
		System.out.println("Found at location: " + res.prefixLeafNode.lineFoundAt + " " + res.prefixLeafNode.indexInLine);
		res = st.find("help");
		System.out.println("Found at location: " + res.prefixLeafNode.lineFoundAt + " " + res.prefixLeafNode.indexInLine);
		res = st.find("hel");
		System.out.println(res.prefixLeafNode.occurrances);
	}

}
