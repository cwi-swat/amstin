


require 'test/unit'

require 'grammar/cpsparser'
require 'grammar/tokenize'
require 'grammar/parsetree'
require 'grammar/grammargrammar'
require 'grammar/unparse'

require 'diffy'

class ParseTest < Test::Unit::TestCase

  def parse(path, grammar, string = File.read(path))
    tokenizer = Tokenize.new
    input = tokenizer.tokenize(grammar, path, string)
    parse = CPSParser.new(input, Factory.new(ParseTreeSchema.schema))
    parse.run(grammar)
  end


  def test_parse_unparse
    grammar = GrammarGrammar.grammar
    grammargrammar = 'grammar/grammar.grammar'
    src = File.read(grammargrammar)
    tree = parse(grammargrammar, grammar, src)
    s = Unparse.run(grammar, tree)
    #puts Diffy::Diff.new(src, s)
    assert_equal(src, s, "unparse not the same as input source")
  end

end
