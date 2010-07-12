package amstin.tools;

import java.util.HashSet;

import java.util.Map;
import java.util.Set;

import amstin.parsing.ast.DefaultVisitor;
import amstin.parsing.ast.Instance;
import amstin.parsing.ast.Reference;
import amstin.parsing.ast.Symbol;


public class UndefinedReferences extends DefaultVisitor {
	private Set<Reference> undefined;
	private Map<Symbol, Instance> defined;

	public static Set<Reference> undefinedReferences(Object obj) {
		Map<Symbol, Instance> defs = CollectDefines.collectDefines(obj);
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

}
