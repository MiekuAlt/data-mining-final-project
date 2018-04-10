# data-mining-final-project

CSCI 4144 Final Project
Greg Mailman B00695833
Michael Altair B00599791
Samantha Williamson B00690535

Note) The code for this project utilizes code recycled from Ass3 and implements HWA(O) between Ass3's Apriori and Association Algorithms 

Compiling)
1) Place the data files and importance charts alongside Main.java.
2) run "javac Main.java" in the terminal
3) run "java Main" in the terminal

Running the program) 
1) Once it runs, you will be prompted for the input file and values for the support and confidence.
2) Input the local path to the file in question (supermarket-test-1.csv if it is in the same directory for example), and either a percentage or a decimal for the support and confidence.
3) The program will give errors in the console if the numbers are out of scope, or if it cannot find the file.
4) After processing, the program will ask you if you want to use HWA(O). (If you don't, the program will run as the normal Apriori from ass3).
5) You will be prompted for a minimum score to use for the HWA(O) algorithm (An example score would be .10).
6) You will next be prompted for the local path of the importance chart. (Use imp-chart.csv for example).
7) After processing, you will be informed that the rules have been written in Rules.txt within the directory of Main.java.

The program only has one supporting java file for the main file, we used a black box approach in that the main file inputs data and gets data back, without doing any manipulating itself.

Apriori.java has the following methods: 

(Original Apriori Ass3 Methods)
public static List<String> runApriori(List<List<String>> data, double supportInput, double confidenceInput)
private static void runAssociation(List<KeyValue> freqTable)
private static List<KeyValue> genTables()
private static List<List<String>> expandItemSet(List<KeyValue> table, int n)
public static <T> List<List<T>> combination(List<T> values, int size)
private static List<String> convertTableToData(List<KeyValue> table)
private static double calcSupport(List<Item> itemsChecked)
private static List<KeyValue> buildFreq(List<KeyValue> cand)
private static List<KeyValue> buildCand(List<List<String>> itemSets)
private static List<KeyValue> buildFirstCand(List<Item> itemSets)
private static List<Item> findUniquesInData(List<List<String>> data)
private static List<String> removeDups(List<String> withDups)

(New HWA(O) Methods to be used with this Project)
private static List<KeyValue> runHWAO(List<KeyValue> freqData)
private static List<KeyValue> calcPruneTWeight(List<KeyValue> weightedTable)
private static double calcTWeight(KeyValue curRow)
private static long binomialCoefficient(int n, int k)
private static List<KeyValue> setHWeights(List<KeyValue> tableToAddWeight)
private static int findWeight(String cat)
private static void getHWAInput()
private static boolean checkIfHWA()

Overall Flow) 
Apriori Algorithm:
- The input system reads the file line by line, using the first line to set up a tagging system, and adding the tags to each of the data values in the following lines.
An example of this would be "outlook=sunny". These data points are stored in a two dimensional list, which are then passed to the runApriori function.
- runApriori goes through the given list, finds all unique item-sets and builds a candidate table from this. It loops using the Apriori algorithm, checking supports for the candidates, pruning sets that don't match, then expanding the item-sets until there are no more items in the frequency table.
- The frequency table is then sent to the HWA(O) algorithm.

HWA(O) Algorithm:
- Receives the min_score and path for the importance chart from the user through the input system.
- Each item gets its weight assigned based on the importance chart.
- For each item-set, the total weight (TWeight) is calculated.
- Using the TWeight and the support value for the item-set, the score is calculated.
- Checks to see if the score is equal to or surpasses the min_score, if it isn't the item-set if pruned out, while the passing item-sets are stored in a weighted frequency table.
- The weighted frequency table is then sent to the association algorithm.

Association Algorithm:
- After this a list of candidate rules remains, which is passed into a rule generation algorithm, which generates all possible rules, then prunes based on the given confidence. Once this is done, it formats the rules for output and returns a new list containing the discovered rules.
- This list is passed back to the main file, which exports it into a Rules file along with the other information required, such as the number of rows, number of rules, support, confidence, and min_score.
