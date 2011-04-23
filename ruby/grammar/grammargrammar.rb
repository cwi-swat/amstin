
require 'grammar/grammargen'

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
    alt :Sqstr, lit("sqstr")
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


if __FILE__ == $0 then
  require 'grammar/grammarschema'
  require 'schema/checkschema'
  
  c = Conformance.new
  c.recurse(GrammarSchema.schema, GrammarGrammar.grammar)
  c.errors.each do |x|
    puts x
  end
end
