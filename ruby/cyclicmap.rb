
class CyclicThing
  def initialize
    @memo = {}
  end

  def self.run(*args)
    self.new().recurse(*args)
  end

  def prim?(obj)
    obj.is_a?(String) || 
      obj.is_a?(Integer) || 
      obj.is_a?(TrueClass) || 
      obj.is_a?(FalseClass) || 
      obj.is_a?(Array) ||
      obj.is_a?(Hash)
  end
end

class CyclicCollectOnSecondArg < CyclicThing
  def recurse(obj, arg)
    if !prim?(arg) then
      if @memo[arg] then
        return 
      else
        @memo[arg] = true
      end
    end
    send(obj.schema_class.name, obj, arg)
  end
end

# used in checkschema
class CyclicCollectOnBoth < CyclicThing
  def recurse(obj, arg)
    if !prim?(arg) then
      if @memo[[obj, arg]] then
        return 
      else
        @memo[[obj, arg]] = true
      end
    end
    @memo[obj] = true
    send(obj.schema_class.name, obj, arg)
  end
end

# in use
class CyclicMapNew < CyclicThing
  def initialize()
    super()
    puts @memo
  end
  def recurse(from)
    raise "shouldn't be nil" if from.nil?
    to = @memo[from]
    return to if to
    #puts "SENDING #{from.schema_class.name}"
    send(from.schema_class.name, from)
  end
  def register(from, to)
    @memo[from] = to
    yield to
    return to
  end
  # TODO: HACK!!!
  def registerUpdate(from, to)
    @memo[from] = to
    result = yield to
    @memo[from] = result
    return result
  end
end

# used in grammar.rb
class CyclicClosure < CyclicThing
  def recurse(obj)
    if @memo[obj] then
      return
    end
    ref = nil
    @memo[obj] = lambda { |*x| ref.call(*x) }
    ref = send(obj.schema_class.name, obj)
  end
end

# should this be called CyclicVisit?
class CyclicCollect < CyclicThing
  def recurse(obj)
    if @memo[obj] then
      return 
    end
    @memo[obj] = true
    send(obj.schema_class.name, obj)
  end
end

=begin
class 

What about Fixpoint Cyclic Map
  
data = info[from.key]
if data.computed then
  return data.value
if !CHANGE
  INCYCLE = true
  data.visited = true
  do
    CHANGE = false
    compute()
  } while (CHANGE)
    data.computed = true
  data.visited = false
  INCYCLE = false
}
else if !data.visited
  data.visited = true
  compute()
  data.visited = false
}
return data.value

compute()
  val = call(...)
  CHANGE |= val != data.value
  data.value = val
=end

class CyclicExecOtherwise < CyclicThing
  def recurse(obj)
    if @memo[obj] then
      return 
    end
    @memo[obj] = true
    msg = obj.schema_class.name
    if respond_to?(msg) then
      send(msg, obj)
    else
      send(:_, obj)
    end
  end
end

