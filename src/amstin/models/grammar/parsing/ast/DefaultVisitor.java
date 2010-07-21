package amstin.models.grammar.parsing.ast;

public class DefaultVisitor implements Visitor {
	
	@Override
	public void visit(Instance obj) {
		for (Object arg: obj.getArgs()) {
			visitObject(arg);
		}
	}


	@Override
	public void visit(Field labeled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Reference reffed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Define keyed) {
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("unchecked")
	public void visitObject(Object arg) {
		if (arg instanceof Visitable) {
			((Visitable)arg).accept(this);
		}
		if (arg instanceof Iterable) {
			for (Object o: (Iterable)arg) {
				visitObject(o);
			}
		}
	}


}
