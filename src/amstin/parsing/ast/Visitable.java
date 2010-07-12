package amstin.parsing.ast;

public interface Visitable {
	void accept(Visitor visitor);
}
