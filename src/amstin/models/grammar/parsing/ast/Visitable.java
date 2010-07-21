package amstin.models.grammar.parsing.ast;

public interface Visitable {
	void accept(Visitor visitor);
}
