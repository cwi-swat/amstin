
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
  schema.metaclass = SchemaSchema::Schema.klass
  schema.primitives.each do |p|
    p.metaclass = SchemaSchema::Primitive.klass
  end
  schema.classes.each do |c|
    c.metaclass = SchemaSchema::Klass.klass
    c.fields.each do |f|
      f.metaclass = SchemaSchema::Field.klass
    end
  end
  
end

if __FILE__ == $0 then
  ss = GrammarSchema.schema
  puts "****** SCHEMA: #{ss.name} *******"
  ss.classes.each do |c|
    puts "CLASS #{c.name}  (#{c._id})"
    if c.super then
      puts "\tSuper: #{c.super.name}  (#{c.super._id})"
    end
    c.subtypes.each do |s|
      puts "\tSubtype: #{s.name} (#{s._id})"
    end
    puts "\tInstanceof: #{c.metaclass}"
    c.fields.each do |f|
      puts "\tFIELD #{f.name} (#{f._id})"
      puts "\t\ttype #{f.type.name} (#{f.type._id})"
      puts "\t\toptional #{f.optional}"
      puts "\t\tmany #{f.many}"
      puts "\t\tinverse #{f.inverse ? f.inverse.name : nil} (#{f.inverse ? f.inverse._id : nil})"

    end
  end
end
