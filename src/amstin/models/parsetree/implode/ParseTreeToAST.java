package amstin.models.parsetree.implode;

import java.util.ArrayList;

import amstin.models.ast.AST;
import amstin.models.ast.Cons;
import amstin.models.ast.Nil;
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

public class ParseTreeToAST {

	public static AST parseTreeToAST(ParseTree pt) {
		return new ParseTreeToAST(pt).implode();
	}

	private ParseTree pt;

	private ParseTreeToAST(ParseTree pt) {
		this.pt = pt;
	}

	private AST implode() {
		return implodeTree(((Arg)pt.top).value);
	}

	private AST implodeTree(Tree tree) {
		if (tree == null) {
			return new Nil();
		}
		if (tree instanceof Obj) {
			Cons ast = new Cons();
			Obj obj = (Obj)tree;
			ast.name = obj.name;
			ast.args = new ArrayList<amstin.models.ast.Arg>();
			for (Tree kid: obj.args) {
				if (!(kid instanceof Arg)) {
					continue;
				}
				amstin.models.ast.Arg arg = new amstin.models.ast.Arg();
				arg.name = ((Arg)kid).name;
				arg.ast = implodeTree(((Arg)kid).value);
				ast.args.add(arg);
			}
			return ast;
		}
		if (tree instanceof List) {
			amstin.models.ast.List ast = new amstin.models.ast.List();
			List list = (List)tree;
			ast.elements = new ArrayList<AST>();
			for (Tree kid: list.elements) {
				if (kid instanceof Ws || kid instanceof Lit || kid instanceof Comment) {
					continue;
				}
				ast.elements.add(implodeTree(kid));
			}
			return ast;
		}
		if (tree instanceof Id) {
			amstin.models.ast.Id ast = new amstin.models.ast.Id();
			ast.value = ((Id)tree).value;
			return ast;
		}
		if (tree instanceof Str) {
			amstin.models.ast.Str ast = new amstin.models.ast.Str();
			ast.value = ((Str)tree).value;
			return ast;
		}
		if (tree instanceof Int) {
			amstin.models.ast.Int ast = new amstin.models.ast.Int();
			ast.value = ((Int)tree).value;
			return ast;
		}
		if (tree instanceof Real) {
			amstin.models.ast.Real ast = new amstin.models.ast.Real();
			ast.value = ((Real)tree).value;
			return ast;
		}
		if (tree instanceof Ref) {
			amstin.models.ast.Ref ast = new amstin.models.ast.Ref();
			ast.value = ((Ref)tree).name;
			return ast;
		}
		if (tree instanceof Bool) {
			amstin.models.ast.Bool ast = new amstin.models.ast.Bool();
			ast.value = ((Bool)tree).value;
			return ast;
		}
		// TODO: rename Def to key
		if (tree instanceof Def) {
			amstin.models.ast.Key ast = new amstin.models.ast.Key();
			ast.value = ((Def)tree).name;
			return ast;
		}
		throw new RuntimeException("unsupported tree type: " + tree.getClass());
	}

	
	
}
