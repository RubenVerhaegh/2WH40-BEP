import java.util.ArrayList;
import java.util.Set;

public class Clause {
    private Set<Literal> literals;

    public Clause(Set<Literal> literals) {
        this.literals = literals;
    }

    public Set<Literal> getLiterals() {
        return literals;
    }

    public int getNrOfLiterals() {
        return literals.size();
    }
}
