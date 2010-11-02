package amstin.models.format.tobox;

import java.util.ArrayList;

import amstin.models.ast.AST;
import amstin.models.ast.Arg;
import amstin.models.ast.Bool;
import amstin.models.ast.Cons;
import amstin.models.ast.Id;
import amstin.models.ast.Int;
import amstin.models.ast.Key;
import amstin.models.ast.List;
import amstin.models.ast.Nil;
import amstin.models.ast.Real;
import amstin.models.ast.Ref;
import amstin.models.ast.Str;
import amstin.models.format.Align;
import amstin.models.format.Alt;
import amstin.models.format.Box;
import amstin.models.format.Cond;
import amstin.models.format.Format;
import amstin.models.format.Group;
import amstin.models.format.Horizontal;
import amstin.models.format.Indented;
import amstin.models.format.KeyBox;
import amstin.models.format.LitBox;
import amstin.models.format.NumBox;
import amstin.models.format.RefBox;
import amstin.models.format.Row;
import amstin.models.format.Rule;
import amstin.models.format.SepList;
import amstin.models.format.StrBox;
import amstin.models.format.Text;
import amstin.models.format.Var;
import amstin.models.format.VarBox;
import amstin.models.format.Vertical;

public class ASTtoBox {
	public static Box astToBox(Format format, AST ast) {
		return new ASTtoBox(format, ast).toBox();
	}

	private AST root;
	private Format format;

	public ASTtoBox(Format format, AST ast) {
		this.format = format;
		this.root = ast;
	}

	private Box toBox() {
		return toBox(root);
	}

	private void toBox(AST ast, java.util.List<Box> output) {
		Box box = toBox(ast);
		if (box != null) {
			output.add(box);
		}
	}
	
	private Box toBox(AST ast) {
		if (ast instanceof Cons) {
			Cons cons = (Cons)ast;
			Box box = lookupBox(cons.name);
			return eval(box, cons.args);
		}
		if (ast instanceof List) {
			throw new AssertionError("lists are handled when procesing cons args");
		}
		if (ast instanceof Int) {
			return text(((Int)ast).value.toString());
		}
		if (ast instanceof Id) {
			return text(((Id)ast).value.toString());
		}
		if (ast instanceof Real) {
			return text(((Real)ast).value.toString());
		}
		if (ast instanceof Str) {
			// TODO: escaping
			return text("\"" + ((Str)ast).value.toString() + "\"");
		}
		if (ast instanceof Ref) {
			return text(((Ref)ast).value.toString());
		}
		if (ast instanceof Key) {
			return text(((Key)ast).value.toString());
		}
		if (ast instanceof Bool) {
			return text(((Bool)ast).value.toString());
		}
		if (ast instanceof Nil) {
			return null;
		}
		throw new RuntimeException("unsupported AST type: " + ast.getClass());
	}

	private Box eval(Box box, java.util.List<Arg> args) {
		java.util.List<Box> output = new ArrayList<Box>();
		eval(box, args, output);
		assert output.size() == 1;
		return output.get(0);
	}

	private void eval(Box box, java.util.List<Arg> args, java.util.List<Box> output) {
		if (box instanceof Var) {
			evalVar((Var)box, args, output);
		}
		else if (box instanceof Cond) {
			evalCond((Cond)box, args, output);
		}
		else if (box instanceof Text) {
			output.add(box);
		}
		else {
			evalComposite(box, args, output);
		}
	}

	private void evalComposite(Box box, java.util.List<Arg> args, java.util.List<Box> output) {
		// TODO: use a reflective trick here to avoid this duplication
		if (box instanceof Vertical) {
			Vertical v1 = (Vertical)box;
			Vertical v2 = new Vertical();
			v2.options = v1.options;
			v2.kids = new ArrayList<Box>();
			evalKids(v1.kids, args, v2.kids);
			output.add(v2);
		} 
		else if (box instanceof Horizontal) {
			Horizontal h1 = (Horizontal)box;
			Horizontal h2 = new Horizontal();
			h2.options = h1.options;
			h2.kids = new ArrayList<Box>();
			evalKids(h1.kids, args, h2.kids);
			output.add(h2);
		} 
		else if (box instanceof Indented) {
			Indented i1 = (Indented)box;
			Indented i2 = new Indented();
			i2.options = i1.options;
			i2.kids = new ArrayList<Box>();
			evalKids(i1.kids, args, i2.kids);
			output.add(i2);
		} 
		else if (box instanceof Group) {
			Group g1 = (Group)box;
			Group g2 = new Group();
			g2.options = g1.options;
			g2.kids = new ArrayList<Box>();
			evalKids(g1.kids, args, g2.kids);
			output.add(g2);
		} 
		else if (box instanceof Align) {
			Align a1 = (Align)box;
			Align a2 = new Align();
			a2.options = a1.options;
			a2.kids = new ArrayList<Box>();
			evalKids(a1.kids, args, a2.kids);
			output.add(a2);
		} 
		else if (box instanceof Row) {
			Row r1 = (Row)box;
			Row r2 = new Row();
			r2.options = r1.options;
			r2.kids = new ArrayList<Box>();
			evalKids(r1.kids, args, r2.kids);
			output.add(r2);
		} 
		else if (box instanceof SepList) {
			SepList s1 = (SepList)box;
			SepList s2 = new SepList();
			s2.options = s1.options;
			s2.kids = new ArrayList<Box>();
			evalKids(s1.kids, args, s2.kids);
			output.add(s2);
		} 
		else {
			throw new RuntimeException("unsupported box expression " + box.getClass());
		}
	}
	
	private void evalCond(Cond cond, java.util.List<Arg> args, java.util.List<Box> output) {
		for (Arg arg: args) {
			if (arg.name.equals(cond.var)) {
				AST ast = arg.ast;
				if (ast == null) {
					return;
				}
				if (ast instanceof Bool && !((Bool)ast).value) {
					return;
				}
				output.add(cond.arg);
				return;
			}
		}
		throw new RuntimeException("undefined label " + cond.var);
	}

	private void evalVar(Var var, java.util.List<Arg> args, java.util.List<Box> output) {
		for (Arg arg: args) {
			if (arg.name.equals(var.name)) {
				AST ast = arg.ast;
				if (ast instanceof List) {
					for (AST k: ((List)ast).elements) {
						toBox(k, output);
					}
				}
				else {
					toBox(ast, output);
				}
				return;
			}
		}
		throw new RuntimeException("undefined label " + var.name);
	}

	private void evalKids(java.util.List<Box> kids, java.util.List<Arg> args, java.util.List<Box> output) {
		for (int i = 0; i < kids.size(); i++) {
			eval(kids.get(i), args, output);
		}
	}

	private Box lookupBox(String name) {
		for (Rule rule: format.rules) {
			// Here we assume (like everywhere) that
			// NT names and production names are disjoint
			if (name.equals(rule.name)) {
				// return the first alternative
				Alt alt = rule.alts.get(0);
				assert alt.label == null;
				return alt.box;
			}
			for (Alt alt: rule.alts) {
				if (alt.label != null && name.equals(alt.label.name)) {
					return alt.box;
				}
			}
		}
		throw new RuntimeException("Could not find formatting expression for " + name);
	}

	private Box text(String string) {
		Text box = new Text();
		box.value = string;
		return box;
	}

	
	
}
