
class CyclicThing
  def initialize(root)
    @root = root
    @memo = {}
  end

  def run
    recurse(@root)
  end
end

class CyclicMap < CyclicThing
  def recurse(obj)
    if @memo[obj] then
      return @memo[obj]
    end
    
    result = BootstrapModel.new 
    @memo[obj] = result
    send(obj.klass, obj, result)
    return result
  end
end

class CyclicClosure < CyclicThing
  def recurse(obj)
    if @memo[obj] then
      return
    end
    ref = nil
    @memo[obj] = lambda { |*x| ref.call(*x) }
    ref = send(obj.klass, obj)
  end
end

class CyclicCollect < CyclicThing
  def recurse(obj)
    if @memo[obj] then
      return 
    end
    @memo[obj] = true
    send(obj.klass, obj)
  end
end


