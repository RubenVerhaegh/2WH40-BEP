public class Literal {
    private String  id;
    private boolean requiredTruthAssignment;

    public Literal(String id, boolean requiredTruthAssignment) {
        this.id = id;
        this.requiredTruthAssignment = requiredTruthAssignment;
    }

    public String getId() {
        return id;
    }

    public boolean getRequiredTruthAssignment() {
        return requiredTruthAssignment;
    }
}
