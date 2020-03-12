import java.util.*;

public class FormKB {
    private List<HashMap<String,String>> mapList = new ArrayList<HashMap<String,String>>();
    private Set<String> classMap = new HashSet<String>();
    private HashMap<String,List<String>> predicateMap = new HashMap<String,List<String>>();
    public FormKB(String[] statements) {
        for ( int i = 0; i < statements.length; i++){
            statements[i] = findPredicates(statements[i]);
            statements[i] = replaceImp(statements[i]);
            statements[i] = eliminateDuplicates(statements[i]);
            pushToKB(statements[i],i);
        }
        Set<String> clauseMap = new HashSet<>();
        int i = 0;
        for (String clause : classMap){
            clause = standardizeVariables(clause, i);
            clauseMap.add(clause);
            i++;
        }
        classMap = clauseMap;
        for (String clause : classMap){
            fillKB(clause);
        }
    }

    public Set<String> getClassMap() {
        return classMap;
    }

    public void setClassMap(HashSet<String> classMap) {
        this.classMap = classMap;
    }

    public HashMap<String, List<String>> getPredicateMap() {
        return predicateMap;
    }

    public void setPredicateMap(HashMap<String, List<String>> predicateMap) {
        this.predicateMap = predicateMap;
    }


    private String findPredicates(String str) {
        HashMap<String, String> map = new HashMap<>();
        for ( int i = 0; i < str.length(); i++){
            if ( str.charAt(i) >= 65 && str.charAt(i) <= 90){
                int openIndex = -1;

                int j = i;
                while (str.charAt(j)!= ')'){
                    if (str.charAt(j) == '('){
                        openIndex = j;
                    }
                    j++;
                }
                String predicate = str.substring(i,openIndex);
                String operand = str.substring(i,j+1);
                String additions = "";
                while (map.get(predicate)!= null){
                    if (map.get(predicate).equals(operand)){
                        break;
                    } else {
                        predicate = predicate+"@";
                        additions = additions+"@";
//                        System.out.println(predicate);
                    }
                }

                map.put(predicate,operand);
                map.put("~"+predicate, "~"+operand);
//                System.out.println(predicate + "//" + operand);
                str = str.substring(0, openIndex)+ additions + str.substring(j+1);

                i = openIndex;
            }
        }
        mapList.add(map);
        return str;
    }


    private String eliminateDuplicates(String string) {
        String[] tokens = string.split("\\|");
        Set<String> tokenSet = new HashSet<String>();
        String token = "";
        for ( int i = 0; i < tokens.length; i++ ){
            if (tokenSet.add(tokens[i])){
                token  = token + tokens[i]+"|";
            }
        }
        token = token.substring(0, token.length()-1);
        return token;
    }

    private static String getPredicate(String query){
        return query.split("\\(")[0];
    }

    private String replaceImp(String str) {
        StringBuilder sb = new StringBuilder();
        int indexForImp = 0;
        for (char c : str.toCharArray()) {
            if (c == '=') break;
            indexForImp++;
        }
        if (indexForImp == str.length()) return str;
        String temp = str.substring(0, indexForImp);
        String[] strArr = temp.split("&");
        for (String aStrArr : strArr) {
            String tr = aStrArr.trim();
            if (tr.charAt(0) == '~') sb.append(tr.substring(1)).append(" | ");
            else sb.append("~").append(tr).append(" | ");
        }
        sb.append(str.substring(indexForImp + 3));

        return sb.toString();
    }

    private void pushToKB(String str, int statementNumber) {
        HashMap<String,String> map = mapList.get(statementNumber);
        StringBuilder sb = new StringBuilder();
        for (String st : str.split("\\|")) {
            sb.append(map.get(st.trim())).append("|");
        }
        classMap.add(sb.toString().substring(0, sb.length() - 1));
    }


    private String standardizeVariables(String str, int index) {
        StringBuilder sb = new StringBuilder();
        char[] literArr = str.toCharArray();
        for (int i = 0; i < literArr.length; i++) {
            sb.append(Character.toString(literArr[i]));
            if (isVar(i, literArr)) {
                sb.append(Integer.toString(index));
            }
        }
        return sb.toString();
    }

    private boolean isVar(int i, char[] literArr) {
        return i > 0 && i < literArr.length - 1 && ((literArr[i - 1] == '(' && literArr[i + 1] == ',') || (literArr[i - 1] == ',' && literArr[i + 1] == ',')
        || (literArr[i - 1] == ',' && literArr[i + 1] == ')') || (literArr[i - 1] == '(' && literArr[i + 1] == ')'))
                && Character.isLowerCase(literArr[i]);
    }

    private void fillKB(String clause) {
        String[] predicates = clause.split("\\|");
        for (int i =0 ; i < predicates.length; i++){
            predicates[i] = predicates[i].split("\\(")[0];
        }
        for ( int j =0 ; j < predicates.length; j++){
            predicateMap.computeIfAbsent(predicates[j], k -> new ArrayList<>()).add(clause);
        }
    }


//    public static void main(String[] args) {
//        String[] stArr = new String[5];
//        stArr[0] = "Take(x,Warfarin) => ~Take(x,NSAIDs)";
//        stArr[1] = "HighBP(x) => Alert(x,NSAIDs)";
//        stArr[2] = "Take(Bob,Antacids)";
//        stArr[3] = "Take(Bob,VitA)";
//        stArr[4] = "HighBP(Bob)";
//
//        FormKB kb = new FormKB();
//        kb.formKB(stArr);
//    }
}


