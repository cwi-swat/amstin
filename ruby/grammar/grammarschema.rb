
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
    # do NOT define an inverse here for rules
    field :arg, :type => Expression
  end

  klass Expression do
  end
    
  klass Epsilon, :super => Expression do
  end

  klass Alt, :super => Expression do
    field :alts, :type => Expression, :many => true
  end

  klass Sequence, :super => Expression do
    field :elements, :type => Expression, :optional => true, :many => true
  end

  klass Create, :super => Expression do
    field :name, :type => :str
    field :arg, :type => Expression
  end

  klass Field, :super => Expression do
    field :name, :type => :str
    field :arg, :type => Expression
  end
  
  klass Value, :super => Expression do
    field :kind, :type => :str
  end

  klass Key, :super => Expression do 
  end

  klass Ref, :super => Expression do
    field :name, :type => :str
  end

  klass Lit, :super => Expression do
    field :value, :type => :str
    field :case_sensitive, :type => :bool
  end

  klass Call, :super => Expression do 
    field :rule, :type => Rule
  end

  klass Regular, :super => Expression do
    field :arg, :type => Expression
    field :many, :type => :bool
    field :optional, :type => :bool
    field :sep, :type => Lit, :optional => true
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
