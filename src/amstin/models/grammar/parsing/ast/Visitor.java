package amstin.models.grammar.parsing.ast;

public interface Visitor {

	public void visit(Instance obj);

	public void visit(Field labeled);

	public void visit(Reference reffed);

	public void visit(Define keyed);
	
}
