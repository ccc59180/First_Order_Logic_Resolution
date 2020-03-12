import javax.naming.spi.Resolver;
import java.io.*;
import java.util.*;

public class homework {

    private String fileName;
    private String[] queries, statements;
    private int numberOfQueries, numStatements;
    private boolean[] result;
    private homework(String fileName) {
        this.fileName = fileName;
        queries = null;
        statements = null;
        numberOfQueries = 0;
        numStatements = 0;
    }

    private void readFile() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        numberOfQueries = Integer.parseInt(bufferedReader.readLine());
        queries = new String[numberOfQueries];
        result = new boolean[numberOfQueries];
        for ( int i = 0; i < numberOfQueries; i++){
            queries[i] = bufferedReader.readLine();
        }
        numStatements = Integer.parseInt(bufferedReader.readLine());
        statements = new String[numStatements];
        for ( int i = 0; i < numStatements; i++){
            statements[i] = bufferedReader.readLine();
        }
        bufferedReader.close();
    }

    private void getResult() {
        FormKB kb = new FormKB(statements);
        for (int i = 0; i < queries.length; i++) {
            HashMap<String,List<String>> predicateMap =  kb.getPredicateMap();
            HashMap<String,List<String>> predicateMapClone = new HashMap<>();
            Set<String> keys = predicateMap.keySet();
            for (String key : keys){
                List<String> list = predicateMap.get(key);
                List<String> listClone =  new ArrayList<String>();
                listClone.addAll(list);
                predicateMapClone.put(key, listClone);
            }

            Set<String> classMap = kb.getClassMap();
            HashSet<String> classMapClone = new HashSet<String>();
            classMapClone.addAll(classMap);

            Resolution res =  new Resolution(classMapClone, predicateMapClone);
            result[i] = res.resolve(queries[i]);
        }
    }




    private void writeFile() throws IOException {
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter("output.txt"));
        for (int i = 0; i < result.length - 1; i++){
            if (result[i]){
                fileWriter.write("TRUE\n");
            } else {
                fileWriter.write("FALSE\n");
            }
        }
        if (result[result.length - 1]){
            fileWriter.write("TRUE");
        } else {
            fileWriter.write("FALSE");
        }
        fileWriter.close();
    }

    public static void main(String[] args) throws IOException {
        homework hw = new homework("/Users/chris/IdeaProjects/561_homework3/src/input.txt");
        hw.readFile();
        hw.getResult();
        hw.writeFile();
    }

}
