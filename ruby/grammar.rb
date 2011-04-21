
require 'cyclicmap'
require 'bootgrammar'

class PGen < CyclicClosure
  IDPATTERN = "[\\\\]?[a-zA-Z_$][a-zA-Z_$0-9]*"

  def initialize(root)
    super(root)
    @keywordRegexp = keywords()
    puts @keywordRegexp
  end

  def keywords
    kw = ["true", "false"];
    @root.rules.each do |r|
      r.alts.each do |a|
        a.elements.each do |e|
          if e.symbol.klass == "Lit" then
            kw << Regexp.escape(e.symbol.value)
          end
          if e.symbol.klass == "CiLit" then
            kw << ciPattern(e.symbol.value)
          end
        end
      end
    end
    return Regexp.new(kw.join("|"));
  end

  def ciPattern(lit)
    re = "("
    lit.each_char do |c|
      re += "[#{c.upcase}#{c.downcase}]"
    end
    re + ")";
  end

	
  def token(type, pattern)
    lambda { |input, cont, pos|
      if pos >= input.length then
        return
      end
      if input.match(pattern, pos) then
        cont.call(pos + $&.length, factory.Token(type, $&))
      end
    }
  end

  def nonReserved(type, pattern)
    id = token(type, pattern);
    lambda { |input, cont, pos| 
      id.call(input, lambda { |pos1, tree1|
                if tree1.value !~ @keywordRegexp then
                  cont.call(pos1, tree1)
                end
              }, pos)
    }
  end

  class Entry
    attr_reader :continuations, :results
    def initialize
      @continuations = []
      @results = []
    end
  end

  class Table
    def initialize
      @table = {}
    end

    def [](cont, pos)
      @table[cont] ||= {}
      @table[cont][pos] ||= Entry.new
    end
  end
	
  def memo(cpsFn)
    table = Table.new
    return lambda { |input, cont, pos| 
      entry = table[cont, pos]
      if entry.continuations == [] then
        entry.continuations << cont
        cpsFn.call(input, lambda { |result, tree|
                     if !entry.subsumed?(result, tree) then
                       entry.results << [result, tree]
                       enty.continuations.each do |c|
                         c.call(result, tree)
                       end
                     end
                   }, pos)
      else
        entry.continuations << cont
        entry.results.each do |result, tree|
          cont.call(result, tree)
        end
      end
    }
  end

  def empty
    lambda { |input, cont, pos| cont.call(pos, []) }
  end

  def cons(h, t)
    lambda { |input, cont, pos|
      h.call(input, lambda { |pos1, tree1|
               t.call(input, lambda { |pos2, tree2|
                        cont.call(pos2, [tree1, tree2])
                      }, pos1)
             }, pos)
    }
  end

  def cons1(h, t)
    
  end

  def Grammar(obj)
    recurse(obj.startSymbol)
  end

  def Rule(obj)
    alts = obj.alts.map { |x| recurse(x); }
    return memo(lambda { |input, cont, pos|
      alts.each do |alt|
        alt.call(input, lambda { |pos1, tree1| 
                   cont.call(pos1, factory.Symbol(obj, tree1)) 
                 }, pos)
      end
    })
  end

  def Alt(obj)
    elts = obj.elements.map { |x| recurse(x); }
    seq = elts.inject(empty) { |cur,x| cons(x, cur) }
    return lambda { |input, cont, pos|
      seq.call(input, lambda { |pos1, tree|
                 cont.call(pos1, factory.Appl(obj, tree))
               }, pos)
    }
  end

  def Element(obj)
    sym = recurse(obj.symbol);
    return lambda { |input, cont, pos|
      sym.call(input, lambda { |pos1, tree|
                 cont.call(pos1, factory.Arg(obj.label, tree))
               }, pos)
    }
  end

  def Opt(obj)
    arg = recurse(obj.arg)
    lambda { |input, cont, pos|
      cont.call(pos, nil)
      arg.call(input, cont, pos)
    }
  end

  def IterStar(obj)
    iter = Iter(obj)
    lambda { |input, cont, pos|
      cont.call(pos, [])
      iter.call(input, cont, pos)
    end
  end

  def Iter(obj)
    base = recurse(obj.sym)
    me = nil
    me = lambda { |input, cont, pos|
      sym.call(input, cont, pos)
      cons(sym, me).call(input, cont, pos)
    }
  end

  def Id(obj)
    nonReserved(obj, Regexp.new(IDPATTERN))
  end

  def Key(obj)
    nonReserved(obj, Regexp.new(IDPATTERN))
  end
		
  def Ref(obj)
    nonReserved(obj, Regexp.new("(#{IDPATTERN})(\\.#{IDPATTERN})*"))
  end


  def Int(obj)
    token(obj, /[-+]?[0-9]+/)
  end

  def Str(obj)
    # todo: unescaping
    token(obj, /"(\\\\.|[^"])*"/)
  end

  def SqStr(obj)
    # todo: unescaping
    token(obj, /'(\\\\.|[^'])*'/)
  end
  
  def Real(obj)
    token(obj, /[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?/)
  end

  def Bool(obj)
    token(obj, /true|false/)
  end

  def Lit(obj)
    token(obj, Regexp.new(Regexp.escape(obj.value)))
  end

  def CiLit(obj)
    token(obj, Regexp.new(ciPattern(obj.value)))
  end
  
  def Sym(obj)
    recurse(obj.rule)
  end		
end

parser = PGen.new(grammarGrammar).run
