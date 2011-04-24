class Factory
  def initialize(schema)
    @schema = schema
  end

  def [](class_name)
    schema_class = @schema.classes[class_name.to_s]
    raise "Unknown class '#{class_name}'" unless schema_class
    obj = CheckedObject.new(schema_class, self)
    return obj
  end
  
  def method_missing(m, *args)
    obj = self[m.to_s]
    obj.schema_class.fields.each_with_index do |field, i|
      break if i >= args.length
      obj[field.name] = args[i]
    end
    return obj
  end
end

class CheckedObject

  attr_reader :schema_class
  attr_reader :_factory
  
  def initialize(schema_class, factory)
    @hash = {}
    @schema_class = schema_class
    @_factory = factory
    schema_class.fields.each do |field|
      if field.many
        # TODO: check for primitive many-valued???
        key = key(field.type)
        if key
          _primitive_set(field.name, ManyIndexedField.new(self, field, key))
        else
          _primitive_set(field.name, ManyField.new(self, field))
        end
      #else if field.expression && !field.computed
      #  @hash[field.name] = eval(field.expression)
      end
    end
  end
  
  def key(type)
    type.fields.find { |f| f.key && f.type.schema_class.name == "Primitive" }
  end  

  def [](field_name)
    field = @schema_class.fields[field_name]; 
    raise "Accessing non-existant field '#{field_name}'" unless field
    return @hash[field_name]
  end

  def []=(field_name, v)
    #puts "Setting #{field_name} to #{v}"
    field = @schema_class.fields[field_name]
    raise "Assign to invalid field '#{field_name}'" unless field
    raise "Can't assign to many-valued field '#{field_name}'" if field.many
    if v.nil?
      raise "Can't assign nil to required field '#{field_name}'" if !field.optional
    else
      case field.type.name
        when "str" then raise "Expected string found #{v.class} #{v}" unless v.is_a?(String)
        when "int" then raise "Expected int found #{v}" unless v.is_a?(Integer)
        when "bool" then raise "Expected bool found #{v}" unless v.is_a?(TrueClass) || v.is_a?(FalseClass)
        else 
          raise "Inserting into the wrong model" unless _factory.equal?(v._factory)
          unless subtypeOf(v.schema_class, field.type)
            raise "Expected #{field.type.name} found #{v.schema_class.name}" 
          end
      end
    end
    #if hash[field_name].primequal(v)  # SCARY!!!
      if _primitive_set(field_name, v)
        if field.inverse
          if field.inverse.many
            v.send(field.inverse.name)._primitive_insert(self)
          else
            v._primitive_set(field.inverse.name, self);
          end
        end
      end
    #end
    return v
  end
  
  def subtypeOf(a, b)
    return true if a.name == b.name
    return subtypeOf(a.super, b) if a.super
  end
  
  def _primitive_set(k, v)
    set = @hash[k] != v
    @hash[k] = v if set
    return set
  end
    
  def method_missing(m, *args, &block)
    if m =~ /(.*)=/
      self[$1] = args[0]
    else
      return self[m.to_s]
    end
  end
end

# eg. "classes" field on Schema
class ManyIndexedField
  include Enumerable
  
  def initialize(realself, field, key)
    @hash = {}
    @realself = realself
    @field = field
    @key = key
  end
  
  def [](x)
    @hash[x]
  end
  
  def length
    @hash.length
  end
  
  def keys
    @hash.keys
  end
  
  def values
    @hash.values
  end
  
  def <<(v)
    k = v.send(@key.name)
    self[k] = v
  end

  def []=(k, v)
    if @hash[k] != v
      @hash[k] = v
      if v && @field.inverse
        if @field.inverse.many
          v.send(@field.inverse.name)._primitive_insert(@realself)
        else
          v._primitive_set(@field.inverse.name, @realself)
        end
      end
    end
    return v
  end
  
  def _primitive_insert(v)
    raise "Inserting into the wrong model" unless @realself._factory.equal?(v._factory)
    k = v.send(@key.name)
    change = @hash[k] != v
    @hash[k] = v if change
    return change
  end
    
  def deleteByKey(v)

  end
  
  def each(&block) 
    @hash.each_value &block
  end
end  

# eg. "classes" field on Schema
class ManyField
  include Enumerable
  
  def initialize(realself, field)
    @list = []
    @realself = realself
    @field = field
  end
  
  def [](x)
    @list[x]
  end
  
  def length
    @list.length
  end
  
  def <<(v)
    if _primitive_insert(v)
      if v && @field.inverse
        if @field.inverse.many
          v.send(@field.inverse.name)._primitive_insert(@realself)
        else
          v._primitive_set(@field.inverse.name, @realself)
        end
      end
    end
  end

  def _primitive_insert(v)
    raise "Inserting into the wrong model" unless @realself._factory.equal?(v._factory)
    add = !@list.index(v)
    @list << v if add
    return add
  end
    
  def each(&block) 
    @list.each &block
  end
end  
