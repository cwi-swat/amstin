package amstin.models.grammar.parsing.gll;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import amstin.models.ast.Tree;
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
import amstin.models.grammar.Symbol;
import amstin.models.grammar.parsing.gll.stack.AbstractStackNode;
import amstin.models.grammar.parsing.gll.stack.CharStackNode;
import amstin.models.grammar.parsing.gll.stack.IReducableStackNode;
import amstin.models.grammar.parsing.gll.stack.ListStackNode;
import amstin.models.grammar.parsing.gll.stack.LiteralStackNode;
import amstin.models.grammar.parsing.gll.stack.NonTerminalStackNode;
import amstin.models.grammar.parsing.gll.stack.OptionalStackNode;
import amstin.models.grammar.parsing.gll.stack.SeparatedListStackNode;

public class Parser extends SGLL {
	private static final String TERMINAL_SIGIL = "#";

	
	
	
	public static void main(String[] args) {
		Grammar g = amstin.models.grammar.parsing.mj.Parser.parseGrammar("src/amstin/models/grammar/parsing/gll/test.mdg");
		String src = "+123+2";
		
		Parser p = new Parser(g);
		Tree tree = p.parse("Exp", src);
		System.out.println(tree);
	}
	
	
	private Grammar grammar;

	private int ids = 0;

	// TODO: [\\\\]?[a-zA-Z_$][a-zA-Z_$0-9]*
	private final CharStackNode ID_HEAD = new CharStackNode(ids++, new char[][]{{'a', 'z'}, {'A', 'Z'}}); 
	private final CharStackNode ID_TAIL_CHAR = new CharStackNode(ids++, new char[][]{{'a', 'z'}, {'A', 'Z'}, {'0', '9'}}); 
	private final ListStackNode ID_TAIL = new ListStackNode(ids++, new Production("tail"), ID_TAIL_CHAR, false);
	private final Production ID_PROD = new Production(new Id());
	private final IReducableStackNode[] ID_RESTRICTIONS = new IReducableStackNode[] {ID_TAIL_CHAR};
	private final AbstractStackNode[] ID_REJECTS;
	
	//[-+]?[0-9]+
	private final CharStackNode INT_SIGN = new CharStackNode(ids++, new char[][]{{'-', '-'}, {'+', '+'}});
	private final OptionalStackNode INT_SIGN_OPT = new OptionalStackNode(ids++, new Production("signopt"), INT_SIGN);
	private final CharStackNode INT_DIGIT = new CharStackNode(ids++, new char[][]{{'0', '9'}}); 
	private final ListStackNode INT_DIGITS = new ListStackNode(ids++, new Production("digits"), INT_DIGIT, true);
	private final IReducableStackNode[] INT_RESTRICTIONS = new IReducableStackNode[] {INT_DIGIT};
	private final Production INT_PROD = new Production(new Int());
	
	
	public Parser(Grammar grammar) {
		super();
		this.grammar = grammar;
		ID_REJECTS = keywordLiterals();
	}
	
	public Tree parse(String startSymbol, String src) {
		NonTerminalStackNode start = new NonTerminalStackNode(ids++, startSymbol);
		return parse(start, null, src);
	}
	
	private class Expectation {
		private Production prod;
		private AbstractStackNode[] nodes;

		Expectation(Production prod, AbstractStackNode[] nodes) {
			this.prod = prod;
			this.nodes = nodes;
		}
		
		void expect() {
			Parser.this.expect(prod, nodes);
		}
	}
	
	private Map<String, List<Expectation>> expectations = new HashMap<String, List<Expectation>>();

	@Override
	protected void invokeExpects(String methodName) {
		if (methodName.startsWith(TERMINAL_SIGIL)) {
			expectTerminal(methodName.substring(TERMINAL_SIGIL.length()));
			return;
		}
		
		if (!expectations.containsKey(methodName)) {
			saveExpectations(methodName);
		}
		
		for (Expectation exp: expectations.get(methodName)) {
			exp.expect();
		}
	}

	private void expectTerminal(String name) {
		try {
			Method method = getClass().getDeclaredMethod("expect" + name);
			method.invoke(this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	

	@SuppressWarnings("unused")
	private void expectId() {
		expect(ID_PROD, ID_RESTRICTIONS, ID_HEAD, ID_TAIL);
		for (AbstractStackNode reject: ID_REJECTS) {
			expectReject(ID_PROD, reject);
		}
	}

	//[-+]?[0-9]+
	@SuppressWarnings("unused")
	private void expectInt() {
		expect(INT_PROD, INT_RESTRICTIONS, INT_SIGN_OPT, INT_DIGITS);
	}
	
	private AbstractStackNode[] keywordLiterals() {
		Set<String> keywords = grammar.reservedKeywords();
		AbstractStackNode[] result = new AbstractStackNode[keywords.size()];
		int i = 0;
		for (String s: keywords) {
			result[i] = new LiteralStackNode(ids++, new Production(s), s.toCharArray());
			i++;
		}
		return result;
	}

	private void saveExpectations(String methodName) {
		for (Rule rule: grammar.rules) {
			if (methodName.equals(rule.name)) {
				expectations.put(methodName, altExpectations(rule));
				return;
			}
		}
	}

	private List<Expectation> altExpectations(Rule rule) {
		List<Expectation> result = new ArrayList<Expectation>();
		for (Alt alt: rule.alts) {
			int size = alt.elements.size();
			AbstractStackNode nodes[] = new AbstractStackNode[size * 2 - 1];
			for (int i = 0; i < size * 2 - 1; i += 2) {
				nodes[i] = makeStackNodeForSymbol(alt.elements.get(i / 2).symbol);
				if (i < nodes.length - 1) {
					nodes[i+1] = makeLayout();
				}
			}
			result.add(new Expectation(new Production(rule, alt), nodes));
		}
		return result;
	}

	private ListStackNode makeLayout() {
		CharStackNode whitespace = new CharStackNode(ids++, new char[][]{
				{' ', ' '}, 
				{'\t', '\t'},
				{'\n', '\n'}});
		return new ListStackNode(ids++, new Production("layout"), whitespace, false);
	}

	private AbstractStackNode makeStackNodeForSymbol(Symbol symbol) {
		if (symbol instanceof Id 
				|| symbol instanceof Str
				|| symbol instanceof Real
				|| symbol instanceof Int
				|| symbol instanceof Ref
				|| symbol instanceof Key
				|| symbol instanceof Bool) {
			 return new NonTerminalStackNode(ids++, terminalName(symbol));
		}
		if (symbol instanceof Sym) {
			Rule rule = ((Sym)symbol).rule;
			return new NonTerminalStackNode(ids++, rule.name);
		}
		if (symbol instanceof Iter) {
			Symbol arg = ((Iter)symbol).arg;
			AbstractStackNode node = makeStackNodeForSymbol(arg);
			return new ListStackNode(ids++, new Production(symbol), node, true);
		}
		if (symbol instanceof IterStar) {
			Symbol arg = ((IterStar)symbol).arg;
			AbstractStackNode node = makeStackNodeForSymbol(arg);
			return new ListStackNode(ids++, new Production(symbol), node, false);			
		}
		if (symbol instanceof IterSep) {
			Symbol arg = ((IterSep)symbol).arg;
			String sep = ((IterSep)symbol).sep;
			AbstractStackNode node = makeStackNodeForSymbol(arg);
			LiteralStackNode sepNode = new LiteralStackNode(ids++, null, sep.toCharArray());
			//public SeparatedListStackNode(int id, Production production, AbstractStackNode child, AbstractStackNode[] separators, boolean isPlusList){

			return new SeparatedListStackNode(ids++, new Production(symbol), node, new AbstractStackNode[]{sepNode}, true);
		}
		if (symbol instanceof IterSepStar) {
			Symbol arg = ((IterSepStar)symbol).arg;
			String sep = ((IterSepStar)symbol).sep;
			AbstractStackNode node = makeStackNodeForSymbol(arg);
			LiteralStackNode sepNode = new LiteralStackNode(ids++, null, sep.toCharArray());
			return new SeparatedListStackNode(ids++, new Production(symbol), node, new AbstractStackNode[]{sepNode}, false);			
		}
		if (symbol instanceof Opt) {
			Symbol arg = ((Opt)symbol).arg;
			AbstractStackNode node = makeStackNodeForSymbol(arg);
			return new OptionalStackNode(ids++, new Production(symbol), node);
		}
		if (symbol instanceof Lit) {
			String lit = ((Lit)symbol).value;
			return new LiteralStackNode(ids++, new Production(symbol), lit.toCharArray());
		}
		throw new RuntimeException("Invalid symbol: " + symbol);
	}

	private String terminalName(Symbol symbol) {
		return TERMINAL_SIGIL + symbol.getClass().getSimpleName();
	}
	
}
