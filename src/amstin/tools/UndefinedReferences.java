package amstin.tools;

import java.util.HashSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import amstin.parsing.ast.DefaultVisitor;
import amstin.parsing.ast.Define;
import amstin.parsing.ast.Instance;
import amstin.parsing.ast.Reference;
import amstin.parsing.ast.Symbol;


public class UndefinedReferences extends DefaultVisitor {
	private Set<Reference> undefined;
	private Map<Symbol, Instance> defined;

	public static Set<Reference> undefinedReferences(Object obj) {
		Map<Symbol, Instance> defs = collectDefines(obj);
		UndefinedReferences uses = new UndefinedReferences(defs);
		uses.visitObject(obj);
		return uses.undefined;
	}

	private UndefinedReferences(Map<Symbol, Instance> defined) {
		this.defined = defined;
		this.undefined = new HashSet<Reference>();
	}


	@Override
	public void visit(Reference reffed) {
		Symbol key = reffed.getSymbol();
		if (!defined.containsKey(key)) {
			undefined.add(reffed);
		}
		else {
			String defType = defined.get(key).getType();
			String refType = reffed.getType();
			if (!refType.equals(defType)) {
				System.err.println("Warning: reference " + reffed + " has type " + refType
						+ ", but should be " + defType + ".");
			}
		}
	}

	public static Map<Symbol, Instance> collectDefines(Object obj) {
		CollectDefines v = new CollectDefines();
		v.visitObject(obj);
		return v.defined;
	}
	
	
	private static class CollectDefines extends DefaultVisitor {

		private Map<Symbol, Instance> defined;
		private Stack<Instance> stack;

		
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

}
