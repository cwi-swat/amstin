

class OptimizingFactory
  def initialize(factory)
    @factory = factory
    @epsilon = factory.Epsilon()
  end

  def Grammar(*args)
    @factory.grammar(*args)
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
  
  def Seq(left, right)
    left.nil? ? right : right.nil? ? left : @factory.Seq(left, right)
  end

  def method_missing(*args)
    @factory.send(*args)
  end
end

class Derivative < CyclicMap
  def initialize(token)
    @rule_num = 1
    @token = token
  end

  def Grammar(src)
    register(src, factory.Grammar(src.name)) do |to|
      to.start = recurse(gram.start)
      raise "parse fail" unless to.start
    end
  end

  def Rule(from)
    register(from, @factory.Rule(from.name + @rule_num++)) do |to|
      from.alts.each do |r|
        newAlt = recurse(r)
        to.alts << newAlt if newAlt
      end
    end
  end

  # nonterminal
  def Ref(from)
    factory.Ref(recurse(from.rule))
  end

  def Epsilon(from)
    nil
  end

  # Dc(p*) ==>  if p.nullable then Dc(p)* else Dc(p);p*
  def IterStar(from)
    if (CanGenerateNull.lookup(from))
      factory.IterStar(recurse(from.sub))
    else
      factory.Seq(recurse(from.sub), copy(from.sub))
  end
  
  # Dc(p?)  ==>  if Dc(p) != error then Dc(p)? else empty
  def Opt(from)
    p = recurse(from.sub)
    p ? factory.Opt(p) : factory.Epsilon()
  end
  
  # Dc(p;q)  ==>  if p.nullable then  Dc(p);q | Dc(q)  else  Dc(p);q
  def Seq(from)
    p = factory.Seq(recurse(from.left), copy(from.right))
    if (CanGenerateNull(from.left))
      factory.Alt(recurse(from.right), p)
    else
      p
    end
  end
    
  def RefSpec(from)
    return checkToken(token, IDENT, result)
  end

  def Int(from)
    return checkToken(token, INT, result)
  end

  def Real(from)
    return checkToken(token, REAL, result)
  end

  def Id(from)
    return checkToken(token, IDENT, result)
  end

  def Key(from)
    return checkToken(token, IDENT, result)
  end

  def Lit(from)
    return checkToken(token, from.value, result)
  end

  def checkToken(token, kind, result)
    if (token == kind)
      return factory.Epsilon()
    else
      return nil
    end
  end
end


if __FILE__ == $0 then
  require 'grammar/grammarschema.rb'
  require 'schema/factory'
  
  f = Factory.new(GrammarSchema.schema)
  
  G = f.Grammar()
  R = f.Rule("P", f.Seq(f.Lit("a"), f.Lit("b"))
  G.rules << R
  G.start = R
  
  
  
  