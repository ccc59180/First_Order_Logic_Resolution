import java.util.*;

public class Resolution {
    private Set<String> classSet ;
    private List<String> claList;
    private Map<String,List<String>> predMap;

    public Resolution(Set<String> classSet, Map<String,List<String>> predMap) {
        this.classSet = classSet;
        this.predMap = predMap;
        claList =  new ArrayList<>(classSet);
    }

    public boolean resolve(String query){
        String negatedQue = neQuery(query);
        classSet.add(negatedQue);
        claList.add(0, negatedQue);
        int sizeDifference = -1;
        while (sizeDifference != 0){
            int size = claList.size();
            for (int i = 0; i < claList.size(); i++){
                if (claList.size() > 50000) return false;
                String clause = claList.get(i);
                boolean te = unify(clause);
//                System.out.println(clause);
                if(te) return true;
            }
            sizeDifference = classSet.size() - size;
        }
        return false;
    }

    private boolean isVar(String[] a, String[] b) {
        for (int i = 0; i < a.length; i++) {
            if (!isConstant(a[i]) && !isConstant(b[i])) continue;
            else return false;
        }
        return true;
    }


    private boolean unify(String aClause) {
        String[] aTokens = aClause.split("\\|");
        if(aTokens.length == 1 && classSet.contains(neQuery(aTokens[0]))) return true;
        for (int i1 = 0; i1 < aTokens.length; i1++) {
            String aToken1 = aTokens[i1];
            String aToken = neQuery(aToken1);
            String[] aArguments = getAllArguments(aToken);
            String aPre = getPredicate(aToken);
            List<String> clauseList = predMap.get(aPre);
            boolean aBelongsToList = false;
            if (clauseList != null) {
                for (int i = 0; i < clauseList.size(); i++) {
                    String aClauseList = clauseList.get(i);
                    if (aClause.equals(aClauseList)) {
                        aBelongsToList = true;
                        break;
                    }
                }
            }
            if (aBelongsToList) continue;
            if (clauseList != null) {
//                System.out.println(clauseList);
                for (int i = 0; i < clauseList.size(); i++) {
                    String bclause = clauseList.get(i);
                    HashMap<String, String> arguMap = null;
                    String[] bTokens = bclause.split("\\|");
                    String bToken = null;
                    boolean couldResolve = false;
                    boolean wouldResolve = false;

                    for (int i2 = 0; i2 < bTokens.length; i2++) {
                        String bToken1 = bTokens[i2];
                        bToken = bToken1;
                        String[] bArguments;
                        String bPredicate = getPredicate(bToken);

                        if (bPredicate.equals(aPre)) {
                            arguMap = new HashMap<>();
                            bArguments = getAllArguments(bToken);
                            for (int k = 0; k < bArguments.length; k++) {
                                if (bArguments[k].equals(aArguments[k])) {
                                    couldResolve = isConstant(bArguments[k]);
                                    arguMap.put(bArguments[k], bArguments[k]);
                                } else {
                                    if (isConstant(aArguments[k]) && isConstant(bArguments[k])) {
                                        wouldResolve = false;
                                        couldResolve = false;
                                        break;
                                    } else if (isConstant(aArguments[k])) {
                                        if (arguMap.containsKey(bArguments[k])) {
                                            wouldResolve = false;
                                            break;
                                        }
                                        wouldResolve = true;
                                        arguMap.put(bArguments[k], aArguments[k]);
                                    } else if (isConstant(bArguments[k])) {
                                        if (arguMap.containsKey(aArguments[k])) {
                                            wouldResolve = false;
                                            break;
                                        }
                                        wouldResolve = true;
                                        arguMap.put(aArguments[k], bArguments[k]);
                                    } else {
                                        arguMap.put(aArguments[k], bArguments[k]);
                                        if (bTokens.length == 1 || aTokens.length == 1) wouldResolve = true;
                                    }
                                }
                            }
                            if (wouldResolve) {
                                break;
                            } else if (couldResolve && aTokens.length == 1) {
                                wouldResolve = true;
                                break;
                            }
                        }
                    }

                    if (wouldResolve) {
                        String[] newForATokens = aClause.split("\\|");
                        String[] newForBTokens = bclause.split("\\|");
                        String newForA = "";
                        for (int p = 0; p < newForATokens.length; p++) {
                            String newForThisToken = newForATokens[p];
                            if (!newForThisToken.equals(neQuery(aToken))) {
                                String[] newForArgs = getAllArguments(newForThisToken);
                                newForThisToken = getPredicate(newForThisToken) + "(";
                                for (int n = 0; n < newForArgs.length; n++) {
                                    newForThisToken += (arguMap.get(newForArgs[n]) != null ? arguMap.get(newForArgs[n]) : newForArgs[n]);
                                    if (n != newForArgs.length - 1) {
                                        newForThisToken = newForThisToken + ",";
                                    }
                                }
                                newForA = newForA + newForThisToken + ")";
                                if (p != newForATokens.length - 1) {
                                    newForA = newForA + "|";
                                }
                            }
                        }
                        String newForB = "";
                        for (int p = 0; p < newForBTokens.length; p++) {
                            String newForThisToken = newForBTokens[p];
                            if (!newForThisToken.equals(bToken)) {
                                String[] newForArgs = getAllArguments(newForThisToken);
                                newForThisToken = getPredicate(newForThisToken) + "(";
                                for (int n = 0; n < newForArgs.length; n++) {
                                    newForThisToken += (arguMap.get(newForArgs[n]) != null ? arguMap.get(newForArgs[n]) : newForArgs[n]);
                                    if (n != newForArgs.length - 1) {
                                        newForThisToken = newForThisToken + ",";
                                    }
                                }
                                newForB = newForB + newForThisToken + ")";
                                if (p != newForBTokens.length - 1) {
                                    newForB = newForB + "|";
                                }
                            }
                        }

                        StringBuilder newString;
                        if (newForA.isEmpty() && newForB.isEmpty()) {
                            return true;
                        } else if (newForA.isEmpty()) {
                            if (newForB.charAt(newForB.length() - 1) == '|') {
                                newForB = newForB.substring(0, newForB.length() - 1);
                            }
                            newString = new StringBuilder(newForB);
                        } else if (newForB.isEmpty()) {
                            if (newForA.charAt(newForA.length() - 1) == '|') {
                                newForA = newForA.substring(0, newForA.length() - 1);
                            }
                            newString = new StringBuilder(newForA);
                        } else {
                            if (newForA.charAt(newForA.length() - 1) == '|') {
                                newForA = newForA.substring(0, newForA.length() - 1);
                            }
                            if (newForB.charAt(newForB.length() - 1) == '|') {
                                newForB = newForB.substring(0, newForB.length() - 1);
                            }
                            newString = new StringBuilder(newForA + "|" + newForB);
                        }

                        String[] arguments = newString.toString().split("\\|");
                        HashMap<String, Boolean> argumentsMap = new HashMap<>();
                        for (int i2 = 0; i2 < arguments.length; i2++) {
                            String arg = arguments[i2];
                            argumentsMap.put(arg, true);
                        }
                        newString = new StringBuilder();
                        Set<String> keys = argumentsMap.keySet();
                        if (!keys.isEmpty()) {
                            for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
                                String key = iterator.next();
                                newString.append(key).append("|");
                            }
                            newString = new StringBuilder(newString.substring(0, newString.length() - 1));
                            if (newString.length() == 0) {
                                return true;
                            }
                        }
                        if ((newString.length() == 0) || newString.toString().equals("")) {
                            continue;
                        }
                        if (classSet.add(newString.toString())) {
                            String[] tokens = newString.toString().split("\\|");
                            if (tokens.length == 1 && classSet.contains(neQuery(newString.toString()))) return true;
                            claList.add(newString.toString());
                            String[] tokenArray = newString.toString().split("\\|");
                            for (String aTokenArray : tokenArray) {
                                predMap.computeIfAbsent(getPredicate(aTokenArray), k -> new ArrayList<>()).add(newString.toString());
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isConstant(String str) {
        return Character.isUpperCase(str.charAt(0));
    }

    private String[] getAllArguments(String token) {
        return token.split("\\(")[1].split("\\)")[0].split(",");
    }

    private static String getPredicate(String query){
        return query.split("\\(")[0];
    }

    private static String neQuery(String predicate){
        if (predicate.charAt(0) == '~') return predicate.substring(1);
        return "~" + predicate;
    }
}
