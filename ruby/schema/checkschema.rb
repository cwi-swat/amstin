
class CyclicThing
  def initialize
    @memo = {}
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

class CyclicCollectOnBoth < CyclicThing
  def recurse(obj, arg)
    if !prim?(arg) then
      if @memo[[obj, arg]] then
        return 
      else
        @memo[[obj, arg]] = true
      end
    end
    send(obj.schema_class.name, obj, arg)
  end
end

# problem
# cyclic visiting on schema: stops to early, because it may find the the same class many times
# cyclic visiting on mode: stops to early,  because  the same primitive value may occur many times


class Conformance < CyclicCollectOnBoth
  attr_reader :errors

  def initialize()
    super()
    @errors = []
  end

  def Type(this)
  end

  def Primitive(this, obj)
    ok = case this.name
        when "str"  then obj.is_a?(String)
        when "int"  then obj.is_a?(Integer)
        when "bool"  then obj.is_a?(TrueClass) || obj.is_a?(FalseClass)
        end
    unless ok
      @errors << "Type mismatch: expected #{this.name}, got #{obj}"
    end
  end

  def Klass(this, obj)
    puts "KLASS: Checking #{obj} against #{this.name}"
    if obj.is_a?(String) || obj.is_a?(Integer) || obj == true || obj == false then
      @errors << "Expected class type, not primitive #{obj}"
    elsif obj.is_a?(Array) || obj.is_a?(Hash) then
      # check all elements
      obj.each do |elt|
        recurse(this, elt)
      end
    elsif !subtypeOf(obj.schema_class, this) then
      @errors << "Invalid class: expected #{this.name}, got #{obj.schema_class.name}"
    else
      this.fields.each do |f|
        recurse(f, obj[f.name])
      end
    end
  end
  
  def subtypeOf(a, b)
    return true if a.name == b.name
    return subtypeOf(a.super, b) if a.super
  end

  def Field(this, obj)
    puts "FIELD: #{this.name}, #{obj}"
    return if this.optional && !this.many && obj.nil? 
    return if this.optional && this.many && obj == []

    if !this.optional && !this.many && obj.nil? then
      @errors << "Field #{this.name} is required"
    elsif this.many && !(obj.is_a?(Array) || obj.is_a?(Hash)) then
      @errors << "Field #{this.name} is many but did not get array"
    elsif !this.many && (obj.is_a?(Array) || obj.is_a?(Hash)) then
      @errors << "Field #{this.name} is not many but got an array"
    elsif this.many && !this.optional && obj == [] then
      @errors << "Field #{this.name} is non-optional many but got empty array"
    elsif this.inverse && (obj.is_a?(Array) || obj.is_a?(Hash)) then
      # for each element in obj, there should be a field named this.inverse.name
      # that's not null if this.inverse. And it should point to the current thing
      # e.g. the klass containing "this" field.
    elsif this.inverse && prim?(obj) then
      @errors << "Primitive field #{this.name} cannot have inverse"
    elsif this.inverse && !this.inverse.optional && !obj.send(this.inverse.name) then
      @errors << "Inverse of field #{this.name} is non-optional"
    end

    puts "THIS.Type: #{this.type.name}"
    recurse(this.type, obj)
  end

  
end

if __FILE__ == $0 then
  require 'schema/schemaschema'
  
  ss = SchemaSchema.schema
  # if you want to print something out, see example at end of print.rb 
  puts "Checking #{ss.name}"

  check = Conformance.new
  check.recurse(ss.classes["Schema"], ss)
  check.errors.each do |x|
    puts x
  end
end

