
require 'schema/schemagen'
require 'schema/schemaschema'

class ParseTreeSchema < SchemaGenerator
  primitive :str
  primitive :int
  primitive :bool

  def self.print_paths
    { :top => { :args => { } } }
  end
  
  klass ParseTree do
    field :filename, :type => :str
    field :layout_before, :type => :str
    field :layout_after, :type => :str
    field :top, :type => Tree
  end

  klass Tree do
  end

  klass Appl, :super => Tree do
    field :rule, :type => :str
    field :arg, :type => Tree
  end

  klass Sequence, :super => Tree do
    field :args, :type => Tree, :optional => true, :many => true
  end

  klass Create, :super => Tree do
    field :name, :type => :str
    field :arg, :type => Tree
  end

  klass Field, :super => Tree do
    field :name, :type => :str
    field :arg, :type => Tree
  end

  klass Value, :super => Tree do
    field :kind, :type => :str
    field :value, :type => :str
  end

  klass Lit, :super => Tree do
    field :value, :type => :str
    field :case_sensitive, :type => :bool
  end

  klass Ref, :super => Tree do
    field :name, :type => :str
  end

  klass Key, :super => Tree do
    field :name, :type => :str
  end

  klass Regular, :super => Tree do
    field :args, :type => Tree, :optional => true, :many => true
    field :many, :type => :bool
    field :optional, :type => :bool
    field :sep, :type => :str, :optional => true
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
  require 'tools/print'
  Print.recurse(ParseTreeSchema.schema, SchemaSchema.print_paths)
end
