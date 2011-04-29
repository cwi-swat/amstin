

require 'schema/schemagen'

class SchemaSchema < SchemaGenerator

  primitive :str
  primitive :int
  primitive :bool
  primitive :real

  # this is a little model that describes how to print out schema schemas
  # perhaps it should be in the model itself
  def self.print_paths
    { :classes => { :fields => {} } }
  end

  klass Schema do
    field :name, :type => :str, :key => true
    field :classes, :type => Klass, :optional => true, :many => true
    field :primitives, :type => Primitive, :optional => true, :many => true
  end
    
  klass Type do
  end

  klass Primitive, :super => Type do
    field :name, :type => :str, :key => true
  end

  klass Klass, :super => Type do
    field :schema, :type => Schema, :inverse => Schema.classes
    field :name, :type => :str, :key => true
    field :super, :type => Klass, :optional => true
    field :subtypes, :type => Klass, :optional => true, :many => true, :inverse => Klass.super
    field :fields, :type => Field, :optional => true, :many => true
  end

  klass Field do
    field :owner, :type => Klass, :inverse => Klass.fields, :inverse => Klass.fields
    field :name, :type => :str, :key => true
    field :type, :type => Type
    field :optional, :type => :bool
    field :many, :type => :bool
    field :key, :type => :bool
    field :inverse, :type => Field, :optional => true, :inverse => Field.inverse
  end

  SchemaSchema.finalize(schema)
  
end

def main
  require 'tools/print'
  
  Print.new.recurse(SchemaSchema.schema, SchemaSchema.print_paths)
end

if __FILE__ == $0 then
  main
end
