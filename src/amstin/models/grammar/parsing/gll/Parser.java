package amstin.models.grammar.parsing.gll;

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
import amstin.models.grammar.parsing.gll.prods.Anon;
import amstin.models.grammar.parsing.gll.prods.Appl;
import amstin.models.grammar.parsing.gll.prods.Production;
import amstin.models.grammar.parsing.gll.prods.Regular;
import amstin.models.grammar.parsing.gll.prods.Terminal;
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

	
	
	
	public static void main(String[] args) throws InterruptedException {
//		String grammarSrc = amstin.models.grammar.parsing.mj.Parser.readPath("src/amstin/models/grammar/grammarBug.mdg");
//		Grammar g = amstin.models.grammar.parsing.mj.Parser.parseGrammar(_Main.GRAMMAR_MDG);
		Grammar g = amstin.models.grammar.parsing.cps.Parser.parseGrammar("src/amstin/models/grammar/parsing/gll/test.mdg");
		String src = "a b c";
		
		Parser p = new Parser(g);
		
//		String trimmed = grammarSrc.trim();
//		System.out.println(trimmed.substring(178));
//		Tree tree = p.parse("Grammar", trimmed);
		
		Tree tree = p.parse("Stat", src);
		
		System.out.println(tree);
	}
	
	
	private Grammar grammar;

	private int ids = 0;

	// TODO: [\\\\]?[a-zA-Z_$][a-zA-Z_$0-9]*
	private final CharStackNode ID_HEAD = new CharStackNode(ids++, new char[][]{{'a', 'z'}, {'A', 'Z'}}); 
	private final CharStackNode ID_TAIL_CHAR = new CharStackNode(ids++, new char[][]{{'a', 'z'}, {'A', 'Z'}, {'0', '9'}}); 
	private final ListStackNode ID_TAIL = new ListStackNode(ids++, new Anon(), ID_TAIL_CHAR, false);
	private final Production ID_PROD = new Terminal(new Id());
	private final IReducableStackNode[] ID_RESTRICTIONS = new IReducableStackNode[] {ID_TAIL_CHAR};
	private final AbstractStackNode[] ID_REJECTS;
	
	//[-+]?[0-9]+
	private final CharStackNode INT_SIGN = new CharStackNode(ids++, new char[][]{{'-', '-'}, {'+', '+'}});
	private final OptionalStackNode INT_SIGN_OPT = new OptionalStackNode(ids++, new Anon(), INT_SIGN);
	private final CharStackNode INT_DIGIT = new CharStackNode(ids++, new char[][]{{'0', '9'}}); 
	private final ListStackNode INT_DIGITS = new ListStackNode(ids++, new Anon(), INT_DIGIT, true);
	private final IReducableStackNode[] INT_RESTRICTIONS = new IReducableStackNode[] {INT_DIGIT};
	private final Production INT_PROD = new Terminal(new Int());
	
	// [-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?
	
	private final CharStackNode REAL_SIGN = new CharStackNode(ids++, new char[][]{{'-', '-'}, {'+', '+'}});
	private final OptionalStackNode REAL_SIGN_OPT = new OptionalStackNode(ids++, new Anon(), REAL_SIGN);
	private final CharStackNode REAL_DIGIT_PRE = new CharStackNode(ids++, new char[][]{{'0', '9'}});
	private final ListStackNode REAL_DIGITS_PRE = new ListStackNode(ids++, new Anon(), REAL_DIGIT_PRE, false);
	private final CharStackNode REAL_PERIOD = new CharStackNode(ids++, new char[][] {{'.', '.'}});
	private final OptionalStackNode REAL_PERIOD_OPT = new OptionalStackNode(ids++, new Anon(), REAL_PERIOD);
	private final CharStackNode REAL_DIGIT_POST = new CharStackNode(ids++, new char[][]{{'0', '9'}});
	private final ListStackNode REAL_DIGITS_POST = new ListStackNode(ids++, new Anon(), REAL_DIGIT_POST, true);
	private final Production REAL_PROD = new Terminal(new Real());
	private final IReducableStackNode[] REAL_RESTRICTIONS = new IReducableStackNode[] {REAL_DIGIT_POST};
	
	
	private final CharStackNode EXP_E = new CharStackNode(ids++, new char[][]{{'e', 'e'}, {'E', 'E'}});
	private final CharStackNode EXP_SIGN = new CharStackNode(ids++, new char[][]{{'-', '-'}, {'+', '+'}});
	private final OptionalStackNode EXP_SIGN_OPT = new OptionalStackNode(ids++, new Anon(), EXP_SIGN);
	private final CharStackNode EXP_DIGIT = new CharStackNode(ids++, new char[][]{{'0', '9'}});
	private final ListStackNode EXP_DIGITS= new ListStackNode(ids++, new Anon(), EXP_DIGIT, true);
	
	// [\"]~[\"]*[\"]
	private final CharStackNode STR_QUOTE_1 = new CharStackNode(ids++, new char[][] {{'"', '"'}});
	private final CharStackNode STR_NOQUOTE = new CharStackNode(ids++, 
			new char[][] {{0, '"' - 1}, {'"' + 1, (char) Character.MAX_CODE_POINT }});
	private final ListStackNode STR_CONTENTS = new ListStackNode(ids++, new Anon(), STR_NOQUOTE, false); 
	private final CharStackNode STR_QUOTE_2 = new CharStackNode(ids++, new char[][] {{'"', '"'}});
	private final Production STR_PROD = new Terminal(new Str());
	
	// true|false
	private final LiteralStackNode BOOL_TRUE = new LiteralStackNode(ids++, new Anon(), "true".toCharArray());
	private final LiteralStackNode BOOL_FALSE = new LiteralStackNode(ids++, new Anon(), "false".toCharArray());
	private final Production BOOL_PROD = new Terminal(new Bool());
	private final CharStackNode BOOL_CHARS = new CharStackNode(ids++, new char[][]{{'a', 'z'}, {'A', 'Z'}, {'0', '9'}}); 
	private final IReducableStackNode[] BOOL_RESTRICTIONS = new IReducableStackNode[] {BOOL_CHARS};
	
	// Id
	private final Production KEY_PROD = new Terminal(new Key());
	private final CharStackNode KEY_HEAD = new CharStackNode(ids++, new char[][]{{'a', 'z'}, {'A', 'Z'}}); 
	private final CharStackNode KEY_TAIL_CHAR = new CharStackNode(ids++, new char[][]{{'a', 'z'}, {'A', 'Z'}, {'0', '9'}}); 
	private final ListStackNode KEY_TAIL = new ListStackNode(ids++, new Anon(), KEY_TAIL_CHAR, false);
	
	private final Production REF_PROD = new Terminal(new Ref());
	private final CharStackNode REF_HEAD = new CharStackNode(ids++, new char[][]{{'a', 'z'}, {'A', 'Z'}}); 
	private final CharStackNode REF_TAIL_CHAR = new CharStackNode(ids++, new char[][]{{'a', 'z'}, {'A', 'Z'}, {'0', '9'}}); 
	private final ListStackNode REF_TAIL = new ListStackNode(ids++, new Anon(), REF_TAIL_CHAR, false);
	
	
	private final Production LAYOUT_PROD = new Anon();
	private final CharStackNode LAYOUT_CHAR = new CharStackNode(ids++, new char[][]{
				{' ', ' '}, 
				{'\t', '\t'},
				{'\n', '\n'}});
	private final ListStackNode LAYOUT_CHARS = new ListStackNode(ids++, new Anon(), LAYOUT_CHAR, false);
	private final IReducableStackNode[] LAYOUT_RESTRICTIONS = new IReducableStackNode[] {LAYOUT_CHAR};
	
	
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
			System.out.println("Saving expectations for " + methodName);
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

	// [-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?
	@SuppressWarnings("unused")
	private void expectReal() {
		expect(REAL_PROD, REAL_RESTRICTIONS, REAL_SIGN_OPT, REAL_DIGITS_PRE, REAL_PERIOD_OPT, REAL_DIGITS_POST);
		expect(REAL_PROD, REAL_RESTRICTIONS, REAL_SIGN_OPT, REAL_DIGITS_PRE, REAL_PERIOD_OPT, REAL_DIGITS_POST,
				EXP_E, EXP_SIGN_OPT, EXP_DIGITS);
	}


	@SuppressWarnings("unused")
	private void expectStr() {
		expect(STR_PROD, STR_QUOTE_1, STR_CONTENTS, STR_QUOTE_2);
	}
	
	@SuppressWarnings("unused")
	private void expectBool() {
		expect(BOOL_PROD, BOOL_RESTRICTIONS, BOOL_TRUE);
		expect(BOOL_PROD, BOOL_RESTRICTIONS, BOOL_FALSE);
	}

	//[-+]?[0-9]+
	@SuppressWarnings("unused")
	private void expectRef() {
		expect(REF_PROD, ID_RESTRICTIONS, REF_HEAD, REF_TAIL);
	}

	@SuppressWarnings("unused")
	private void expectKey() {
		expect(KEY_PROD, ID_RESTRICTIONS, KEY_HEAD, KEY_TAIL);
	}

	@SuppressWarnings("unused")
	private void expectLayout() {
		expect(LAYOUT_PROD, /*LAYOUT_RESTRICTIONS,*/ LAYOUT_CHARS);
	}

	


	private AbstractStackNode[] keywordLiterals() {
		Set<String> keywords = grammar.reservedKeywords();
		AbstractStackNode[] result = new AbstractStackNode[keywords.size()];
		int i = 0;
		for (String s: keywords) {
			result[i] = new LiteralStackNode(ids++, new Anon(), s.toCharArray());
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
			result.add(new Expectation(new Appl(rule, alt), nodes));
		}
		return result;
	}
	
	private NonTerminalStackNode makeLayout() {
		return new NonTerminalStackNode(ids++, terminalName("Layout"));
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
			return new SeparatedListStackNode(ids++, new Regular(symbol), node, new AbstractStackNode[]{
				makeLayout()}, true);
		}
		if (symbol instanceof IterStar) {
			Symbol arg = ((IterStar)symbol).arg;
			AbstractStackNode node = makeStackNodeForSymbol(arg);
			return new SeparatedListStackNode(ids++, new Regular(symbol), node, new AbstractStackNode[]{
				makeLayout()}, false);
		}
		if (symbol instanceof IterSep) {
			Symbol arg = ((IterSep)symbol).arg;
			String sep = ((IterSep)symbol).sep;
			AbstractStackNode node = makeStackNodeForSymbol(arg);
			LiteralStackNode sepNode = new LiteralStackNode(ids++, new Anon(), sep.toCharArray());
			return new SeparatedListStackNode(ids++, new Regular(symbol), node, new AbstractStackNode[]{
				makeLayout(),
				sepNode,
				makeLayout()}, true);
		}
		if (symbol instanceof IterSepStar) {
			Symbol arg = ((IterSepStar)symbol).arg;
			String sep = ((IterSepStar)symbol).sep;
			AbstractStackNode node = makeStackNodeForSymbol(arg);
			LiteralStackNode sepNode = new LiteralStackNode(ids++, new Anon(), sep.toCharArray());
			return new SeparatedListStackNode(ids++, new Regular(symbol), node, new AbstractStackNode[]{
				makeLayout(),
				sepNode,
				makeLayout()}, false);			
		}
		if (symbol instanceof Opt) {
			Symbol arg = ((Opt)symbol).arg;
			AbstractStackNode node = makeStackNodeForSymbol(arg);
			return new OptionalStackNode(ids++, new Regular(symbol), node);
		}
		if (symbol instanceof Lit) {
			String lit = ((Lit)symbol).value;
			return new LiteralStackNode(ids++, new amstin.models.grammar.parsing.gll.prods.Lit(lit), lit.toCharArray());
		}
		throw new RuntimeException("Invalid symbol: " + symbol);
	}

	private String terminalName(Symbol symbol) {
		return terminalName(symbol.getClass().getSimpleName());
	}
	
	private String terminalName(String name) {
		return TERMINAL_SIGIL + name;
	}

	
}
