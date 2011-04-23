class Factory
  def initialize(schema)
    @schema = schema
  end

  def method_missing(m, *args)
    klass = @schema.classes[m.to_s]
    #raise "Unknown class '#{m}'" unless klass
    CheckedObject.new(klass)
  end
end

class CheckedObject
  def initialize(klass)
    @hash = {}
    @klass = klass
    klass.fields.each do |field|
      if field.many
        @hash[field.name] = ManyField.new(self, field)
      #else if field.expression && !field.computed
      #  @hash[field.name] = eval(field.expression)
      end
    end
  end
  
  def method_missing(m, *args, &block)
    v = args[0]
    if m =~ /(.*)=/
      m = $1
      if m =~ /_primitive_(.*)/
        @hash[$1] = v
      else
        field = @klass.fields[m]
        raise "Assign to invalid field '#{m}'" unless field
        raise "Can't assign to many-valued field '#{m}'" if field.many
        raise "Can't assign nil to required field '#{m}'" if v.nil? && !field.optional
        ok = case field.type.name
          when "str"  then v.is_a?(String)
          when "int"  then v.is_a?(Integer)
          when "bool"  then v.is_a?(TrueClass) || v.is_a?(FalseClass)
          else v.klass.name == field.type.name
        end
        raise "Invalid value assigned to field '#{m}'" unless ok
        #if hash[$1].primequal(v)  # SCARY!!!
          @hash[$1] = v
          if field.inverse
            if field.inverse.many
              v.send(field.inverse.name).primitive_add(self)
            else
              v.send("#{field.inverse.name}=", self);
            end
          end
        #end
      end
    else
      field = @klass.fields[m]; 
      raise "Accessing non-existant field '#{m}'" unless field
      @hash[m]
    end
  end
end

# eg. "classes" field on Schema
class ManyField
  def initialize(realself, field)
    @hash = {}
    @realself = realself
    @field = field
    @key = "name" # field.type.key.name  # e.g. "name" field Klass
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
    k = v.send(key)
    self[k] = v
  end

  def _primitive_insert(v)
    k = v.send(key)
    @hash[k] = v
  end
    
  def []=(k, v)
    @hash[k] = v
    v.send("_primitive_#{field.inverse.name}=", realself)
  end
  
  def deleteByKey(v)

  end
  
  def each(&block) 
    @hash.each_value &block
  end
end  

