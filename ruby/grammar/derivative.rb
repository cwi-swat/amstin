
require 'schema/schemaschema'
require 'schema/factory'
require 'tools/copy'
require 'cyclicmap'

class OptimizingGrammarFactory
  def initialize
    @factory = Factory.new(GrammarSchema.schema)
    @epsilon = @factory.Epsilon()
  end

  def Grammar(*args)
    @factory.Grammar(*args)
  end

  def Rule(sub)
    @factory.Rule(sub) if sub 
  end

  def IterStar(sub)
    @factory.IterStar(sub) if sub
  end
  
  def Opt(sub)
    @factory.Opt(sub) if sub
  end
  
  def method_missing(*args)
    @factory.send(*args)
  end
end

class NullTest < CyclicMapNew
  def recurse(obj)
    x = @memo[obj]
    return x unless x.nil?
    @memo[obj] = false  # TODO: wrong
    x = send(obj.schema_class.name, obj)
    @memo[obj] = x
    x
  end

  def Rule(from)
    registerUpdate(from, false) do |result|
      from.alts.each do |a|
        result = true if recurse(a)
      end
      result
    end
  end

  def Sequence(from)
    from.elements.each do |e|
      return false unless recurse(e)
    end
    true
  end

  def Field(from)
    recurse(from.arg)
  end
  
  def Epsilon(from)
    true
  end

  def IterStar(from)
    true
  end
  
  def Opt(from)
    true
  end
  
  def Int(from)
    false
  end

  def Real(from)
    false
  end

  # symbol representing a 
  def Ref(from)
    false
  end

  def Id(from)
    false
  end

  def Key(from)
    false
  end

  def Lit(from)
    false
  end
end

class RuleCopy < Copy
  def initialize(factory, grammar)
    super(factory)
    @grammar = grammar
  end
  def copy(source)
    rule = super(source)
    if source.schema_class.name == "Rule"
      @grammar.rules << rule
    end
    return rule
  end
end

class Derivative < CyclicMapNew
  def initialize(token)
    super()
    @rule_num = 1
    @token = token
    @factory = OptimizingGrammarFactory.new    
    @nullable = NullTest.new()
  end

  def Grammar(from)
    register(from, @factory.Grammar(from.name)) do |to|
      @grammar = to
      @copier = RuleCopy.new(@factory, @grammar)
      to.start = recurse(from.start)
      raise "parse fail" unless to.rules.length > 0
    end
  end

  def Rule(from)
    @rule_num += 1
    register(from, @factory.Rule(from.name + @rule_num.to_s)) do |to|
      from.alts.each do |r|
        recurse(r)
      end
      if to.alts.length > 0
        @grammar.rules << to
      end
    end
  end

  # Dc(p;q)  ==>  if p.nullable then  Dc(p);q | Dc(q)  else  Dc(p);q
  def Sequence(from)
    n = from.elements.length
    toRule = recurse(from.rule) # need the inverse here!
    for i in 0...n
      first = recurse(from.elements[i])
      if first
        p = @factory.Sequence()
        p.elements << first unless first.schema_class.name == "Epsilon"
        for j in i+1...n
          p.elements << @copier.copy(from.elements[j])
        end
        toRule.alts << p
      end
      break unless @nullable.recurse(from.elements[i])
    end
    
  end
  
  # nonterminal
  def Call(from)
    @factory.Call(recurse(from.rule))
  end

  def Epsilon(from)
    nil
  end

  # Dc(p*) ==>  if p.nullable then Dc(p)* else Dc(p);p*
  def IterStar(from)
    s = @factory.Sequence()
    s.elements << recurse(from.arg)
    s.elements << @copier.copy(from.arg)
    r = @factory.Rule("S" + @rule_num.to_s)
    r.alts << s
    @grammar.rules << r
    @factory.Opt(@factory.Call(r))
  end
  
  def Field(from)
    return recurse(from.arg)
  end
  
  # Dc(p?)  ==>  if Dc(p) != error then Dc(p)? else empty
  def Opt(from)
    p = recurse(from.arg)
    p ? @factory.Opt(p) : @factory.Epsilon()
  end
  
  # identifier representing a remote object
  def Ref(from)
    return checkToken(Symbol)
  end

  def Int(from)
    return checkToken(Integer)
  end

  def Real(from)
    return checkToken(Float)
  end

  def Id(from)
    return checkToken(Symbol)
  end

  def Key(from)
    return checkToken(Symbol)
  end

  def Lit(from)
    if (@token == from.value)
      return @factory.Epsilon()
    else
      return nil
    end
  end

  def checkToken(kind)
    if (@token.class == kind)
      return @factory.Epsilon()
    else
      return nil
    end
  end
end


if __FILE__ == $0 then
  require 'grammar/grammargrammar'
  require 'tools/print'
 
  x = GrammarGrammar.grammar
  Print.recurse(x, GrammarSchema.print_paths)

  #y = Derivative.new("foo").recurse(x)
  #Print.recurse(y, GrammarSchema.print_paths)
  
  x = Derivative.new("grammar").recurse(x)
  x = Derivative.new(:foo).recurse(x)
  x = Derivative.new("start").recurse(x)
  x = Derivative.new(:bar).recurse(x)

  x = Derivative.new(:r1).recurse(x)
  x = Derivative.new(":=").recurse(x)
  x = Derivative.new("test").recurse(x)
  x = Derivative.new("*").recurse(x)
  
  Print.recurse(x, GrammarSchema.print_paths)
    
end

  
