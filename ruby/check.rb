
class CyclicThing
  def initialize(root)
    @root = root
    @memo = {}
  end

  def run
    recurse(@root)
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
    send(obj.metaclass.name, obj, arg)
  end

  def prim?(model)
    model.is_a?(String) || 
      model.is_a?(Integer) || 
      model.is_a?(TrueClass) || 
      model.is_a?(FalseClass) || 
      model.is_a?(Array)
  end
end

# problem
# cyclic visiting on schema: stops to early, because it may find the the same class many times
# cyclic visiting on mode: stops to early,  because  the same primitive value may occur many times


class Conformance < CyclicCollectOnSecondArg
  attr_reader :errors

  def initialize(schema, obj)
    super(schema)
    @obj = obj
    @errors = []
  end

  def run
    klass = @root.classes.find do |c|
      c.name == @root.metaclass.name
    end
    if klass then
      recurse(klass, @root)
    else
      @errors << "Cannot find class #{@model.metaclass.name}"
    end
  end

  def Type(this)
  end

  def Primitive(this, model)
    ok = case this.name
        when "str"  then model.is_a?(String)
        when "int"  then model.is_a?(Integer)
        when "bool"  then model.is_a?(TrueClass) || model.is_a?(FalseClass)
        end
    unless ok
      @errors << "Type mismatch: expected #{this.name}, got #{model}"
    end
  end

  def Klass(this, model)
    puts "KLASS: Checking #{model} against #{this.name}"
    if model.is_a?(String) || model.is_a?(Integer) || model == true || model == false then
      @errors << "Expected class type, not primitive #{model}"
    elsif model.is_a?(Array) then
      # check all elements
      model.each do |elt|
        recurse(this, elt)
      end
    elsif this.name != model.metaclass.name then
      @errors << "Invalid class: expected #{this.name}, got #{model.metaclass.name}"
    else
      this.fields.each do |f|
        #puts "Model = #{model}, #{f.name}"
        recurse(f, model[f.name])
      end
    end
  end

  def Field(this, model)
    klass = "<unknown>"
    #puts "FIELD: Checking #{model} against field #{this.name}"
    if !this.optional && !this.many && model.nil? then
      @errors << "Field #{klass}.#{this.name} is required"
    elsif this.optional && !this.many && model.nil? then
      return
    elsif this.optional && this.many && model == [] then
      return
    elsif this.many && !model.is_a?(Array) then
      @errors << "Field #{klass}.#{this.name} is many but did not get array"
    elsif !this.many && model.is_a?(Array) then
      @errors << "Field #{klass}.#{this.name} is not many but got an array"
    elsif this.many && !this.optional && model == [] then
      @errors << "Field #{klass}.#{this.name} is non-optional many but got empty array"
    else
      recurse(this.type, model)
    end
  end

  
end
