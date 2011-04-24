
require 'schema/schemamodel'
require 'schema/factory'
require 'grammar/grammarschema'

class GrammarGenerator

  THE_SCHEMA = GrammarSchema.schema
  Factory = Factory.new(GrammarSchema.schema)
  
  @@grammars = {}

  def self.class_for(name)
    klass = THE_SCHEMA.classes[name]
    raise "Unknown class #{name}" unless klass
    return klass
  end

  TOKENS = {
    :str => Factory.Str(),
    :int => Factory.Int(),
    :real => Factory.Real(),
    :id => Factory.Id(),
    :key => Factory.Key(),
    :sqstr => Factory.Sqstr()
  }

  def self.inherited(subclass)
    g = Factory.Grammar(subclass.to_s)
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
      grammar.rules << r
      @@current = r
      yield
    end

    def alt(*elts)
      if elts[0].is_a?(Array)
        a = Factory.Create(elts.shift.first.to_s)
      else
        a = Factory.Sequence()
      end
      elts.each do |e|
        if e.is_a?(String) then
          a.elements << Factory.Lit(e)
        elsif e.is_a?(Hash) then
          e.each do |k, v|
            a.elements << Factory.Field(k.to_s, make_symbol(v))
          end
        else
          a.elements << make_symbol(e)
        end
      end
      @@current.alts << a
    end
    
    def make_symbol(e)
      if e.is_a?(Symbol)
        r = TOKENS[e]
        raise "Unrecognized grammar symbol #{e}" unless r
        return r
      end
      return e
    end
    
    def call(r)
      Factory.Call(r)
    end
    
    def ref(r)
      Factory.Ref(r.name)
    end

    def iter(sym)
      Factory.Iter(sym)
    end
    
    def iter_star(sym)
      Factory.IterStar(sym)
    end
    
    def iter_sep(sym, sep)
      Factory.IterSep(sym, sep)
    end

    def iter_star_sep(sym, sep)
      Factory.IterStarSep(sym, sep)
    end
    
    def opt(sym)
      Factory.Opt(sym)
    end  

    def cilit(s)
      # todo
    end
      
    def const_missing(name)
      get_rule(name.to_s)
    end

    def get_rule(name)
      m = grammar.rules[name]
      if !m
        m = Factory.Rule(name)
        grammar.rules << m
      end
      return m
    end
      
  end
end
