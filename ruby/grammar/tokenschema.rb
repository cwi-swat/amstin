

require 'schema/schemagen'
require 'schema/schemaschema'


class TokenSchema < SchemaGenerator
  primitive :str
  primitive :int
  primitive :bool

  def self.print_paths 
    {:tokens => {}}
  end

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

  SchemaSchema.finalize(schema)
end
