

require 'test/unit'

require 'grammar/cpsparser'
require 'grammar/tokenize'
require 'grammar/parsetree'
require 'grammar/grammargrammar'
require 'grammar/instantiate'

require 'tools/equals'
require 'tools/diff'


class BootstrapTests < Test::Unit::TestCase

  def parse(path, grammar)
    tokenizer = Tokenize.new
    input = tokenizer.tokenize(grammar, path, File.read(path)) 
    parse = CPSParser.new(input, Factory.new(ParseTreeSchema.schema))
    parse.run(grammar)
  end


  def test_grammar_grammar
    grammar = GrammarGrammar.grammar
    grammargrammar = 'grammar/grammar.grammar'
    tree = parse(grammargrammar, grammar)
    inst = Instantiate.new(Factory.new(GrammarSchema.schema))
    grammar2 = inst.run(tree)
    assert_equal([], Diff.diff(grammar, grammar2),
           "parsed grammar.grammar != bootstrap grammargrammar")
    assert_not_equal([], Diff.diff(GrammarSchema.schema, SchemaSchema.schema),
           "parsed grammar.grammar != bootstrap grammargrammar")

    tree2 = parse(grammargrammar, grammar2)
    grammar3 = inst.run(tree2)
    assert_equal([], Diff.diff(grammar, grammar3),
           "parsed grammar.grammar using itself != bootstrap grammar")

    tree3 = parse(grammargrammar, grammar3)
    grammar4 = inst.run(tree3)

    assert_equal([], Diff.diff(grammar, grammar4),
           "parsed grammar.grammar using itself from itself != bootstrap grammar")
  end
  
  def test_schema_grammar
    grammar = GrammarGrammar.grammar
    tree = parse('schema/schema.grammar', grammar)
    inst = Instantiate.new(Factory.new(GrammarSchema.schema))
    grammar2 = inst.run(tree)

    tree = parse('schema/schema.schema', grammar2)

    inst2 = Instantiate.new(Factory.new(SchemaSchema.schema))
    schema_schema = inst2.run(tree)
    schema_schema.finalize

    assert_equal([], Diff.diff(SchemaSchema.schema, SchemaSchema.schema),
           "Boot SchemaSchema != Boot SchemaSchema")
    assert_equal([], Diff.diff(schema_schema, SchemaSchema.schema),
           "Parsed schema != Boot SchemaSchema")
    assert_equal([], Diff.diff(SchemaSchema.schema, schema_schema),
           "Boot SchemaSchema != Parsed schema")
    assert_equal([], Diff.diff(schema_schema, schema_schema),
           "Parsed schema != Parsed schema")
  end


end
