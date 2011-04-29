

require 'test/unit'

require 'grammar/cpsparser'
require 'grammar/tokenize'
require 'grammar/parsetree'
require 'grammar/grammargrammar'
require 'grammar/instantiate'

require 'tools/equals'


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
    assert(Equals.equals(GrammarSchema.schema, grammar, grammar2),
           "parsed grammar.grammar != bootstrap grammargrammar")

    tree2 = parse(grammargrammar, grammar2)
    grammar3 = inst.run(tree2)
    assert(Equals.equals(GrammarSchema.schema, grammar, grammar3),
           "parsed grammar.grammar using itself != bootstrap grammar")

    tree3 = parse(grammargrammar, grammar3)
    grammar4 = inst.run(tree3)

    assert(Equals.equals(GrammarSchema.schema, grammar, grammar4),
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

    assert(Equals.equals(SchemaSchema.schema, SchemaSchema.schema, SchemaSchema.schema),
           "schemaschema != schemaschema according to schemaschema")
    assert(Equals.equals(SchemaSchema.schema, schema_schema, SchemaSchema.schema),
           "schema.schema != schemaschema according to schemaschema")
    assert(Equals.equals(SchemaSchema.schema, schema_schema, schema_schema),
           "schema.schema != schema.schema according to schemaschema")
    assert(Equals.equals(schema_schema, schema_schema, SchemaSchema.schema),
           "schema.schema != schemaschema according to schema.schema")
    assert(Equals.equals(schema_schema, schema_schema, schema_schema),
           "schema.schema != schema.schema according to schema.schema")
  end


end
