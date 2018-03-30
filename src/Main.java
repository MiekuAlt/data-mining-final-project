import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;

public class Main {

	private static String filename;
	private static double support;
	private static double confidence;

	public static void main(String[] args) {

		
		// Initial User Input - filename
		System.out.print("Welcome to Apriori Miner!\nPlease enter data file's name: ");
		Scanner in = new Scanner(System.in);
		filename = in.nextLine();
		
		// Initial User Input - support
		// Accepts a number between 0 and 100
		int reloop;
		do {
			reloop = 0;
			System.out.print("Please enter support percentage or a decimal: ");
			in = new Scanner(System.in);
			try {
				support = in.nextDouble();
				if (support>=1 && support<=100) {
					support = support/100;
				}
				while (support>1 || support<0) {
					System.out.print("Invalid number, please enter a percentage or a decimal: ");
					support = in.nextDouble();
					if (support>=1 && support<=100) {
						support = support/100;
					}
				}
			} catch(Exception e) {
				System.out.println("Incorrect format, please enter a percentage or a decimal: ");
				reloop++;
			}
		} while(reloop != 0);

		
		// Initial User Input - confidence
		// Accepts a number between 0 and 100
		do {
			reloop = 0;
			System.out.print("Please enter confidence percentage: ");
			in = new Scanner(System.in);
			try {
				confidence = in.nextDouble();
				if (confidence>=1 && confidence<=100) {
					confidence = confidence/100;
				}
				while (confidence>1 || confidence<0) {
					System.out.print("Invalid number, please enter a percentage between 0 and 100: ");
					confidence = in.nextDouble();
					if (confidence>=1 && confidence<=100) {
						confidence = confidence/100;
					}
				}
			} catch(Exception e) {
				System.out.println("Incorrect format, please enter a percentage between 0 and 100: ");
				reloop++;
			}
		} while(reloop != 0);

		// Just closing the scanner
		in.close();

		// Used to count the rows
		int numRows = 0;
		List<List<String>> Data = new ArrayList<List<String>>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
			// Separating the first line and setting up the tags to add to the data
			String line = br.readLine();
			String[] tags = line.split(" +");

			// Loops through the file, reading line by line
			// Splits the line, then adds the tags to the corresponding value
			// Adds the split line into the data array
			line = br.readLine();
			while (line != null) {
				String[] split = line.split(" +");
				if(split.length == tags.length) {
					numRows++;
					for (int i = 0; i < split.length; i++) {
						split[i] = tags[i] + "=" + split[i];
					}
				}

				Data.add(Arrays.asList(split));
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

		// Passes the input to the apriori algorithm. Gets a list of rules in return.
		List<String> rules = Apriori.runApriori(Data, support, confidence);

		// Outputs the summary and discovered rules to a file named "Rules"
		// Overwrites a file if it already exists, and creates one if it does not.
		BufferedWriter bw = null;
		try {
			File outPut = new File("Rules");
			if (!outPut.exists()) {
				outPut.createNewFile();
			}

			FileWriter fw = new FileWriter(outPut);
			bw = new BufferedWriter(fw);

			bw.write("Summary: \n");
			bw.write("Total rows in the original set: " + numRows + "\n");
			bw.write("Total rules discovered: " + rules.size() + "\n");
			bw.write("The selected measures: Support=" + support + ", Confidence=" + confidence + "\n");
			bw.write("----------------------------\n");
			bw.write("Discovered rules:\n\n" + rules);
		} catch(Exception e) {
			System.out.println("Error outputting the rules.");
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch(Exception e) {
				System.out.println("Error closing the writer.");
			}
		}
	}
}
