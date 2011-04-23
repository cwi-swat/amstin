
class Factory
  def initialize(schema)
    @schema = schema
  end
  def method_missing(m, *args)
   klass = schema.classes[m]
   error "Unknown class" unless klass
   ModelObject.new(klass)
  end
end

class ModelObject
  def initialize(klass)
    @hash = {}
    @klass = klass
    klass.fields.foreach |field|
      if field.many
        hash[field.name] = ManyValued.new(self, field)
      #else if field.expression && !field.computed
      #  hash[field.name] = eval(field.expression)
      end
  end
  
  def method_missing(m, *args, &block)
    v = args[0]
    if m =~ /(.*)=/
      m = $1
      if m =~ /_primitive_(.*)/
        hash[$1] = v
      else
        field = klass.fields[m]; 
        error "Assign to invalid field" unless field
        error "Wrong type" unless v.klass.name == field.type.name
        error "Can't assign to many-valued field" if field.many
        error "Can't assign nil to required field" if v.nil? && !field.optional
        if hash[$1].primequal(v)  # SCARY!!!
          hash[$1] = v
          if field.inverse
            if field.inverse.many
              v.send(field.inverse.name).primitive_add(self)
            else
              v.send("#{field.inverse.name}=", self);
            end
          end
        end
      end
    else
      field = klass.fields[m]; 
      error "Accessing non-existant field" unless field
      hash[m]
    end
  end
end

# eg. "classes" field on Schema
class ManyField
  def initialize(realself, field)
    @hash = {}
    @realself = realself
    @field = field
    @key = field.type.key.name  # e.g. "name" field Klass
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
    hash[k] = v
  end
    
  def []=(k, v)
    hash[k] = v
    v.send("_primitive_#{field.inverse.name}=", realself)
  end
  
  def deleteByKey(v)

  end
  
  def each(&block) 
    hash.each_value &block
  end
end  

