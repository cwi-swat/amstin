package amstin.models.grammar.parsing.oogll;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import amstin.models.grammar.parsing.oogll.forest.Flattener;
import amstin.models.grammar.parsing.oogll.sppf.Node;
import amstin.models.grammar.parsing.oogll.symbol.NonTerminal;
import amstin.models.grammar.parsing.oogll.symbol.RegExp;
import amstin.models.grammar.parsing.oogll.symbol.Symbol;
import amstin.models.grammar.Alt;
import amstin.models.grammar.Bool;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.Id;
import amstin.models.grammar.Int;
import amstin.models.grammar.Iter;
import amstin.models.grammar.IterSep;
import amstin.models.grammar.IterSepStar;
import amstin.models.grammar.IterStar;
import amstin.models.grammar.Key;
import amstin.models.grammar.Lit;
import amstin.models.grammar.Opt;
import amstin.models.grammar.Real;
import amstin.models.grammar.Ref;
import amstin.models.grammar.Rule;
import amstin.models.grammar.Str;
import amstin.models.grammar.Sym;

public class MDGParser {

	private static final String ID_REGEX = "[\\\\]?[a-zA-Z_$][a-zA-Z_$0-9]*";
	private static final String REF_REGEX = "(" + ID_REGEX + ")" + "(\\."  + ID_REGEX + ")*";

	private static Token REF = new Token("ref", REF_REGEX);
	private static Token KEY = new Token("key", ID_REGEX);
	private static Token REAL = new Token("real", "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
	private static Token INT = new Token("int", "[-+]?[0-9]+");
	private static Token STR = new Token("str", "\"(\\\\.|[^\"])*\"");
	private static Token BOOL = new Token("bool", "true|false");
	private static Token LAYOUT = new Token("layout", "([\\t\\n\\r\\f ]*(//[^\\n]*\\n)?)*");

	private final Token ID;
	
	private static class Token extends RegExp {

		private final String name;

		public Token(String name, String regex) {
			super(regex);
			this.name = name;
		}
		
		@Override
		public String toString() {
			return "<" + name + ">";
		}
		
	}
	
	private static class IdToken extends Token {

		private final Set<String> reserved;

		public IdToken(Set<String> reserved) {
			super("id", ID_REGEX);
			this.reserved = reserved;
		}

		@Override
		protected String result(Matcher m) {
			if (!reserved.contains(m.group())) {
				return super.result(m);
			}
			return null;
		}
		
	}
	
	private static class LitToken extends RegExp {

		private final String lit;

		public LitToken(String lit) {
			super(Pattern.quote(lit));
			this.lit = lit;
			if (lit.matches(ID_REGEX)) {
				setPattern(Pattern.compile(getPattern() + "(?![a-zA-Z0-9_$])"));
			}
		}	
		
		@Override
		public String toString() {
			return "'" + lit + "'";
		}
	}
 	
	public static void main(String[] args) throws InterruptedException {
//		Grammar g = amstin.models.grammar.parsing.cps.Parser.parseGrammar(_Main.GRAMMAR_MDG);
		Grammar g = amstin.models.grammar.parsing.cps.Parser.parseGrammar("src/amstin/models/grammar/parsing/gll/test.mdg");
//		String grammarSrc = amstin.models.grammar.parsing.cps.Parser.readPath("src/amstin/models/grammar/parsing/gll/test.mdg");
		String src = " ac ";
		
		MDGParser p = new MDGParser(g);
		
		Node tree = p.parse(src);
		Test.writeDot("test.dot", Flattener.flatten(tree).toDot());
		System.out.println(tree.toDot());
	}
	
	

	private final Map<Rule, NonTerminal> rules = new HashMap<Rule, NonTerminal>();
	private final GLL gll;
	
	
	public MDGParser(Grammar grammar) {
		ID = new IdToken(grammar.reservedKeywords());
		
		for (Rule rule: grammar.rules) {
			rules.put(rule, new NonTerminal(rule.name));
		}
		for (Rule rule: grammar.rules) {
			convertRule(rule, rules.get(rule));
		}
		NonTerminal startSymbol = rules.get(grammar.startSymbol);
		NonTerminal startPrime = new NonTerminal("START'");
		startPrime.addAlt(new amstin.models.grammar.parsing.oogll.Alt(LAYOUT, startSymbol, LAYOUT));
		this.gll = new GLL(startPrime);
	}
	
	private Node parse(String src) {
		return gll.parse(src);
	}


	
	
	private void convertRule(Rule rule, NonTerminal nt) {
		for (Alt alt: rule.alts) {
			int size = alt.elements.size();
			amstin.models.grammar.parsing.oogll.symbol.Symbol elts[] = new amstin.models.grammar.parsing.oogll.symbol.Symbol[size * 2 - 1];
			for (int i = 0; i < size * 2 - 1; i += 2) {
				elts[i] = convertSymbol(alt.elements.get(i / 2).symbol);
				if (i < elts.length - 1) {
					elts[i+1] = LAYOUT;
				}
			}
			nt.addAlt(new amstin.models.grammar.parsing.oogll.Alt(elts));
		}
	}

	private Symbol convertSymbol(amstin.models.grammar.Symbol symbol) {
		if (symbol instanceof Id) {
			return ID;
		}
		if (symbol instanceof Str) {
			return STR;
		}
		if (symbol instanceof Real) {
			return REAL;
		}
		if (symbol instanceof Int) {
			return INT;
		}
		if (symbol instanceof Ref) {
			return REF;
		}
		if (symbol instanceof Key) {
			return KEY;
		}
		if (symbol instanceof Bool) {
			return BOOL;
		}
		if (symbol instanceof Sym) {
			return rules.get(((Sym)symbol).rule);
		}
		if (symbol instanceof Iter) {
			Symbol arg = convertSymbol(((Iter)symbol).arg);
			return new amstin.models.grammar.parsing.oogll.symbol.IterSep(arg, LAYOUT);
		}
		if (symbol instanceof IterStar) {
			Symbol arg = convertSymbol(((IterStar)symbol).arg);
			return new amstin.models.grammar.parsing.oogll.symbol.IterSepStar(arg, LAYOUT);
		}
		if (symbol instanceof IterSep) {
			String sep = ((IterSep)symbol).sep;
			Symbol node = convertSymbol(((IterSep)symbol).arg);
			Symbol sepNode = new amstin.models.grammar.parsing.oogll.symbol.Lit(sep);
			return new amstin.models.grammar.parsing.oogll.symbol.IterSep(node, LAYOUT, sepNode, LAYOUT);
		}
		if (symbol instanceof IterSepStar) {
			String sep = ((IterSep)symbol).sep;
			Symbol node = convertSymbol(((IterSep)symbol).arg);
			Symbol sepNode = new amstin.models.grammar.parsing.oogll.symbol.Lit(sep);
			return new amstin.models.grammar.parsing.oogll.symbol.IterSepStar(node, LAYOUT, sepNode, LAYOUT);
		}

		if (symbol instanceof Opt) {
			Symbol node = convertSymbol(((Opt)symbol).arg);
			return new amstin.models.grammar.parsing.oogll.symbol.Opt(node);
		}
		if (symbol instanceof Lit) {
			return new LitToken(((Lit)symbol).value);
		}
		throw new RuntimeException("Invalid symbol: " + symbol);
	}
	
	
}
