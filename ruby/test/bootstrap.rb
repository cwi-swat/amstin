

require 'test/unit'

require 'grammar/cpsparser'
require 'grammar/parsetree'
require 'grammar/grammargrammar'
require 'grammar/instantiate'

require 'tools/equals'
require 'tools/diff'


class BootstrapTests < Test::Unit::TestCase

  def test_grammar_grammar
    grammar = GrammarGrammar.grammar
    grammar2 = CPSParser.load('grammar/grammar.grammar', grammar, GrammarSchema.schema)

    assert_equal([], Diff.diff(grammar, grammar2),
           "parsed grammar.grammar != bootstrap grammargrammar")
    assert_not_equal([], Diff.diff(GrammarSchema.schema, SchemaSchema.schema),
           "parsed grammar.grammar != bootstrap grammargrammar")

    tree2 = CPSParser.parse(grammargrammar, grammar2)
    grammar3 = inst.run(tree2)
    assert_equal([], Diff.diff(grammar, grammar3),
           "parsed grammar.grammar using itself != bootstrap grammar")

    tree3 = CPSParser.parse(grammargrammar, grammar3)
    grammar4 = inst.run(tree3)

    assert_equal([], Diff.diff(grammar, grammar4),
           "parsed grammar.grammar using itself from itself != bootstrap grammar")
  end
  
  def test_schema_grammar1
    grammar = GrammarGrammar.grammar
    grammar2 = CPSParser.load('schema/schema.grammar', grammar, GrammarSchema.schema)
    schema_schema = CPSParser.load('schema/schema.schema', grammar2, SchemaSchema.schema)

    assert_equal([], Diff.diff(SchemaSchema.schema, SchemaSchema.schema),
           "Boot SchemaSchema != Boot SchemaSchema")
  end

  def test_schema_grammar2
    grammar = GrammarGrammar.grammar
    grammar2 = CPSParser.load('schema/schema.grammar', grammar, GrammarSchema.schema)
    schema_schema = CPSParser.load('schema/schema.schema', grammar2, SchemaSchema.schema)

    assert_equal([], Diff.diff(schema_schema, SchemaSchema.schema),
           "Parsed schema != Boot SchemaSchema")
  end

  def test_schema_grammar3
    grammar = GrammarGrammar.grammar
    grammar2 = CPSParser.load('schema/schema.grammar', grammar, GrammarSchema.schema)
    schema_schema = CPSParser.load('schema/schema.schema', grammar2, SchemaSchema.schema)

    assert_equal([], Diff.diff(SchemaSchema.schema, schema_schema),
           "Boot SchemaSchema != Parsed schema")
  end

  def test_schema_grammar4
    grammar = GrammarGrammar.grammar
    grammar2 = CPSParser.load('schema/schema.grammar', grammar, GrammarSchema.schema)
    schema_schema = CPSParser.load('schema/schema.schema', grammar2, SchemaSchema.schema)

    assert_equal([], Diff.diff(schema_schema, schema_schema),
           "Parsed schema != Parsed schema")
  end

  def test_parsetree_schema
    grammar = GrammarGrammar.grammar
    grammar2 = CPSParser.load('schema/schema.grammar', grammar, GrammarSchema.schema)
    pt_schema = CPSParser.load('grammar/parsetree.schema', grammar2, SchemaSchema.schema)
    p pt_schema
    assert_not_nil(pt_schema)
  end
    

end
