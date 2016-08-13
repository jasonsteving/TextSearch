/*
 * This program will take a text file as an argument to build a syntax tree out of it
 * (if one does not already exist for this version of the file) and then take queries
 * for strings to be searched for in the file. Currently only a binary yes/no result
 * will be returned for each query.
 * 
 * Use the "--serialize" option if you want the suffix tree created for the specified 
 * file to be serialized. CAUTION: It turns out that it's actually WAYYY SLOWER to serialize 
 * or deserialize this tree structure than it is to just create the actual tree itself
 * entirely each run with the limitation of query strings being below 21 chars. I highly
 * recommend not using the serialize option at all. It was good to get more practice 
 * with serialization, but it turns out not to be useful in this instance.
 * 
 * Terminate the program using ^C.
 * 
 *  Usage: java SearchFile [filename] [--serialize (optional)]
 *  		>>> [query]
 *  		>>> [query]
 *  		...
 */

package Search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import SuffixTree.SuffixTree;
import SuffixTree.SuffixTree.FindResults;

public class SeearchFile {

	public static void main(String ... args){
		//get the name of the file to read from
		String filename = args[0];
		//find out if we're supposed to save the suffix tree we'll build or not
		boolean saveTree = args.length > 1 && args[1].compareToIgnoreCase("--serialize") == 0;
		
		//open that file and read the whole thing in (this represents the first suffix)
		//StringBuilder suffixBuilder = null;
		ArrayList<String> lines = null;
		try{
			//Stream<String> fileStream = Files.lines(Paths.get(filename));
			//suffixBuilder = new StringBuilder(fileStream.collect(Collectors.joining()));
			//fileStream.close();
			lines = (ArrayList<String>) Files.readAllLines(Paths.get(filename));
		} catch (IOException e){
			e.printStackTrace();
		}
		
		//check if a suffix tree for this file has already been serialized
		File serializedTree = new File(filename + ".ser");
		SuffixTree suffixTree = null;
		if(serializedTree.exists()){
			System.out.println("Found Serialized suffix tree for the specified file!\nDeserializing now.\n...");
			try {
				FileInputStream fileIn = new FileInputStream(filename + ".ser");
				ObjectInputStream objIn = new ObjectInputStream(fileIn);
				suffixTree = (SuffixTree) objIn.readObject();
				objIn.close();
				fileIn.close();
				System.out.println("Done Deserializing stored suffix tree for this file!");
				System.out.println("*******************************************************************\n");
			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("Could not Deserialize the serialized suffix tree.");
				System.out.println("Giving up on this. Will rebuild tree and write new serialized tree to file.");
				//e.printStackTrace(); //NO POINT TO FAIL HERE FOR NOW
			}
		}

		//Don't have a suffix tree because we either we haven't built one for this file yet,
		//or because the deserialization process failed. Either way, build tree now
		if(suffixTree == null){
			System.out.println("Building a Suffix Tree for the specified file.");
			System.out.println("This may take some time, please be patient.\n...");
			//create a suffix tree to represent the file contents
			suffixTree = new SuffixTree();
			
			//insert all suffixes in the file to the suffix tree
			/*int i = 0;
			while(i < suffixBuilder.length() ){//suffixBuilder.length() > 0){
				//TODO: Passing only the first 20 chars of the current suffix 
				//rather than the entire suffix is a MASSIVE SPACE SAVER. But 
				//it would still be nice to be able to search arbitrary length strings,
				//so try to extend the SuffixTree to handle that by modifying find()
				suffixTree.insert(suffixBuilder.substring(i, Math.min(i + 19, suffixBuilder.length() - 1)));
				i++;
				//suffixTree.insert(suffixBuilder.toString());
				//suffixBuilder.deleteCharAt(0); // get rid of the first char in the suffix
			}*/
			
			for(int lineNum = 0; lineNum < lines.size(); lineNum++){
				String currLine = lines.get(lineNum);
				for(int i = 0; i < currLine.length(); i++)
					suffixTree.insert(currLine.substring(i), lineNum + 1, i);
			}
	
			System.out.println("Done building Suffix Tree!");

			if(saveTree){
				//now that you've spent alllll the time building that suffix tree, serialize it to 
				//a file so we can just read it in later if possible to save that time.
				System.out.println("Serializing the tree now for future convenience.\n...");
				
				try {
					FileOutputStream fileOut = new FileOutputStream(filename + ".ser");
					ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
					objOut.writeObject(suffixTree);
					objOut.close();
					fileOut.close();
					System.out.println("Suffix tree Serialization for this file was successful!");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Suffix tree Serialization for this file FAILED!\nMoving on.");
					//e.printStackTrace(); NOT SUPER IMPORTANT TO FAIL HERE
				}
			}
			
			System.out.println("*******************************************************************\n");
		}
			
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		do{ // now that you've read in the whole file, take queries all day until ^C
			System.out.print("Enter the string you would like to search \"" + filename + "\" for:\n>>>");
			String queryString = scan.nextLine();
			
			//determine if this query string is present in the file
			FindResults res = suffixTree.find(queryString);
			if(res.stringFound){
				int lineNum = res.prefixLeafNode.lineFoundAt;
				int lineIndex = res.prefixLeafNode.indexInLine;
				int occurrances = res.prefixLeafNode.occurrances;
				System.out.println("---The query string \"" + queryString + "\" was found in the file " + occurrances + " time(s)!");
				System.out.println("---First occurrance at Line: " + lineNum + " Index: " + lineIndex + " !\n");
			}
			else
				System.out.println("---The query string \"" + queryString + "\" was NOT found!\n");
		} while(true);
	}
	
}
