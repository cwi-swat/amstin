
require 'grammar/grammargen'

class GrammarGrammar < GrammarGenerator

  start Grammar

  rule Grammar do
    alt [:Grammar], "grammar", {name: :sym}, "start", {start: ref(Rule)}, {rules: iter_star(Rule)}
  end

  rule Rule do
    alt [:Rule], {name: :key}, "::=", {alts: Alts}
  end

  rule Alts do
    alt [:Alts], {alts: iter_sep(Create, "|")}
  end

  rule Create do
    alt [:Create], "[", {name: :sym}, "]", {arg: Sequence}
    alt Sequence
  end

  rule Sequence do
    alt [:Sequence], {elements: iter_star(Field)}
  end

  rule Field do
    alt [:Field], {name: :sym}, ":", {arg: Pattern}
    alt Pattern
  end

  rule Pattern do
    alt [:Value], "int" # todo: add init code when it is parsed x.kind = "Int"
    alt [:Value], "str" 
    alt [:Value], "sqstr"
    alt [:Value], "real" 
    alt [:Value], "bool" 
    alt [:Value], "sym" 

    alt [:Key], "key"

    alt [:Ref], {ref: :sym}, "^"

    alt [:Lit], {value: :str} # todo: add init code x.case_sensitive = true

    alt [:Lit], {value: :sqstr}

    alt [:Call], {rule: ref(Rule)}

    alt [:Regular], {arg: Pattern}, "?" # todo add init code optional, many etc.

    alt [:Regular], {arg: Pattern}, "+" 

    alt [:Regular], {arg: Pattern}, "*" 

    alt [:Regular], "{", {arg: Pattern}, {sep: :str}, "}", "+" 

    alt [:Regular], "{", {arg: Pattern}, {sep: :str}, "}", "*" 
    
    alt "(", Alts, ")"
  end
end


if __FILE__ == $0 then
  require 'schema/schemaschema'
  require 'tools/print'
  require 'tools/copy'
  require 'schema/factory'

  Print.recurse(GrammarGrammar.grammar, GrammarSchema.print_paths)
  
  G = Copy.new(Factory.new(GrammarSchema.schema)).copy(GrammarGrammar.grammar)
  
  Print.recurse(G, GrammarSchema.print_paths)

end

