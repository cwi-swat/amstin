
class SchemaModel < BasicObject
  @@ids = 0

  attr_accessor :metaclass
	
  def initialize()
    @fields = {}
    @id = @@ids += 1
  end

  def [](k)
    @fields[k.to_sym]
  end

  def []=(k, v)
    @fields[k.to_sym] = v
  end

  def method_missing(name, *args, &block)
    if (name.to_s =~ /^([a-zA-Z0-9\_]*)=$/)
      self[$1] = args[0]
    else
      return self[name]
    end
  end

  def to_s
    "model(#{_id})"
  end

  def hash
    _id
  end

  def _id
    return @id
  end

  def inspect
    to_s
  end
end
