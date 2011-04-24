
require 'schema/schemagen'
require 'schema/schemaschema'

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
    field :alts, :type => Pattern, :many => true, :inverse => Pattern.owner
  end

  klass Pattern do
    field :label, :type => :str, :optional => true
    field :owner, :type => Rule
    field :elements, :type => Element, :optional => true, :many => true, :inverse => Element.owner
  end

  klass Element do
    field :symbol, :type => Sym
    field :label, :type => :str, :optional => true
    field :owner, :type => Pattern
  end
  
  klass Sym do
  end

  klass Int, :super => Sym do
  end

  klass Str, :super => Sym do
  end

  klass Sqstr, :super => Sym do
  end

  klass Real, :super => Sym do
  end

  klass Bool, :super => Sym do
  end

  klass Id, :super => Sym do 
  end

  klass Ref, :super => Sym do
    field :ref, :type => :str
  end

  klass Lit, :super => Sym do
    field :value, :type => :str
  end

  klass CiLit, :super => Sym do
    field :value, :type => :str
  end

  klass Call, :super => Sym do 
    field :rule, :type => Rule
  end


  klass Opt, :super => Sym do
    field :arg, :type => Sym
  end

  klass Iter, :super => Sym do
    field :arg, :type => Sym
  end

  klass IterStar, :super => Sym do
    field :arg, :type => Sym
  end

  klass IterSep, :super => Sym do
    field :arg, :type => Sym
    field :sep, :type => :str
  end

  klass IterStarSep, :super => Sym do
    field :arg, :type => Sym
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
