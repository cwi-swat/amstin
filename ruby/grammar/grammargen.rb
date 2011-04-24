
require 'schema/schemamodel'
require 'grammar/grammarschema'

class GrammarGenerator

  THE_SCHEMA = GrammarSchema.schema

  @@grammars = {}

  def self.class_for(name)
    THE_SCHEMA.classes[name]
  end


  TOKENS = {}
  [:str, :int, :bool, :real, :id, :sqstr].each do |x|
    TOKENS[x] = SchemaModel.new
    TOKENS[x].schema_class = class_for(x.to_s.capitalize)
  end

  def self.inherited(subclass)
    g = SchemaModel.new
    g.schema_class = class_for("Grammar")
    g.name = subclass.to_s
    g.rules = ValueHash.new
    g.start = nil
    @@grammars[subclass.to_s] = g
  end


  def self.grammar
    @@grammars[self.to_s]
  end

  class << self

    def start(r)
      grammar.start = r
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
      a.schema_class = class_for("Pattern")
      a.elements = elts.collect do |e|
        s = SchemaModel.new
        if e.is_a?(Hash) then
          s.label = e.keys.first
          s.symbol = make_symbol(e.values.first)
        else
          s.label = nil
          s.symbol = make_symbol(e)
        end
        s.schema_class = class_for("Element")
        s.owner = a
        s
      end
    end
    
    def make_symbol(e)
      if e.is_a?(Symbol)
        TOKENS[e]
      else
        e
      end
    end
    
    def lit(s)
      m = SchemaModel.new
      m.schema_class = class_for("Lit")
      m.value = s
      return m
     end
   
    def call(r)
      m = SchemaModel.new
      m.schema_class = class_for("Call")
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
      m = SchemaModel.new
      m.schema_class = class_for(type)
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

    def get_rule(name)
      grammar.rules[name] ||= SchemaModel.new
      m = grammar.rules[name]
      m.schema_class = class_for("Rule")
      m.name = name
      m.alts ||= []
      return m
    end
      
  end
end
