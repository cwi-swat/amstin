
require 'schema/schemamodel'

class ValueHash < Hash
  def each(&block)
    values.each &block
  end
  def each_with_index(&block)
    values.each_with_index &block
  end
end

class SchemaGenerator
  ## NB: to use this schemagenerator, be careful with names of classes
  ## defined using klass(): if you use a name that collides with any name
  ## included in Kernel, it'll break. 
  ## Todo: fix this?

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


  @@schema = SchemaModel.new
  @@classes = ValueHash.new
  @@primitives = ValueHash.new
  @@current = nil

  def self.schema
    @@schema.name = self.to_s
    @@schema.classes = @@classes
    @@schema.primitives = @@primitives
    return @@schema
  end
    

  class << self
    def primitive(name)
      m = SchemaModel.new
      m.name = name.to_s
      @@primitives[name] = m
    end
      
    def klass(wrapped, opts = {}, &block)
      m = wrapped.klass
      m.super = opts[:super] ? opts[:super].klass : nil
      m.super.subtypes << m if m.super
      m.schema = @@schema # don't call schema, it sets classes/primitives.
      @@current = m
      yield
    end

    def field(name, opts = {})
      f = get_field(@@current, name.to_s)
      t = opts[:type]
      f.type = @@primitives[t] || t.klass
      f.optional = opts[:optional] || false
      f.many = opts[:many] || false
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
      #puts "Creating field #{name} (#{f._id})"
      f.name = name
      klass.fields[name] = f
      f.owner = klass
      return f
    end

    def get_class(name)
      @@classes[name] ||= SchemaModel.new
      m = @@classes[name]
      #puts "Getting class #{name} (#{m._id})"
      m.name = name
      m.fields ||= ValueHash.new
      m.subtypes ||= []
      return m
    end

  end

end

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
