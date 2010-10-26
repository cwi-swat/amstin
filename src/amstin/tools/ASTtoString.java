package amstin.tools;

import amstin.models.ast.Arg;
import amstin.models.ast.Bool;
import amstin.models.ast.Comment;
import amstin.models.ast.Def;
import amstin.models.ast.Id;
import amstin.models.ast.Int;
import amstin.models.ast.List;
import amstin.models.ast.Lit;
import amstin.models.ast.Obj;
import amstin.models.ast.ParseTree;
import amstin.models.ast.Real;
import amstin.models.ast.Ref;
import amstin.models.ast.Str;
import amstin.models.ast.Tree;
import amstin.models.ast.Ws;

public class ASTtoString {

	public static void main(String[] args) {
		
	}
	
	
	public static String astToString(ParseTree pt) {
		String s = astToString(pt.preLayout);
		s += astToString(pt.top);
		return s + astToString(pt.postLayout);
	}
	
	public static String astToString(Tree ast) {
		if (ast == null) {
			return "";
		}
		if (ast instanceof Obj) {
			String s = "";
			for (Tree t: ((Obj)ast).args) {
				s += astToString(t);
			}
			return s;
		}
		if (ast instanceof Arg) {
			return astToString(((Arg)ast).value);
		}
		if (ast instanceof Str) {
			// TODO: escaping
			return "\"" + ((Str)ast).value + "\"";
		}
		if (ast instanceof Int) {
			return ((Int)ast).value.toString();
		}
		if (ast instanceof Bool) {
			return ((Bool)ast).value.toString();
		}
		if (ast instanceof Real) {
			return ((Real)ast).value.toString();
		}
		if (ast instanceof Id) {
			return ((Id)ast).value;
		}
		if (ast instanceof Ref) {
			return ((Ref)ast).name;
		}
		if (ast instanceof Def) {
			return ((Def)ast).name;
		}
		if (ast instanceof Ws) {
			return ((Ws)ast).value;
		}
		if (ast instanceof Lit) {
			return ((Lit)ast).value;
		}
		if (ast instanceof Comment) {
			return ((Comment)ast).value;
		}
		if (ast instanceof List) {
			String s = "";
			for (Tree t: ((List)ast).elements) {
				s += astToString(t);
			}
			return s;
		}
		throw new AssertionError("invalid object in ast to model: " + ast);
	}

	
}
