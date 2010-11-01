package amstin.tools;

import amstin.models.parsetree.Arg;
import amstin.models.parsetree.Bool;
import amstin.models.parsetree.Comment;
import amstin.models.parsetree.Def;
import amstin.models.parsetree.Id;
import amstin.models.parsetree.Int;
import amstin.models.parsetree.List;
import amstin.models.parsetree.Lit;
import amstin.models.parsetree.Obj;
import amstin.models.parsetree.ParseTree;
import amstin.models.parsetree.Real;
import amstin.models.parsetree.Ref;
import amstin.models.parsetree.Str;
import amstin.models.parsetree.Tree;
import amstin.models.parsetree.Ws;

public class ParseTreeToString {

	
	public static String parseTreeToString(ParseTree pt) {
		String s = treeToString(pt.preLayout);
		s += treeToString(pt.top);
		return s + treeToString(pt.postLayout);
	}
	
	private static String treeToString(Tree tree) {
		if (tree == null) {
			return "";
		}
		if (tree instanceof Obj) {
			String s = "";
			for (Tree t: ((Obj)tree).args) {
				s += treeToString(t);
			}
			return s;
		}
		if (tree instanceof Arg) {
			return treeToString(((Arg)tree).value);
		}
		if (tree instanceof Str) {
			// TODO: escaping
			return "\"" + ((Str)tree).value + "\"";
		}
		if (tree instanceof Int) {
			return ((Int)tree).value.toString();
		}
		if (tree instanceof Bool) {
			return ((Bool)tree).value.toString();
		}
		if (tree instanceof Real) {
			return ((Real)tree).value.toString();
		}
		if (tree instanceof Id) {
			return ((Id)tree).value;
		}
		if (tree instanceof Ref) {
			return ((Ref)tree).name;
		}
		if (tree instanceof Def) {
			return ((Def)tree).name;
		}
		if (tree instanceof Ws) {
			return ((Ws)tree).value;
		}
		if (tree instanceof Lit) {
			return ((Lit)tree).value;
		}
		if (tree instanceof Comment) {
			return ((Comment)tree).value;
		}
		if (tree instanceof List) {
			String s = "";
			for (Tree t: ((List)tree).elements) {
				s += treeToString(t);
			}
			return s;
		}
		throw new AssertionError("invalid object in ast to model: " + tree);
	}

	
}
