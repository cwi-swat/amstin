
require 'schema/schemamodel'

class ValueHash < Hash
  include Enumerable
  def each(&block)
    values.each &block
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

    def method_missing(name, *args)
      @builder.get_field(@klass, name.to_s)
    end
  end

  @@schemas = {}

  def self.inherited(subclass)
    schema = SchemaModel.new
    @@schemas[subclass.to_s] = schema
    schema.name = subclass.to_s
    schema.classes = ValueHash.new
    schema.primitives = ValueHash.new
  end

  def self.schema
    @@schemas[self.to_s]
  end
    

  class << self
    def primitive(name)
      m = SchemaModel.new
      m.name = name.to_s
      schema.primitives[name] = m
    end
      
    def klass(wrapped, opts = {}, &block)
      m = wrapped.klass
      m.super = opts[:super] ? opts[:super].klass : nil
      m.super.subtypes << m if m.super
      m.schema = schema
      @@current = m
      yield
    end

    def field(name, opts = {})
      f = get_field(@@current, name.to_s)
      t = opts[:type]
      f.type = schema.primitives.keys.include?(t) ? schema.primitives[t] : t.klass
      f.optional = opts[:optional] || false
      f.many = opts[:many] || false
      f.key = opts[:key] || false
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
      schema.classes[name] ||= SchemaModel.new
      m = schema.classes[name]
      #puts "Getting class #{name} (#{m._id})"
      m.name = name
      m.fields ||= ValueHash.new
      m.subtypes ||= []
      return m
    end

    def finalize(schema)
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
  end

end
