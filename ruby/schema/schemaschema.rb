

require 'schema/schemagen'

class SchemaSchema < SchemaGenerator
  primitive :str
  primitive :int
  primitive :bool

  klass Schema do
    field :name, :type => :str
    field :classes, :type => Klass, :optional => true, :many => true, :inverse => Klass.schema
    field :primitives, :type => Primitive, :optional => true, :many => true
  end
    
  klass Type do
  end

  klass Primitive, :super => Type do
    field :name, :type => :str
  end

  klass Klass, :super => Type do
    field :name, :type => :str
    field :super, :type => Klass, :optional => true, :inverse => Klass.subtypes
    field :subtypes, :type => Klass, :optional => true, :many => true
    field :fields, :type => Field, :optional => true, :many => true, :inverse => Field.owner
    field :schema, :type => Schema
  end

  klass Field do
    field :owner, :type => Klass, :inverse => Klass.fields
    field :name, :type => :str
    field :type, :type => Type
    field :optional, :type => :bool
    field :many, :type => :bool
    field :inverse, :type => Field, :optional => true, :inverse => Field.inverse
  end

  schema.metaclass = Schema.klass
  schema.primitives.each do |p|
    p.metaclass = Primitive.klass # unfortunate .klass because of wrapping
  end
  schema.classes.each do |c|
    c.metaclass = Klass.klass
    c.fields.each do |f|
      f.metaclass = Field.klass
    end
  end
  
end
