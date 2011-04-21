
class SchemaModel < BasicObject
  @@ids = 0

  def initialize()
    @fields = {}
    @id = @@ids += 1
  end

  def method_missing(name, *args, &block)
    if (name.to_s =~ /^([a-zA-Z0-9]*)=$/)
      @fields[$1.to_sym] = args[0]
    else
      @fields[name]
    end
  end

  def to_s
    "model(#{_id})"
  end

  def _id
    return @id
  end
end

class SchemaGenerator
  class Wrap < BasicObject
    attr_reader :klass
    def initialize(m, builder)
      @klass = m
      @builder = builder
    end

    def method_missing(name)
      @builder.get_field(@klass, name.to_s)
    end
  end


  @@classes = {}
  @@primitives = {}
  @@current = nil

  def self.schema
    s = SchemaModel.new
    s.name = self.to_s
    s.classes = @@classes.values
    s.primitives = @@primitives.values
    return s
  end
    

  class << self
    def primitive(name)
      m = SchemaModel.new
      m.name = name.to_s
      @@primitives[name] = m
    end
      
    def klass(name, opts = {:super => nil}, &block)
      m = get_class(name.to_s)
      m.super = opts[:super] ? opts[:super].klass : nil
      m.super.subtypes << m if m.super
      @@current = m
      yield
    end

    def field(name, opts = {:optional => false, :many => false, :inverse => nil})
      f = get_field(@@current, name.to_s)
      t = opts[:type]
      f.type = @@primitives[t] || t.klass
      f.optional = opts[:optional]
      f.many = opts[:many]
      f.inverse = opts[:inverse]
      f.inverse.inverse = f if f.inverse
    end


    def const_missing(name)
      Wrap.new(get_class(name.to_s), self)
    end

    def get_field(klass, name)
      klass.fields.each do |f|
        return f if f.name == name
      end
      f = SchemaModel.new
      f.name = name
      klass.fields << f
      f.owner = klass
      return f
    end

    def get_class(name)
      @@classes[name] ||= SchemaModel.new
      m = @@classes[name]
      m.name = name
      m.fields ||= []
      m.subtypes ||= []
      return m
    end

  end

end

class SchemaSchema < SchemaGenerator
  primitive :str
  primitive :int
  primitive :bool

  klass(:Schema) do
    field :name, :type => :str
    field :classes, :type => Klass, :optional => true, :many => true, :inverse => Klass.schema
    field :primitives, :type => Primitive, :optional => true, :many => true
  end
    
  klass(:Type) do
  end

  klass(:Primitive, :super => Type) do
    field :name, :type => :str
  end

  klass(:Klass, :super => Type) do
    field :name, :type => :str
    field :super, :type => Klass, :optional => true, :inverse => Klass.subtypes
    field :subtypes, :type => Klass, :optional => true, :many => true
    field :fields, :type => Field, :optional => true, :many => true, :inverse => Field.owner
    field :schema, :type => Schema
  end

  klass(:Field) do
    field :owner, :type => Klass, :inverse => Klass.fields
    field :name, :type => :str
    field :type, :type => Type
    field :optional, :type => :bool
    field :many, :type => :bool
    field :inverse, :type => Field, :optional => true, :inverse => Field.inverse
  end

  schema.primitives.each do |p|
    p.instance_of = Primitive.klass # unfortunate .klass because of wrapping
  end
  schema.classes.each do |c|
    c.instance_of = Klass.klass
    c.fields.each do |f|
      f.instance_of = Field.klass
    end
  end

end


if __FILE__ == $0 then
  ss = SchemaSchema.schema
  puts "****** SCHEMA: #{ss.name} *******"
  ss.classes.each do |c|
    puts "CLASS #{c.name}  (#{c._id})"
    if c.super then
      puts "\tSuper: #{c.super.name}  (#{c.super._id})"
    end
    c.fields.each do |f|
      puts "\tFIELD #{f.name} (#{f._id})"
      puts "\t\ttype #{f.type.name} (#{f.type._id})"
      puts "\t\toptional #{f.optional}"
      puts "\t\tmany #{f.many}"
      puts "\t\tinverse #{f.inverse ? f.inverse.name : nil} (#{f.inverse ? f.inverse._id : nil})"

    end
  end
end
