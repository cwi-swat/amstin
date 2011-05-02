


require 'test/unit'

require 'grammar/cpsparser'
require 'grammar/parsetree'
require 'grammar/grammargrammar'
require 'grammar/unparse'

class ParseTest < Test::Unit::TestCase

  def test_parse_unparse
    grammar = GrammarGrammar.grammar
    grammargrammar = 'grammar/grammar.grammar'
    src = File.read(grammargrammar)
    tree = CPSParser.parse(grammargrammar, grammar)
    s = Unparse.unparse(grammar, tree)
    assert_equal(src, s, "unparse not the same as input source")
  end
 
end
