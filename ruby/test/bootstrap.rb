

require 'test/unit'

require 'grammar/cpsparser'
require 'grammar/parsetree'
require 'grammar/grammargrammar'
require 'grammar/instantiate'

require 'tools/equals'
require 'tools/diff'
require 'tools/print'

class BootstrapTests < Test::Unit::TestCase

  def test_grammar_grammar
    grammar = GrammarGrammar.grammar

    assert(Equals.equals(GrammarSchema.schema, grammar, grammar))
    assert_equal([], Diff.diff(grammar, grammar))

    grammargrammar = 'grammar/grammar.grammar'
    grammar2 = CPSParser.load(grammargrammar, grammar, GrammarSchema.schema)

    assert(Equals.equals(GrammarSchema.schema, grammar2, grammar2))
    assert(Equals.equals(GrammarSchema.schema, grammar, grammar2))
    assert(Equals.equals(GrammarSchema.schema, grammar2, grammar))

    assert_equal([], Diff.diff(grammar2, grammar2))
    assert_equal([], Diff.diff(grammar, grammar2))
    assert_equal([], Diff.diff(grammar2, grammar))
    
    grammar3 = CPSParser.load(grammargrammar, grammar2, GrammarSchema.schema)

    assert(Equals.equals(GrammarSchema.schema, grammar3, grammar3))
    assert(Equals.equals(GrammarSchema.schema, grammar2, grammar3))
    assert(Equals.equals(GrammarSchema.schema, grammar3, grammar2))
    assert(Equals.equals(GrammarSchema.schema, grammar3, grammar))
    assert(Equals.equals(GrammarSchema.schema, grammar, grammar3))

    assert_equal([], Diff.diff(grammar3, grammar3))
    assert_equal([], Diff.diff(grammar2, grammar3))
    assert_equal([], Diff.diff(grammar3, grammar2))
    assert_equal([], Diff.diff(grammar3, grammar))
    assert_equal([], Diff.diff(grammar, grammar3))

    grammar4 = CPSParser.load(grammargrammar, grammar3, GrammarSchema.schema)

    assert(Equals.equals(GrammarSchema.schema, grammar4, grammar4))
    assert(Equals.equals(GrammarSchema.schema, grammar4, grammar3))
    assert(Equals.equals(GrammarSchema.schema, grammar3, grammar4))
    assert(Equals.equals(GrammarSchema.schema, grammar4, grammar2))
    assert(Equals.equals(GrammarSchema.schema, grammar2, grammar4))
    assert(Equals.equals(GrammarSchema.schema, grammar4, grammar))
    assert(Equals.equals(GrammarSchema.schema, grammar, grammar4))

    assert_equal([], Diff.diff(grammar4, grammar4))
    assert_equal([], Diff.diff(grammar4, grammar3))
    assert_equal([], Diff.diff(grammar3, grammar4))
    assert_equal([], Diff.diff(grammar4, grammar2))
    assert_equal([], Diff.diff(grammar4, grammar))
    assert_equal([], Diff.diff(grammar, grammar4))
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


end
