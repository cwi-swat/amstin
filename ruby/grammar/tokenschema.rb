

require 'schema/schemagen'
require 'schema/schemaschema'


class TokenSchema < SchemaGenerator
  primitive :str
  primitive :int
  primitive :bool

  klass Stream do
    field :path, :type => :str
    field :tokens, :type => Token, :optional => true, :many => true, :inverse => Token.stream
    field :layout, :type => :str
  end
    
  klass Token do
    field :stream, :type => Stream
    field :line, :type => :int
    field :start, :type => :int
    field :end, :type => :int
    field :length, :type => :int
    field :kind, :type => :str
    field :value, :type => :str
    field :layout, :type => :str
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
