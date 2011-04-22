
# incomplete & incorrect

class GrammarSchema < SchemaGenerator
  primitive :str
  primitive :int
  primitive :bool

  klass Grammar do
    field :name, :type => :str
    field :start, :type => Rule
  end

  klass Rule do
    field :name, :type => :str
    field :alts, :type => Alt, :many => true, :inverse => Alt.owner
  end

  klass Alt do
    field :label, :type => :str, :optional => true
    field :owner, :type => Rule
    field :elements, :type => Symbol, :optional => true, :many => true, :inverse => Symbol.owner
  end

  klass Symbol do
    # inheritance still?
    field :owner, :type => Alt
    field :label, :type => :str, :optional => true
  end

  klass Id, :super => Symbol {}
  klass Str, :super => Symbol {}
  klass Int, :super => Symbol {}
  klass Real, :super => Symbol {}
  klass Bool, :super => Symbol {}

  klass Ref, :super => Symbol do
    field :id, :type => :str
  end
  

end
