
require 'grammar/grammargen'

class GrammarGrammar < GrammarGenerator

  start Grammar

  rule Grammar do
    alt [:Grammar], "grammar", {:name => :id}, "start", {:start => ref(Rule)}, {rules: iter_star(Rule)}
  end

  rule Rule do
    alt [:Rule], {:name => :key}, "::=", {:alts => iter_sep(Sequence, "|")}
  end

  rule Sequence do
    alt [:Sequence], {:sequence => iter_star(Pattern)}
    alt [:Create], "[", {:name => :str}, "]", {:sequence => iter_star(Pattern)}
  end

  rule Pattern do
    alt [:Int], "int"
    alt [:Str], "str"
    alt [:Sqstr], "sqstr"
    alt [:Real], "real"
    alt [:Bool], "bool"
    alt [:Id], "id"
    alt [:Key], "key"
    alt [:Field], {:name => :id}, {:arg => call(Pattern)}
    alt [:Ref], {:ref => :id}, "^"
    alt [:Lit], {:value => :str}
    alt [:CiLit], {:value => :str}
    alt [:Call], {:rule => ref(Rule)}
    alt [:Opt], {:arg => ref(Rule)}, "?"
    alt [:Iter], {:arg => ref(Rule)}, "+"
    alt [:IterStar], {:arg => ref(Rule)}, "*"
    alt [:IterSep], "{", {:arg => ref(Rule)}, {:sep => :str}, "}", "+"
    alt [:IterStarSep], "{", {:arg => ref(Rule)}, {:sep => :str}, "}", "*"
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

