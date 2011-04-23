
require 'schema/schemamodel'
require 'grammar/grammarschema'

class GrammarGenerator

  @@schema = GrammarSchema.schema
  @@grammar = SchemaModel.new
  @@rules = {}
  @@tokens = {}
  @@start = nil

  def self.class_for(name)
    @@schema.classes.find { |c| 
      c.name == name 
    }
  end
  
  def self.grammar
    @@grammar.metaclass = class_for("Grammar")
    @@grammar.rules = @@rules.values
    @@grammar.name = self.to_s
    return @@grammar
  end

  class << self

    def start(r)
      @@grammar.start = r
    end

    def rule(r)
      @@current = r
      yield
    end

    def alt(label, *elts)
      a = SchemaModel.new
      @@current.alts << a
      a.label = label.to_s
      a.owner = @@current
      a.elements = elts.collect do |e|
        s = SchemaModel.new
        if e.is_a?(Hash) then
          s.label = e.keys.first
          s.symbol = make_symbol(e.values.first)
        else
          s.label = nil
          s.symbol = make_symbol(e)
        end
      end
    end
    
    def make_symbol(e)
      if e.is_a?(Symbol)
        get_token(e)
      else
        e
      end
    end
    
    def lit(s)
      m = SchemaModel.new
      m.metaclass = class_for("Lit")
      m.value = s
      return m
    end
   
    def call(r)
      m = SchemaModel.new
      m.metaclass = class_for("Call")
      m.rule = r
      return m
    end

    def iter(sym)
      regular(sym, "Iter")
    end
    
    def iter_star(sym)
      regular(sym, "IterStar")
    end
    
    def iter_sep(sym, sep)
      regular(sym, "IterSep", sep)
    end

    def iter_star_sep(sym, sep)
      regular(sym, "IterStarSep", sep)
    end
    
    def opt(sym)
      regular(sym, "Opt")
    end
    
    def regular(sym, type, sep = nil)
      c = class_for(type)
      m = SchemaModel.new
      m.metaclass = c
      m.arg = sym
      m.sep = sep if sep
      return m
    end
      

    def cilit(s)
      # todo
    end
      
    def const_missing(name)
      get_rule(name.to_s)
    end

    def get_token(name)
      @@tokens[name] ||= SchemaModel.new
      m = @@tokens[name]
      m.metaclass = class_for(name.to_s.upcase)
      return m
    end
      

    def get_rule(name)
      @@rules[name] ||= SchemaModel.new
      m = @@rules[name]
      m.metaclass = class_for("Rule")
      m.name = name
      m.alts ||= []
      return m
    end
      
  end
  
  
end

class GrammarGrammar < GrammarGenerator
  start Grammar

  rule Grammar do
    alt :Grammar,  lit("grammar"), {name: :str}, lit("start"), {startSymbol: call(Rule)}, {rules: iter_star(Rule)}
  end

  rule Rule do
    alt :Rule, {name: :key}, lit("::="), {alts: iter_sep(Pattern, "|")}
  end

  rule Pattern do
    alt :Pattern, lit("["), {label: :id}, lit("]"), {elements: iter_star(Element)}
    alt :Pattern, {elements: iter_star(Element)}
  end

  rule Element do
    alt :Element, {symbol: Sym}
    alt :Element, {label: :id}, lit(":"), {symbol: Sym}
  end

  rule Sym do
    alt :Int, lit("int")
    alt :Str, lit("str")
    alt :SqStr, lit("sqstr")
    alt :Real, lit("real")
    alt :Bool, lit("bool")
    alt :Id, lit("id")
    alt :Key, lit("key")
    alt :Ref, {ref: :id}, lit("^")
    alt :Lit, {value: :str}
    alt :CiLit, {value: :str}
    alt :Call, {rule: call(Rule)}
    alt :Opt, {arg: Sym}, lit("?")
    alt :Iter, {arg: Sym}, lit("+")
    alt :IterStar, {arg: Sym}, lit("*")
    alt :IterSep,  lit("{"), {arg: Sym}, {sep: :str}, lit("}"), lit("+")
    alt :IterStarSep, lit("{"), {arg: Sym}, {sep: :str}, lit("}"), lit("*")
  end
end


