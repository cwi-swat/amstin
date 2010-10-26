package amstin.models.grammar.parsing.cps;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import amstin.models.ast.Arg;
import amstin.models.ast.Def;
import amstin.models.ast.Location;
import amstin.models.ast.Obj;
import amstin.models.ast.ParseTree;
import amstin.models.ast.Tree;
import amstin.models.ast.Ws;
import amstin.models.grammar.Alt;
import amstin.models.grammar.Bool;
import amstin.models.grammar.Boot;
import amstin.models.grammar.Element;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.Id;
import amstin.models.grammar.Int;
import amstin.models.grammar.Iter;
import amstin.models.grammar.IterSep;
import amstin.models.grammar.IterSepStar;
import amstin.models.grammar.IterStar;
import amstin.models.grammar.Key;
import amstin.models.grammar.Klass;
import amstin.models.grammar.Label;
import amstin.models.grammar.Lit;
import amstin.models.grammar.Opt;
import amstin.models.grammar.Real;
import amstin.models.grammar.Ref;
import amstin.models.grammar.Rule;
import amstin.models.grammar.Str;
import amstin.models.grammar.Sym;
import amstin.models.grammar.Symbol;
import amstin.tools.ASTtoModel;


public class Parser {
	// A unique instance for layout tokens (whitespace).
	private static final Layout LAYOUT = new Layout();
	
	// TODO move to utils or something.
	public static String readPath(String filePath) {
		byte[] buffer = new byte[(int) new File(filePath).length()];
	    BufferedInputStream f;
		try {
			f = new BufferedInputStream(new FileInputStream(filePath));
			f.read(buffer);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e); 
		}
	    String src = new String(buffer);
	    return src;
	}
	
	public static String readPath(File file) {
		return readPath(file.getAbsolutePath());
	}
	
	public static Grammar parseGrammar(String path) {
		String src = readPath(path);
		Grammar grammar = Boot.instance;
		Parser parser = new Parser(grammar);
		ParseTree pt = parser.parse(src);
		return (Grammar) ASTtoModel.instantiate(amstin.models.grammar._Main.GRAMMAR_PKG, pt);
	}

	
	public static final String ID_REGEX = "[\\\\]?[a-zA-Z_$][a-zA-Z_$0-9]*";
	private static final String REF_REGEX = "(" + ID_REGEX + ")" + "(\\."  + ID_REGEX + ")*";

	private Map<Sym, IParser> symCache;
	private Map<Alt, IParser> altCache;
	private Map<Symbol, IParser> optCache;
	private Map<Symbol, IParser> iterCache;
	private Map<Symbol, IParser> iterSepCache;
	private Map<IParser, Map<Integer, Entry>> table;
	private final Grammar grammar;
	private Set<String> reserved;
	private String file = "-";

	public Parser(Grammar grammar) {
		this.grammar = grammar;
		this.reserved = grammar.reservedKeywords();
	}
	
	public Grammar getGrammar() {
		return grammar;
	}
	
	
	public Object parse(String pkg, String src) {
		ParseTree pt = parseAsPath("-", src);
		return ASTtoModel.instantiate(pkg, pt);
	}
	
	public ParseTree parse(File file) {
		String path = file.getAbsolutePath();
		return parseAsPath(path, readPath(path));
	}

	public ParseTree parse(String src) {
		return parseAsPath("-", src);
	}
	
	private ParseTree parseAsPath(String file, String src) { 
		this.file = file;
		this.symCache = new HashMap<Sym, IParser>();
		this.altCache = new HashMap<Alt, IParser>();
		this.optCache = new HashMap<Symbol, IParser>();
		this.iterCache = new HashMap<Symbol, IParser>();
		this.iterSepCache = new HashMap<Symbol, IParser>();
		this.table = new HashMap<IParser, Map<Integer,Entry>>();
		
		Success success = new Success(src);
		try {
			// This is sort of klunky, in order to bypass layout
			// interleaving in normal productions.
			Rule rule = makeParseTreeProduction(grammar.startSymbol);
			Alt alt = rule.alts.get(0);
			IParser p = list(alt.elements);
			int errLoc = p.parse(table, new Build(rule, alt.type, success), src, 0);
			System.err.println("Parse error at: " + errLoc);
			return null;
		}
		catch (Success s) {
			return parseTreeTreeToParseTree((Obj) s.object);
		}
	}
	
	private ParseTree parseTreeTreeToParseTree(Obj pt) {
		ParseTree parseTree = new ParseTree();
		parseTree.preLayout = pt.args.get(0);
		parseTree.top = pt.args.get(1);
		parseTree.postLayout = pt.args.get(2);
		return parseTree;
	}

	public Rule makeParseTreeProduction(Rule rule) {
		Sym start = new Sym();
		start.rule = rule;
		
		List<Element> elts = new ArrayList<Element>();
		elts.add(LAYOUT);
		
		Element e = new Element();
		e.symbol = start;
		Label l = new Label();
		l.name = "<start>";
		e.label = l;
		elts.add(e);			
		
		elts.add(LAYOUT);


		Rule startP = new Rule();
		startP.name = "<start>";
		startP.alts = new ArrayList<Alt>();
		Alt alt = new Alt();
		Klass kls = new Klass();
		kls.name = "<ParseTree>";
		alt.type = kls;
		alt.elements = elts;
		startP.alts.add(alt);

		return startP;
	}
	
	public int parse(Symbol sym, Cnt cnt, String src, int pos) {
		if (sym instanceof Lit) {
			return parseLit((Lit)sym, cnt, src, pos);
		}
		else if (sym instanceof Int) {
			return parseInt((Int)sym, cnt, src, pos);
		}
		else if (sym instanceof Real) {
			return parseReal((Real)sym, cnt, src, pos);
		}
		else if (sym instanceof Bool) {
			return parseBool((Bool)sym, cnt, src, pos);
		}
		else if (sym instanceof Id) {
			return parseId((Id)sym, cnt, src, pos);
		}
		else if (sym instanceof Str) {
			return parseStr((Str)sym, cnt, src, pos);
		}
		else if (sym instanceof Key) {
			return parseKey((Key)sym, cnt, src, pos);
		}
		else if (sym instanceof Ref) {
			return parseRef((Ref)sym, cnt, src, pos);
		}
		else if (sym instanceof Opt) {
			return parseOpt((Opt)sym, cnt, src, pos);
		}
		else if (sym instanceof Sym) {
			return parseSym((Sym)sym, cnt, src, pos);
		}
		else if (sym instanceof Iter) {
			return parseIter((Iter)sym, cnt, src, pos);
		}
		else if (sym instanceof IterStar) {
			return parseIterStar((IterStar)sym, cnt, src, pos);
		}
		else if (sym instanceof IterSep) {
			return parseIterSep((IterSep)sym, cnt, src, pos);
		}
		else if (sym instanceof IterSepStar) {
			return parseIterSepStar((IterSepStar)sym, cnt, src, pos);
		}
		else {
			throw new RuntimeException("invalid symbol: " + sym);
		}
	}
	

	protected int parseElement(Element elt, Cnt cnt, String src, int pos) {
		if (elt instanceof Layout) {
			return parseLayout(cnt, src, pos);
		}
		
		if (elt instanceof IParser) {
			return ((IParser)elt).parse(table, cnt, src, pos);
		}
		
		if (elt.label != null && elt.symbol != null) {
			return parse(elt.symbol, new LabelCnt(cnt, elt.label.name), src, pos);
		}

		if (elt.symbol == null) {
			throw new RuntimeException("Element's symbol is null " + elt);
		}
		return parse(elt.symbol, cnt, src, pos);
	}
	
	protected static class LabelCnt implements Cnt {
		private Cnt cnt;
		private String name;

		public LabelCnt(Cnt cnt, String name) {
			this.cnt = cnt;
			this.name = name;
		}
		
		@Override
		public int apply(int result, Tree obj) {
			Arg arg = new Arg();
			arg.name = name;
			arg.value = obj;
			return cnt.apply(result, arg);
		}
		
	}
	
	private int parseAlt(Rule rule, Alt alt, Cnt cnt, String src, int pos) {
		if (!altCache.containsKey(alt)) {
			altCache.put(alt, new Memo(list(interleaveLayout(alt.elements))));
		}
		IParser p = altCache.get(alt); 
		return p.parse(table, new Build(rule, alt.type, cnt), src, pos);
	}
	
	private int parseIterSepStar(IterSepStar sym, Cnt cnt, String src, int pos) {
		int x = parseIteratedSymbolSep(sym.arg, sym.sep, cnt, src, pos);
		int y = cnt.apply(pos, emptyList());
		return x > y ? x : y;
	}

	private int parseIterSep(IterSep sym, Cnt cnt, String src, int pos) {
		return parseIteratedSymbolSep(sym.arg, sym.sep, cnt, src, pos);
	}

	private int parseIteratedSymbolSep(Symbol sym, String sep, Cnt cnt, String src, int pos) {
		if (!iterSepCache.containsKey(sym)) {
			iterSepCache.put(sym, new Memo(new IterSepParser(sym, sep)));
		}
		return iterSepCache.get(sym).parse(table, cnt, src, pos);
	}
	
	private int parseIterStar(IterStar sym, Cnt cnt, String src, int pos) {
		int x = parseIteratedSymbol(sym.arg, cnt, src, pos);
		int y = cnt.apply(pos, emptyList());
		return x > y ? x : y;
	}

	private static amstin.models.ast.List emptyList() {
		amstin.models.ast.List list = new amstin.models.ast.List();
		list.elements = new ArrayList<Tree>();
		return list;
	}
	
	private int parseIter(Iter sym, Cnt cnt, String src, int pos) {
		return parseIteratedSymbol(sym.arg, cnt, src, pos);
	}

	private int parseIteratedSymbol(Symbol sym, Cnt cnt, String src, int pos) {
		if (!iterCache.containsKey(sym)) {
			iterCache.put(sym, new IterParser(sym));
		}
		return iterCache.get(sym).parse(table, cnt, src, pos);
	}
	
	private class IterParser extends Element implements IParser {
		private IParser base;
		private IParser rec;


		public IterParser(Symbol sym) {
			Element elt = new Element();
			elt.symbol = sym;
			this.base = list(Arrays.asList(elt));
			this.rec = list(Arrays.asList(elt, LAYOUT, this));
		}


		@Override
		public int parse(Map<IParser, Map<Integer, Entry>> table, Cnt cnt, String src, int pos) {
			int x = base.parse(table, cnt, src, pos);
			int y = rec.parse(table, cnt, src, pos);
			return x > y ? x : y;
		}
	}
	
	private class IterSepParser extends Element implements IParser {
		private IParser base;
		private IParser rec;


		public IterSepParser(Symbol sym, String sep) {
			Element elt = new Element();
			elt.symbol = sym;
			this.base = list(Arrays.asList(elt));
			Lit lit = new Lit();
			lit.value = sep;
			Element sepElt = new Element();
			sepElt.symbol = lit;
			this.rec = new Memo(list(Arrays.asList(elt, LAYOUT, sepElt, LAYOUT, this)));
		}


		@Override
		public int parse(Map<IParser, Map<Integer, Entry>> table, Cnt cnt, String src, int pos) {
			int x = base.parse(table, cnt, src, pos);
			int y = rec.parse(table, cnt, src, pos);
			return x > y ? x : y;
		}
	}


	private int parseSym(Sym sym, Cnt cnt, String src, int pos) {
		if (!symCache.containsKey(sym)) {
			symCache.put(sym, new SymParser(sym));
		}
		return symCache.get(sym).parse(table, cnt, src, pos);
	}
	
	private class SymParser implements IParser {
		
		private Sym sym;

		public SymParser(Sym sym) {
			this.sym = sym;
		}

		@Override
		public int parse(Map<IParser, Map<Integer, Entry>> table, Cnt cnt, String src, int pos) {
			int x = -1;
			for (Alt alt: sym.rule.alts) {
				int y = parseAlt(sym.rule, alt, cnt, src, pos);
				if (y > x) {
					x = y;
				}
			}
			return x;
		}
		
	}

	private int parseOpt(Opt sym, Cnt cnt, String src, int pos) {
		if (!optCache.containsKey(sym)) {
			optCache.put(sym, new OptParser(sym.arg));
		}
		IParser p = optCache.get(sym);
		return p.parse(table, cnt, src, pos);
	}
	
	private class OptParser implements IParser {
		
		private Symbol sym;

		public OptParser(Symbol sym) {
			this.sym = sym;
		}

		@Override
		public int parse(Map<IParser, Map<Integer, Entry>> table, Cnt cnt, String src, int pos) {
			if (sym instanceof Lit) {
				int x = Parser.this.parse(sym, new OptCnt(cnt), src, pos);
				amstin.models.ast.Bool b = new amstin.models.ast.Bool();
				b.value = false;
				int y = cnt.apply(pos, b);
				return x > y ? x : y;
			}
			int x = Parser.this.parse(sym, cnt, src, pos);
			int y = cnt.apply(pos, null);
			return x > y ? x : y;
		}
		
	}
	
	private class OptCnt implements Cnt {
		
		private Cnt cnt;

		public OptCnt(Cnt cnt) {
			this.cnt = cnt;
		}

		@Override
		public int apply(int result, Tree obj) {
			amstin.models.ast.Bool b = new amstin.models.ast.Bool();
			b.value = true;
			return cnt.apply(result, b);
		}
		
	}

	protected int parseRef(Ref sym, Cnt cnt, String src, int pos) {
		Pattern re = Pattern.compile(REF_REGEX);
		Matcher m = re.matcher(src.subSequence(pos, src.length()));
		if (m.lookingAt() && !isReserved(m.group())) {
			amstin.models.ast.Ref ref = new amstin.models.ast.Ref();
			ref.name = unescapeId(m.group());
			ref.type = sym.ref;
			ref.loc = makeLoc(pos, m.group().length());
			return cnt.apply(pos + m.end(), ref);
		}
		return pos;
	}

	private int parseKey(Key sym, Cnt cnt, String src, int pos) {
		Pattern re = Pattern.compile(ID_REGEX);
		Matcher m = re.matcher(src.subSequence(pos, src.length()));
		if (m.lookingAt() && !isReserved(m.group())) {
			Def def = new Def();
			def.name = unescapeId(m.group());
			def.loc = makeLoc(pos, m.group().length());
			return cnt.apply(pos + m.end(), def);
		}
		return pos;
	}

	private String unescapeId(String str) {
		return str.replaceAll("\\\\", "");
	}

	public int parseLit(Lit lit, Cnt cnt, String src, int pos) {
		Pattern re = Pattern.compile(Pattern.quote(lit.value));
		Matcher m = re.matcher(src.subSequence(pos, src.length()));
		if (m.lookingAt()) {
			amstin.models.ast.Lit litAst = new amstin.models.ast.Lit();
			litAst.value = lit.value;
			litAst.loc = makeLoc(pos, lit.value.length());	
			return cnt.apply(pos + m.end(), litAst);
		}
		return pos;
	}

	public int parseLayout(Cnt cnt, String src, int pos) {
		Pattern re = Pattern.compile("([\\t\\n\\r\\f ]*(//[^\n]*\n)?)*");
		Matcher m = re.matcher(src.subSequence(pos, src.length()));
		if (m.lookingAt()) {
			Ws ws = new Ws();
			ws.value = m.group();
			ws.loc = makeLoc(pos, m.end());
			return cnt.apply(pos + m.end(), ws);
		}
		return pos;
	}


	public int parseId(Id id, Cnt cnt, String src, int pos) {
		Pattern re = Pattern.compile(ID_REGEX);
		Matcher m = re.matcher(src.subSequence(pos, src.length()));
		if (m.lookingAt() && !isReserved(m.group())) {
			amstin.models.ast.Id idAst = new amstin.models.ast.Id();
			idAst.loc = makeLoc(pos, m.group().length());
			idAst.value = unescapeId(m.group());
			return cnt.apply(pos + m.end(), idAst);
		}
		return pos;
	}
	
	private boolean isReserved(String str) {
		return reserved.contains(str);
	}

	public int parseInt(Int n, Cnt cnt, String src, int pos) {
		Pattern re = Pattern.compile("[-+]?[0-9]+");
		Matcher m = re.matcher(src.subSequence(pos, src.length()));
		if (m.lookingAt()) {
			amstin.models.ast.Int intAst = new amstin.models.ast.Int();
			intAst.value = Integer.parseInt(m.group());
			intAst.loc = makeLoc(pos, m.group().length());
			return cnt.apply(pos + m.end(), intAst);
		}
		return pos;
	}
	
	private int parseBool(Bool sym, Cnt cnt, String src, int pos) {
		Pattern re = Pattern.compile("true|false");
		Matcher m = re.matcher(src.subSequence(pos, src.length()));
		if (m.lookingAt()) {
			amstin.models.ast.Bool bool = new amstin.models.ast.Bool();
			bool.value = Boolean.parseBoolean(m.group());
			bool.loc = makeLoc(pos, m.group().length());
			return cnt.apply(pos + m.end(), bool);
		}
		return pos;
	}
	
	private int parseReal(Real sym, Cnt cnt, String src, int pos) {
		Pattern re = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
		Matcher m = re.matcher(src.subSequence(pos, src.length()));
		if (m.lookingAt()) {
			amstin.models.ast.Real real = new amstin.models.ast.Real();
			real.value = Double.parseDouble(m.group());
			real.loc = makeLoc(pos, m.group().length());
			return cnt.apply(pos + m.end(), real);
		}
		return pos;
	}



	private Location makeLoc(int pos, int length) {
		Location loc = new Location();
		loc.offset = pos;
		loc.length = length;
		loc.file = this.file ;
		return loc;
	}

	public int parseStr(Str str, Cnt cnt, String src, int pos) {
		Pattern re = Pattern.compile("\"(\\\\.|[^\"])*\"");
		Matcher m = re.matcher(src.subSequence(pos, src.length()));
		if (m.lookingAt()) {
			String s = m.group();
			s = s.replaceAll("\\\"", "\"");
			s = s.replaceAll("\\n", "\n");
			s = s.replaceAll("\\t", "\t");
			s = s.replaceAll("\\f", "\f");
			s = s.replaceAll("\\r", "\r");
			amstin.models.ast.Str strAst = new amstin.models.ast.Str();
			strAst.value = s.substring(1, s.length() - 1);
			strAst.loc = makeLoc(pos, m.group().length());
			return cnt.apply(pos + m.end(), strAst);
		}
		return pos;
	}

	static List<Element> interleaveLayout(List<Element> elements) {
		List<Element> list = new ArrayList<Element>();
		for (int i = 0; i < elements.size(); i++) {
			list.add(elements.get(i));
			if (i < elements.size() - 1) {
				list.add(LAYOUT);
			}
		}
		return list;
	}
	
	private static class Layout extends Element {
	}

	
	private IParser list(List<Element> list) {
		if (list.isEmpty()) {
			return new Nil();
		}
		Element head = list.get(0);
		List<Element> tail = list.subList(1, list.size());
		return new Cons(head, list(tail));
	}
	
	
	
	
	private static class Nil implements IParser {

		@Override
		public int parse(Map<IParser, Map<Integer, Entry>> table, Cnt cnt, String src, int pos) {
			return cnt.apply(pos, emptyList());
		}
		
	}
	
	private class Cons implements IParser {
		
		private final Element p1;
		private final IParser p2;

		public Cons(Element p1, IParser p2) {
			this.p1 = p1;
			this.p2 = p2;
		}

		@Override
		public int parse(Map<IParser, Map<Integer, Entry>> table, Cnt cnt, String src, int pos) {
			return Parser.this.parseElement(p1, new Head(p2, src, cnt), src, pos);
		}
		
	}
	
	
	private class Head implements Cnt {

		private IParser p2;
		private String src;
		private Cnt cnt;

		public Head(IParser p2, String src, Cnt cnt) {
			this.p2 = p2;
			this.src = src;
			this.cnt = cnt;
		}

		@Override
		public int apply(int result, Tree obj1) {
			return p2.parse(table, new Tail(obj1, cnt), src, result);
		}
		
	}
	
	private static class Tail implements Cnt {

		private Tree obj1;
		private Cnt cnt;

		public Tail(Tree obj1, Cnt cnt) {
			this.obj1 = obj1;
			this.cnt = cnt;
		}

		@Override
		public int apply(int result, Tree obj) {
			amstin.models.ast.List list = (amstin.models.ast.List)obj;
			if (obj1 != null) {
				if (obj1 instanceof amstin.models.ast.List) {
					list.elements.addAll(((amstin.models.ast.List)obj1).elements);
				}
				else {
					list.elements.add(0, obj1);
				}
			}
			return cnt.apply(result, list);
		}
		
	}

	@SuppressWarnings("serial")
	public static class Success extends RuntimeException implements Cnt {
		private final String src;
		private Tree object;
		
		public Success(String src) {
			this.src = src;
			this.object = null;
		}
		
		public int apply(int result, Tree object) {
			if (result < src.length()) {
				return result;
			}
			this.object = object;
			throw this;
		}
	}
	
	private static class Build implements Cnt {
		private Cnt cnt;
		private Klass klass;
		private Rule rule;

		public Build(Rule rule, Klass klass, Cnt cnt) {
			this.rule = rule;
			this.klass = klass;
			this.cnt = cnt;
		}

		@Override
		public int apply(int result, Tree obj) {
			amstin.models.ast.List kids = (amstin.models.ast.List) obj;
			if (klass == null && rule.isInjection()) {
				// injection
				return cnt.apply(result, kids.elements.get(0));
			}
			
			if (klass == null) {
				// use the rule type as class name.
				Obj ast = new Obj();
				ast.name = rule.name;
				ast.args = kids.elements;
				return cnt.apply(result, ast);
			}

			Obj ast = new Obj();
			ast.name = klass.name;
			ast.args = kids.elements;
			return cnt.apply(result, ast);
		}
		
	}
	
}
