
require 'test/unit'

require 'schema/schemaschema'
require 'schema/factory'
require 'grammar/grammarschema'
require 'grammar/grammargrammar'
require 'grammar/parsetree'

require 'tools/copy'
require 'tools/equals'
require 'tools/print'

require 'diffy'

class CopyTest < Test::Unit::TestCase
  
  def test_schema_schema
    s1 = SchemaSchema.schema
    s2 = Copy.new(Factory.new(SchemaSchema.schema)).copy(s1)
    #assert(Equals.equals(SchemaSchema.schema, s1, s1))
    #assert(Equals.equals(SchemaSchema.schema, s2, s2))
    assert(Equals.equals(SchemaSchema.schema, s1, s2))
  end

  def test_parsetree_schema
    s1 = ParseTreeSchema.schema
    s2 = Copy.new(Factory.new(SchemaSchema.schema)).copy(s1)
    #assert(Equals.equals(SchemaSchema.schema, s1, s1))
    #assert(Equals.equals(SchemaSchema.schema, s2, s2))

    assert(Equals.equals(SchemaSchema.schema, s1, s2))
  end

  def test_grammar_schema
    s1 = GrammarSchema.schema
    s2 = Copy.new(Factory.new(SchemaSchema.schema)).copy(s1)
    #assert(Equals.equals(SchemaSchema.schema, s1, s1))
    #assert(Equals.equals(SchemaSchema.schema, s2, s2))

    File.open('s1.txt', 'w') do |f|
      Print.new(f).recurse(s1, SchemaSchema.print_paths)
    end
    File.open('s2.txt', 'w') do |f|
      Print.new(f).recurse(s2, SchemaSchema.print_paths)
    end
    assert(Equals.equals(SchemaSchema.schema, s1, s2))
  end

  def test_grammar_grammar
    s1 = GrammarGrammar.grammar
    s2 = Copy.new(Factory.new(GrammarSchema.schema)).copy(s1)
    #assert(Equals.equals(SchemaSchema.schema, s1, s1))
    #assert(Equals.equals(SchemaSchema.schema, s2, s2))
    assert(Equals.equals(GrammarSchema.schema, s1, s2))
  end

end
