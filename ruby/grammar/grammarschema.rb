
require 'schema/schemagen'
require 'schema/schemaschema'

# incomplete & incorrect


class GrammarSchema < SchemaGenerator
  primitive :str
  primitive :int
  primitive :bool

  def self.print_paths
    { :rules => { :alts => { :elements => {} } } }
  end
  
  klass Grammar do
    field :name, :type => :str
    field :start, :type => Rule
    field :rules, :type => Rule, :many => true
  end

  klass Rule do
    field :name, :type => :str, :key => true
    field :grammar, :type => Grammar, :inverse => Grammar.rules
    field :alts, :type => Sequence, :many => true
  end

  klass Sequence do
    field :elements, :type => Pattern, :many => true
  end

  klass Create, :super => Sequence do
    field :name, :type => :str
    field :elements, :type => Pattern, :many => true
  end

  klass Pattern do
  end

  klass Field, :super => Pattern do
    field :name, :type => :str
    field :arg, :type => Pattern
  end
  
  klass Int, :super => Pattern do
  end

  klass Str, :super => Pattern do
  end

  klass Sqstr, :super => Pattern do
  end

  klass Real, :super => Pattern do
  end

  klass Bool, :super => Pattern do
  end

  klass Id, :super => Pattern do 
  end

  klass Key, :super => Pattern do 
  end

  klass Lit, :super => Pattern do
    field :value, :type => :str
  end

  klass CiLit, :super => Pattern do
    field :value, :type => :str
  end

  klass Ref, :super => Pattern do
    field :name, :type => :str
  end

  klass Opt, :super => Pattern do
    field :arg, :type => Pattern
  end

  klass Call, :super => Pattern do 
    field :rule, :type => Rule
  end

  klass Iter, :super => Pattern do
    field :arg, :type => Rule
  end

  klass IterStar, :super => Pattern do
    field :arg, :type => Rule
  end

  klass IterSep, :super => Pattern do
    field :arg, :type => Rule
    field :sep, :type => :str
  end

  klass IterStarSep, :super => Pattern do
    field :arg, :type => Rule
    field :sep, :type => :str
  end

  # this should be automatic
  schema.schema_class = SchemaSchema::Schema.klass
  schema.primitives.each do |p|
    p.schema_class = SchemaSchema::Primitive.klass
  end
  schema.classes.each do |c|
    c.schema_class = SchemaSchema::Klass.klass
    c.fields.each do |f|
      f.schema_class = SchemaSchema::Field.klass
    end
  end
  
end

if __FILE__ == $0 then

  require 'schema/schemaschema'
  require 'tools/print'
  
  Print.recurse(GrammarSchema.schema, SchemaSchema.print_paths)
  
end
