
require 'schema/schemaschema'
require 'schema/factory'
require 'tools/copy'
require 'cyclicmap'

class BadObject < NilClass
  def initialize(name)
    @name = name
  end
  def to_s
    "<" + @name + ">"
  end
end  

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

  def Create(from)
    recurse(from.arg)
  end
    
  def Epsilon(from)
    true
  end

  def Regular(from)
    from.optional
  end

  # symbol representing a 
  def Ref(from)
    false
  end

  def Value(from)
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
    @indent = 0
  end
  def copy(source)
    @indent += 1
    puts "#{' '*@indent}COPY #{source.to_s}"
    rule = super(source)
    if source.schema_class.name == "Rule"
      @grammar.rules << rule
    end
    @indent -= 1
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
    @grammar = @factory.Grammar(from.name)
    @copier = RuleCopy.new(@factory, @grammar)
    @grammar.start = recurse(from.start)
    raise "parse fail" unless to.rules.length > 0
  end

  def Rule(from)
    @rule_num += 1
    registerUpdate(from, @factory.Rule(from.name + @rule_num.to_s)) do |to|
      arg = recurse(from.arg)
      if arg
        to.arg = arg
        @grammar.rules << to
        to
      else
        BadObject.new("A")
      end
    end
  end

  def Alt(from)
    to = @factory.Alt()
    from.alts.each do |r|
      d = recurse(r)
      to.alts << d if d
    end
    alt.alts.empty? ? BadObject.new("B") : alt
  end

  # Dc(p;q)  ==>  if p.nullable then  Dc(p);q | Dc(q)  else  Dc(p);q
  def Sequence(from)
    n = from.elements.length
    alt = @factory.Alt()
    for i in 0...n
      first = recurse(from.elements[i])
      if first
        p = @factory.Sequence()
        p.elements << first unless first.schema_class.name == "Epsilon"
        for j in i+1...n
          p.elements << @copier.copy(from.elements[j])
        end
        alt.alts << p
      else
        break
      end
      break unless @nullable.recurse(from.elements[i])
    end
    alt.alts.empty? ? BadObject.new("C") : alt
  end
  
  # nonterminal
  def Call(from)
    d = recurse(from.rule)
    d ? @factory.Call(d) : BadObject.new("D")
  end

  def Epsilon(from)
    BadObject.new("E")
  end

  # Dc(p*) ==>  if p.nullable then Dc(p)* else Dc(p);p*
  def Regular(from)
    d = recurse(from.arg)
    BadObject.new("F") if d.nil?
    if !from.many
      return d
    else
      s = @factory.Sequence()
      s.elements << d
      s.elements << @factory.Regular(@copier.copy(from.arg), true, true)
      return s
    end
  end
  
  def Field(from)
    return recurse(from.arg)
  end

  def Create(from)
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

  def Value(from)
    if (@token.class.name == from.kind)
      return @factory.Epsilon()
    else
      return BadObject.new("G")
    end
  end

  def Key(from)
    if (@token.class.name == "id")
      return @factory.Epsilon()
    else
      return BadObject.new("H")
    end
  end

  def Lit(from)
    if (@token == from.value)
      return @factory.Epsilon()
    else
      return BadObject.new("I")
    end
  end
end


if __FILE__ == $0 then
  require 'grammar/grammargrammar'
  require 'tools/print'
 
  x = GrammarGrammar.grammar

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

  
