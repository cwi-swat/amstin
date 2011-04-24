
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
    field :rules, :type => Rule
  end

  klass Rule do
    field :name, :type => :str, :key => true
    field :grammar, :type => Grammar, :inverse => Grammar.rules
    field :alts, :type => Sym, :many => true
  end

  klass Sym do
  end

  klass Create, :super => Sym do
    field :label, :type => :str
    field :arg, :type => Atom
  end

  klass Field, :super => Sym do
    field :label, :type => :str
    field :arg, :type => Atom
  end
  
  klass Atom, :super => Sym do
  end
  
  klass Int, :super => Atom do
  end

  klass Str, :super => Atom do
  end

  klass Sqstr, :super => Atom do
  end

  klass Real, :super => Atom do
  end

  klass Bool, :super => Atom do
  end

  klass Id, :super => Atom do 
  end

  klass Lit, :super => Atom do
    field :value, :type => :str
  end

  klass CiLit, :super => Atom do
    field :value, :type => :str
  end

  klass Ref, :super => Sym do
    field :ref, :type => :str
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
