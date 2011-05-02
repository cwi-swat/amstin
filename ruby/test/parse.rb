


require 'test/unit'

require 'grammar/cpsparser'
require 'grammar/tokenize'
require 'grammar/tokenschema'
require 'grammar/parsetree'
require 'grammar/grammargrammar'
require 'grammar/unparse'

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

  def test_tokenize
    tokenizer = Tokenize.new
    path = 'grammar/token.grammar'
    src = File.read(token_grammar)
    input = tokenizer.tokenize(GrammarGrammar.grammar, path, src)
    token_grammar_pt = CPSParser.new(input, Factory.new(ParseTreeSchema.schema))
    token_grammar = Instantiate.new
    render = Render.new(Factory.new(LayoutSchema.schema))
    layout = render.recurse(GrammarGrammar.grammar, GrammarGrammar.grammar)
  
    puts "WIDTH = #{FormatWidth.new.recurse(layout)}"
  
    FormatChoice.new(80).run(layout)
    DisplayFormat.new($stdout).recurse(layout)
    $stdout << "\n"
  end
    

end
