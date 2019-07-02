import java.util.*;

public class DNFFormula {
    private Set<Clause> clauses;
    private Set<String> variables;

    public DNFFormula (Set<Clause> clauses) {
        this.clauses = clauses;
        this.variables = new HashSet<>();

        for (Clause clause : clauses) {
            for (Literal literal : clause.getLiterals()) {
                this.variables.add(literal.getId());
            }
        }
    }

    public DNFFormula (Set<Clause> clauses, Set<String> variables) {
        this.clauses = clauses;
        this.variables = variables;
    }

    private Clause pickRandomClause() {
        RandomCollection<Clause> clauseCollection = new RandomCollection<>();
        for (Clause clause : clauses) {
            clauseCollection.add(clause, Math.pow(2, variables.size() - clause.getNrOfLiterals()));
        }

        return clauseCollection.next();
    }

    private Map<String, Boolean> findRandomTruthAssignmentSatisfyingClause(Clause clause) {
        Map<String, Boolean> truthAssignment = new HashMap<>();

        Set<Literal> literalsInClause = clause.getLiterals();
        Random random = new Random();

        for (String variable : variables) {
            // If this variable is not in the clause, give it a random truth assignment.
            truthAssignment.put(variable, random.nextBoolean());

            // If it is in the clause, give it the truth assignment needed to satisfy the clause.
            for (Literal literal : literalsInClause) {
                if (variable.equals(literal.getId())) {
                    truthAssignment.put(variable, literal.getRequiredTruthAssignment());
                    break;
                }
            }


        }

        return truthAssignment;
    }

    private boolean checkTruthAssignmentForClause(Map<String, Boolean> truthAssignment, Clause clause) {
        if (truthAssignment.size() != variables.size()) {
            throw new IllegalArgumentException("Truth assignment has " + truthAssignment.size() + ". " +
                    "Expected: " + variables.size() + ".");
        }

        for (Literal literal : clause.getLiterals()) {
            if (literal.getRequiredTruthAssignment() != truthAssignment.get( literal.getId() )) {
                return false;
            }
        }

        return true;
    }

    private int countSatisfyingClausesForTruthAssignment(Map<String, Boolean> truthAssignment) {
        int count = 0;

        for (Clause clause : clauses) {
            if (checkTruthAssignmentForClause(truthAssignment, clause)) {
                count++;
            }
        }

        return count;
    }

    private double sampleXOnce() {
        Clause randomClause = pickRandomClause();

        Map<String, Boolean> truthAssignment = findRandomTruthAssignmentSatisfyingClause(randomClause);

        int numberOfSatisfyingClauses = countSatisfyingClausesForTruthAssignment(truthAssignment);

        int sizeOfM = 0;
        for (Clause clause : clauses) {
            sizeOfM += Math.pow(2, variables.size() - clause.getNrOfLiterals());
        }

        return sizeOfM / (double) numberOfSatisfyingClauses;
    }

    private double averageXOverMultipleSamples(int samples) {
        double sum = 0;

        for (int i = 0; i < samples; i++) {
            /*if (i * 10 / samples > (i - 1) * 10 / samples) {
                System.out.println(i * 100 / samples + "%");
            }*/
            double value = sampleXOnce();
            sum += value;
        }

        return sum / samples;
    }

    public double approximateNumberOfSatisfyingTruthAssignments(double epsilon) {
        int iterations = (int) (4 * Math.pow(clauses.size() - 1, 2) / Math.pow(epsilon, 2));
        //System.out.println(iterations + " iterations");
        return averageXOverMultipleSamples(iterations);
    }

    public int countNumberOfSatisfyingTruthAssignments() {
        ArrayList<Boolean> emptyAssignment = new ArrayList<>();

        return countNumberOfSatisfyingTruthAssignments(emptyAssignment);
    }

    private int countNumberOfSatisfyingTruthAssignments(ArrayList<Boolean> assignmentSoFar) {
        if (assignmentSoFar.size() == variables.size()) {
            Map<String, Boolean> truthAssignment = booleanArrayToTruthAssignment(assignmentSoFar);

            if (doesTruthAssignmentSatisfyFormula(truthAssignment)) {
                return 1;
            } else {
                return 0;
            }
        }

        assignmentSoFar.add(true);
        int sum = countNumberOfSatisfyingTruthAssignments(assignmentSoFar);
        assignmentSoFar.set(assignmentSoFar.size() - 1, false);
        sum += countNumberOfSatisfyingTruthAssignments(assignmentSoFar);
        assignmentSoFar.remove(assignmentSoFar.size() - 1);

        return sum;
    }

    private boolean doesTruthAssignmentSatisfyFormula(Map<String, Boolean> truthAssignment) {
        for (Clause clause : clauses) {
            if (checkTruthAssignmentForClause(truthAssignment, clause)) {
                return true;
            }
        }

        return false;
    }

    private String toString(Clause clause) {
        String string = "";
        for (Literal literal : clause.getLiterals()) {
            string += literal.getId() + ":" + literal.getRequiredTruthAssignment() + ", ";
        }
        return string;
    }

    private Map<String, Boolean> booleanArrayToTruthAssignment(ArrayList<Boolean> array) {
        if (array.size() != variables.size()) {
            throw new IllegalArgumentException("booleanArrayToTruthAssignment() called with an array size of " +
                    array.size() + ". That is not equal to the number of variables of this formula, which is " +
                    variables.size() + ".");
        }

        ArrayList<String> orderedVariables = new ArrayList<>(variables);
        Map<String, Boolean> truthAssignment = new HashMap<>();
        for (int i = 0; i < array.size(); i++) {
            truthAssignment.put(orderedVariables.get(i), array.get(i));
        }

        return truthAssignment;
    }

    public Set<Clause> getClauses() {
        return clauses;
    }

    public Set<String> getVariables() {
        return variables;
    }
}
