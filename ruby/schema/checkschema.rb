
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
      model.is_a?(Array) ||
      model.is_a?(Hash)
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
    klass = @root.classes[@root.metaclass.name]
    if klass then
      recurse(klass, @root)
    else
      @errors << "Cannot find class #{@model.metaclass.name}"
    end
  end

  def prim?(model)
    model.is_a?(String) || 
      model.is_a?(Integer) || 
      model.is_a?(TrueClass) || 
      model.is_a?(FalseClass) || 
      model.is_a?(Array) ||
      model.is_a?(Hash)
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
    elsif model.is_a?(Array) || model.is_a?(Hash) then
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
    return if this.optional && !this.many && model.nil? 
    return if this.optional && this.many && model == []

    if !this.optional && !this.many && model.nil? then
      @errors << "Field #{klass}.#{this.name} is required"
    elsif this.many && !(model.is_a?(Array) || model.is_a?(Hash)) then
      @errors << "Field #{klass}.#{this.name} is many but did not get array"
    elsif !this.many && (model.is_a?(Array) || model.is_a?(Hash)) then
      @errors << "Field #{klass}.#{this.name} is not many but got an array"
    elsif this.many && !this.optional && model == [] then
      @errors << "Field #{klass}.#{this.name} is non-optional many but got empty array"
    elsif this.inverse && (model.is_a?(Array) || model.is_a?(Hash)) then
      # for each element in model, there should be a field named this.inverse.name
      # that's not null if this.inverse. And it should point to the current thing
      # e.g. the klass containing "this" field.
    elsif this.inverse && prim?(model) then
      @errors << "Primitive field #{this.name} cannot have inverse"
    elsif this.inverse && !this.inverse.optional && !model.send(this.inverse.name) then
      @errors << "Inverse of field #{this.name} is non-optional"
    end


    recurse(this.type, model)
  end

  
end
