package amstin.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import amstin.parsing.ast.DefaultVisitor;
import amstin.parsing.ast.Define;
import amstin.parsing.ast.Instance;
import amstin.parsing.ast.Symbol;


public class CollectDefines extends DefaultVisitor {

	private Map<Symbol, Instance> defined;
	private Stack<Instance> stack;

	public static Map<Symbol, Instance> collectDefines(Object obj) {
		CollectDefines v = new CollectDefines();
		v.visitObject(obj);
		return v.defined;
	}
	
	private CollectDefines() {
		this.defined = new HashMap<Symbol, Instance>();
		this.stack = new Stack<Instance>();
	}
	
	@Override
	public void visit(Instance obj) {
		stack.push(obj);
		for (Object arg: obj.getArgs()) {
			visitObject(arg);
		}
		stack.pop();
	}

	@Override
	public void visit(Define keyed) {
		Symbol sym = keyed.getSymbol();
		if (defined.containsKey(sym)) {
			throw new RuntimeException("duplicate key " + sym);
		}
		defined.put(sym, stack.peek());
	}

}
