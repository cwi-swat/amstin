package amstin.tools;

import amstin.models.ast.Arg;
import amstin.models.ast.Comment;
import amstin.models.ast.Def;
import amstin.models.ast.False;
import amstin.models.ast.Id;
import amstin.models.ast.Int;
import amstin.models.ast.List;
import amstin.models.ast.Lit;
import amstin.models.ast.Obj;
import amstin.models.ast.Real;
import amstin.models.ast.Ref;
import amstin.models.ast.Str;
import amstin.models.ast.Tree;
import amstin.models.ast.True;
import amstin.models.ast.Ws;

public class ASTtoString {

	public static void main(String[] args) {
		
	}
	
	
	public static String astToString(Tree obj) {
		if (obj == null) {
			return "";
		}
		if (obj instanceof Obj) {
			String s = "";
			for (Tree t: ((Obj)obj).args) {
				s += astToString(t);
			}
			return s;
		}
		if (obj instanceof Arg) {
			return astToString(((Arg)obj).value);
		}
		if (obj instanceof Str) {
			// TODO: escaping
			return "\"" + ((Str)obj).value + "\"";
		}
		if (obj instanceof Int) {
			return ((Int)obj).value.toString();
		}
		if (obj instanceof Real) {
			return ((Real)obj).value.toString();
		}
		if (obj instanceof Id) {
			return ((Id)obj).value;
		}
		if (obj instanceof True) {
			return "true";
		}
		if (obj instanceof False) {
			return "false";
		}
		if (obj instanceof Ref) {
			return ((Ref)obj).name;
		}
		if (obj instanceof Def) {
			return ((Def)obj).name;
		}
		if (obj instanceof Ws) {
			return ((Ws)obj).value;
		}
		if (obj instanceof Lit) {
			return ((Lit)obj).value;
		}
		if (obj instanceof Comment) {
			return ((Comment)obj).value;
		}
		if (obj instanceof List) {
			String s = "";
			for (Tree t: ((List)obj).elements) {
				s += astToString(t);
			}
			return s;
		}
		throw new AssertionError("invalid object in ast to model: " + obj);
	}

	private void objToString(Obj obj) {
		// TODO Auto-generated method stub
		
	}
	
	
}
