import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public final class Apriori {

	private static List<List<String>> inputData;
	private static double minSupport, minConfidence;
	
	private static List<KeyValue> finalTable;
	
	public static double min_score;
	private static List<List<String>> imp_chart;
	private static int maxLevel;
	
	public static List<String> runApriori(List<List<String>> data, double supportInput, double confidenceInput) {
		inputData = data;
		minSupport = supportInput;
		minConfidence = confidenceInput;
		finalTable = new ArrayList<KeyValue>();
 		genTables();
 		
 		// HWA(O) is implemented here
 		if(checkIfHWA()) {
 			getHWAInput();
 			System.out.println("-------Processing-------");
 			finalTable = runHWAO(finalTable);
 		}
 		
		// Association aspect of the algorithm to generate the rules
		List<String> rules = runAssociation(finalTable);
		List<String> output = new ArrayList<String>();

		rules = removeDups(rules);
		
		for(int i = 0; i <rules.size(); i++) {
			output.add("Rule#" + (i + 1) + ": " + rules.get(i) + "\n");
		}
		
		return output;
	}
	
	// Runs HWA(O), takes in the frequent itemset data, and trims it based on the hierarchical weighing
	private static List<KeyValue> runHWAO(List<KeyValue> freqData) {
		maxLevel = 0; // Initializing the maxLevel
		List<KeyValue> weightedTable = setHWeights(freqData);
		
		weightedTable = calcPruneTWeight(weightedTable);
		
		return weightedTable;
	}
	
	// Calculates the total weight, finds the score, and prunes out the low scoring
	private static List<KeyValue> calcPruneTWeight(List<KeyValue> weightedTable) {
		
		// Iterate through each itemset, going in reverse since some will be pruned
		for(int i = weightedTable.size() - 1; i >= 0; i--) {
			double score = calcTWeight(weightedTable.get(i)) * weightedTable.get(i).support;
			// Pruning out failing itemsets
			if(score < min_score) {
				weightedTable.remove(i);
			}
		}
		
		return weightedTable;
	}
	
	// Calculates the TWeight
	private static double calcTWeight(KeyValue curRow) {
		double result = 0;
		double sumHWeight = 0;
		int numItemset = 0;
		
		// Determining the number of items in this row
		numItemset = curRow.itemSet.size();
		
		// Determining the sum of HWeight
		for(int i = 0; i < numItemset; i++) {
			sumHWeight += curRow.itemSet.get(i).hWeight;
		}
		result = sumHWeight / (binomialCoefficient(numItemset, 2) * maxLevel);
		return result;
	}
	
	// Calculates the binomial coefficient, n choose k
	// Adapted from https://rosettacode.org/wiki/Evaluate_binomial_coefficients#Java
	private static long binomialCoefficient(int n, int k) {
		if (k>n-k) {
            k=n-k;
		}

        long b=1;
        for (int i=1, m=n; i<=k; i++, m--) {
            b=b*m/i;
        }

        return b;
	}
	
	// Sets the HWeight for each of the items
	private static List<KeyValue> setHWeights(List<KeyValue> tableToAddWeight) {
		
 		for(int r = 0; r < tableToAddWeight.size(); r++) {
 			for(int c = 0; c < tableToAddWeight.get(r).itemSet.size(); c++) {
 				String[] split = tableToAddWeight.get(r).itemSet.get(c).value.split("=");
 				tableToAddWeight.get(r).itemSet.get(c).setHWeight(findWeight(split[0]));
 			}
 		}
		
 		return tableToAddWeight;
	}
	
	// Finds the weight of a specific category
	private static int findWeight(String cat) {
		int weight = -1;
		for(int i = 0; i < imp_chart.size(); i++) {
			String[] catChunks = imp_chart.get(i).get(0).split("=");
			String catFromTable = catChunks[1];
			if(catFromTable.equals(cat)) {
				String[] impChunks = imp_chart.get(i).get(1).split("=");
				weight = Integer.parseInt(impChunks[1]);
				// Finding the Max level with this importance chart for later use
				if(weight > maxLevel) {
					maxLevel = weight;
				}
			}
		}
		
		return weight;
	}
	
	// Getting the additional user inputs required for HWA(O)
	private static void getHWAInput() {
		
		// Getting the minimum score
		int reloop;
		do {
			reloop = 0;
			System.out.print("Please enter the minimum score: ");
			Scanner in = new Scanner(System.in);
			try {
				min_score = in.nextDouble();
			} catch(Exception e) {
				System.out.println("Incorrect format, please enter a number: ");
				reloop++;
			}
		} while(reloop != 0);
		
		// Getting the importance chart
		System.out.print("Please enter the importance chart's filename: ");
		Scanner in = new Scanner(System.in);
		String imp_name = in.nextLine();
		
		// Used to count the rows
		imp_chart = new ArrayList<List<String>>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(imp_name));
			// Separating the first line and setting up the tags to add to the data
			String line = br.readLine();
			String[] tags = line.split(",");

			// Loops through the file, reading line by line
			// Splits the line, then adds the tags to the corresponding value
			// Adds the split line into the data array
			line = br.readLine();
			while (line != null) {
				String[] split = line.split(",");
				if(split.length == tags.length) {
					for (int i = 0; i < split.length; i++) {
						split[i] = tags[i] + "=" + split[i];
					}
				}

				imp_chart.add(Arrays.asList(split));
				line = br.readLine();
			}
		} catch(Exception e) {
			System.out.println("No file found.");
			return;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			}
			catch (Exception e) {
				System.out.println("Error closing the reader");
			}
		}
	}
	
	// Checks if the user wants to use the HWA(O) algorithm
	private static boolean checkIfHWA() {
		boolean result = false;
		
		System.out.print("Do you want to use HWA(O)? (y/n): ");
		
		int reloop;
		do {
			reloop = 0;
			Scanner in = new Scanner(System.in);
			String reply;

			reply = in.nextLine();
			if (reply.equals("y") || reply.equals("Y")) {
				result = true;
			} else if (reply.equals("n") || reply.equals("N")) {
				result = false;
				System.out.println("-------Processing-------"); // Because the processing will continue for Association
			} else {
				System.out.print("Invalid Response - Do you want to use HWA(O)? (y/n): ");
				reloop++;
			}

		} while(reloop != 0);
		
		return result;
	}
	
	// Begins the Association aspect of the algorithm
	private static List<String> runAssociation(List<KeyValue> freqTable) {
		List<Rule> rules = new ArrayList<Rule>();
		// Breaking the table apart into rows
		for(int r = 0; r < freqTable.size(); r++) {
			rules.addAll(rulesForRow(freqTable.get(r)));
		}
		
		// Purging repeats from the rules generated from the two sets of rows in the table
		for(int i = rules.size() - 1; i >= 0; i--) {
			if(rules.get(i).hasRepeat() || rules.get(i).confidence < minConfidence) {
				rules.remove(i);
			}
		}
								
		List<String> output = new ArrayList<String>();
		for(int i = 0; i < rules.size(); i++) {
			output.add(rules.get(i).toString());
		}
		
		return output;
	}
	
	// Generates the rules for the row of the frequency table
	private static List<Rule> rulesForRow(KeyValue rowValue) {
		
		// Determines all the subsets required for the row
		List<List<String>> subsets = genSubsets(rowValue.itemSet);
		
		List<Rule> rules = genImplications(subsets);
		
		return rules;
	}
	
	// Finds implications
	private static List<Rule> genImplications(List<List<String>> subsets) {
		List<Rule> rules = new ArrayList<Rule>();
		
		for(int r = 0; r < subsets.size(); r++) {
			List<Item> base = new ArrayList<Item>();
			for(int c = 0; c < subsets.get(r).size(); c++) {
				base.add(new Item(subsets.get(r).get(c)));
			}
			
			for(int ri = 0; ri < subsets.size(); ri++) {
				List<Item> implication = new ArrayList<Item>();
				for(int ci = 0; ci < subsets.get(ri).size(); ci++) {
					implication.add(new Item(subsets.get(ri).get(ci)));
				}
				rules.add(new Rule(base, implication));
			}
		}
		
		// Purging repeats
		for(int i = rules.size() - 1; i >= 0; i--) {
			if(rules.get(i).hasRepeat()) {
				rules.remove(i);
			}
		}
		
		return rules;
	}
	
	// Breaks the set into all the subsets needed for association
	private static List<List<String>> genSubsets(List<Item> initialSets) {
		 List<List<String>> subsets = new ArrayList<List<String>>();
		 // Convert the item list to a string list for processing
		 List<String> setString = new ArrayList<String>();
		 for(int i = 0; i <initialSets.size(); i++) {
			 setString.add(initialSets.get(i).value);
		 }
		 
		 int numSets = setString.size();
		 for (int i = 1; i < numSets; i++)
			 subsets.addAll(combination(setString, i));
		 
		 return subsets;
	}
		
	// Generates the tables until there are none left
	private static void genTables() {
		
		List<List<String>> curDataSets;
		List<Item> uniqueItems;
		List<KeyValue> candTable;
		List<KeyValue> freqTable;
		
		uniqueItems = findUniquesInData(inputData);
		candTable = buildFirstCand(uniqueItems);
		freqTable = buildFreq(candTable);
		
		int iteration = 2;
		while(!freqTable.isEmpty()) {
			curDataSets = expandItemSet(freqTable, iteration);
			candTable = buildCand(curDataSets);

			freqTable = buildFreq(candTable); 
			iteration++;
		}
		
	}
	
	// Expands the itemSets to the nth based on an example from https://stackoverflow.com/questions/5162254/all-possible-combinations-of-an-array
	private static List<List<String>> expandItemSet(List<KeyValue> table, int n) {
		
		List<String> curData = convertTableToData(table);
		List<List<String>> expandSet = new LinkedList<List<String>>();

	    expandSet.addAll(combination(curData, n));
		return expandSet;
	}
	
	// This is based on an example from https://stackoverflow.com/questions/5162254/all-possible-combinations-of-an-array
	public static <T> List<List<T>> combination(List<T> values, int size) {

	    if (0 == size) {
	        return Collections.singletonList(Collections.<T> emptyList());
	    }

	    if (values.isEmpty()) {
	        return Collections.emptyList();
	    }

	    List<List<T>> combination = new LinkedList<List<T>>();
	    T actual = values.iterator().next();
	    List<T> subSet = new LinkedList<T>(values);
	    subSet.remove(actual);
	    List<List<T>> subSetCombination = combination(subSet, size - 1);

	    for (List<T> set : subSetCombination) {
	        List<T> newSet = new LinkedList<T>(set);
	        newSet.add(0, actual);
	        combination.add(newSet);
	    }

	    combination.addAll(combination(subSet, size));
	    return combination;
	}
	// Converts a table into the same dataType this is read on input
	private static List<String> convertTableToData(List<KeyValue> table) {
		
		List<String> newData = new ArrayList<String>();
		
		for(int r = 0; r < table.size(); r++) {
			for(int c = 0; c < table.get(r).itemSet.size(); c++) {
				newData.add(table.get(r).itemSet.get(c).value);
			}
		}	
		
		return removeDups(newData);
	}
	
	// Calculates the support value on an itemSet
	public static double calcSupport(List<Item> itemsChecked) {
		int numSupport = 0;

		for(int r = 0; r < inputData.size(); r++ ) {
			int numCheck = itemsChecked.size();
			for(int i = 0; i < itemsChecked.size(); i++) {
				for(int c = 0; c < inputData.get(r).size(); c++) {
					if(itemsChecked.get(i).value.equals(inputData.get(r).get(c))) {
						numCheck--;
					}
				}
				if(numCheck == 0) {
					numSupport++;
				}
			}
		}
		return numSupport / ((double)inputData.size());
	}
	
	// Builds the frequency table
	private static List<KeyValue> buildFreq(List<KeyValue> cand) {

		for(int r = cand.size() - 1; r >= 0; r--) {
			if(cand.get(r).support < minSupport) {
				cand.remove(r);
			}
		}
		
		for(int i = 0; i < cand.size(); i++) {
			finalTable.add(cand.get(i));
		}
		return cand;
	}
	
	// Builds the typical candidate table
	private static List<KeyValue> buildCand(List<List<String>> itemSets) {
		List<KeyValue> canTable = new ArrayList<KeyValue>();
		
		// Populating the items
		for(int i = 0; i < itemSets.size(); i++) {
			List<Item> entry = new ArrayList<Item>();
			for(int j = 0; j < itemSets.get(i).size(); j++) {
				entry.add(new Item(itemSets.get(i).get(j)));
			}
			canTable.add(new KeyValue(entry, calcSupport(entry)));
		}
		return canTable;
	}
	
	// Builds the first candidate table
	private static List<KeyValue> buildFirstCand(List<Item> itemSets) {
		List<KeyValue> canTable = new ArrayList<KeyValue>();
		
		// Populating all the items
		for(int i = 0; i < itemSets.size(); i++) {
			List<Item> entry = new ArrayList<Item>();
			entry.add(itemSets.get(i));
 			canTable.add(new KeyValue(entry, calcSupport(entry)));
		}
		return canTable;
	}
	
	
	// Finds all of the unique items in the data
	private static List<Item> findUniquesInData(List<List<String>> data) {
		List<String> allItems = new ArrayList<String>();
		
		for(int r = 0; r < data.size(); r ++) {
			for(int c = 0; c < data.get(r).size(); c++) {
				allItems.add(data.get(r).get(c));
			}
		}
		
		// Removing duplicates
		List<String> uniques = removeDups(allItems);

		List<Item> uniqueItems = new ArrayList<Item>();
		for(int i = 0; i < uniques.size(); i++) {
			uniqueItems.add(new Item(uniques.get(i)));
		}
		
		return uniqueItems;
	}
	
	// Removes duplicates from a list of strings
	private static List<String> removeDups(List<String> withDups) {
		// This is derived from an example here https://stackoverflow.com/questions/203984/how-do-i-remove-repeated-elements-from-arraylist
		Set<String> hs = new HashSet<>();
		hs.addAll(withDups);
		List<String> uniques = new ArrayList<String>();
		uniques.addAll(hs);
		
		return uniques;
	}
	
} // end of the Apriori class

// A table row in both candidate and frequency tables
class KeyValue {
	List<Item> itemSet;
	double support;
	public KeyValue(List<Item> itemSet, double support) {
		this.itemSet = itemSet;
		this.support = support;
	}
}

// The item and its value
class Item {
	String value;
	int hWeight;
	public Item(String value) {
		this.value = value;
	}
	
	public void setHWeight(int hWeight) {
		this.hWeight = hWeight;
	}
}

class Rule {
	List<Item> base;
	List<Item> implies;
	double confidence;
	double support; 
	
	public Rule(List<Item> base, List<Item> implies) {
		this.base = base;
		this.implies = implies;
		calcConfidence();
	}
	
	public String toString() {
		
		String baseString = "{ ";
		String impString = "{ ";
		
		for(int i = 0; i < base.size(); i++) 
			baseString += base.get(i).value + " ";
		baseString += "}";
		
		for(int i = 0; i < implies.size(); i++) 
			impString += implies.get(i).value + " ";
		impString += "}";
		
		return "(Support=" + support + ", " + "Confidence=" + confidence + ")\n" + baseString + "\n----> " + impString;
	}
	
	public boolean hasRepeat() {
		boolean result = false;

		for(int b = 0; b < base.size(); b++) {
			for(int i = 0; i < implies.size(); i++) {
				if(base.get(b).value.equals(implies.get(i).value)) {
					result = true;
					return result;
				}
			}
		}
		return false;
	}
	// Determines the confidence value of the rule, also fills in the support
	public void calcConfidence() {

		double baseSupport = Apriori.calcSupport(base);
		List<Item> entireSet = new ArrayList<Item>();
		for(int i = 0; i < base.size(); i++) {
			entireSet.add(base.get(i));
		}
		for(int i = 0; i < implies.size(); i++) {
			entireSet.add(implies.get(i));
		}
		double allSupport = Apriori.calcSupport(entireSet);
		
		double result = allSupport / baseSupport;
		
		// Reducing sigfigs, based on an example from: https://stackoverflow.com/questions/7548841/round-a-double-to-3-significant-figures
		BigDecimal bd = new BigDecimal(result);
		bd = bd.round(new MathContext(2));
		confidence = bd.doubleValue();
		
		bd = new BigDecimal(allSupport);
		bd = bd.round(new MathContext(2));
		support = bd.doubleValue();
	}
}
