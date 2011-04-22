

require 'schema/schemagen'


class TokenSchema < SchemaGenerator
  primitive :str
  primitive :int
  primitive :bool

  klass Stream do
    field :path, :type => :str
    field :tokens, :type => Token, :optional => true, :many => true, :inverse => Token.stream
  end
    
  klass Token do
    field :stream, :type => Stream
    field :line, :type => :int
    field :start, :type => :int
    field :end, :type => :int
    field :length, :type => :int
    field :type, :type => Type
    field :value, :type => :str
  end

  klass Type do
  end

  klass Str, :super => Type do
  end

  klass Int, :super => Type do
  end

  klass Bool, :super => Type do
  end

  klass Lit, :super => Type do
  end

  klass Id, :super => Type do
  end

end
