
class CyclicThing
  def initialize(root)
    @root = root
    @memo = {}
  end

  def run
    recurse(@root)
  end
end


class CyclicCollect < CyclicThing
  def recurse(obj, *args)
    if @memo[obj] then
      return 
    end
    @memo[obj] = true
    
    send(obj.metaclass.name, obj, *args)
  end
end


class Conformance < CyclicCollect
  attr_reader :errors

  def initialize(schema, obj)
    super(schema)
    @obj = obj
    @errors = []
  end

  def run
    recurse(@root, @obj)
	@errors
  end

  def Schema(this, obj)
    #puts "Visiting: #{this.name}"
    klass = this.classes.find do |c|
      c.name == obj.metaclass.name
    end
    if klass then
      recurse(klass, obj)
    else
      @errors << "Cannot find class #{obj.metaclass.name}"
    end
  end

  def Type(this)
  end

  def Primitive(this, value)
    ok = case this.name
        when "str"  then value.is_a?(String)
        when "bool"  then value == true || value == false
        when "int"  then value.is_a?(Integer)
        end
    unless ok
      @errors << "Type mismatch: expected #{this.name}, got #{value}"
    end
  end

  def Klass(this, obj)
    #puts "Checking #{obj} against #{this}"
    if obj.is_a?(String) || obj.is_a?(Integer) || obj == true || obj == false then
      @errors << "Expected class type, not primitive #{obj}"
    elsif obj.is_a?(Array) then
      # check all elements
      #puts "Checking elements of #{obj}"
      obj.each do |elt|
        recurse(this, elt)
      end
    elsif this.name != obj.metaclass.name then
      @errors << "Invalid class: expected #{this.name}, got #{obj.metaclass.name}"
    else
      this.fields.each do |f|
        #puts "obj = #{obj}, #{f.name}"
        recurse(f, this.name, obj[f.name])
      end
    end
  end

  def Field(this, klass, obj)
    if !this.optional && !this.many && obj.nil? then
      @errors << "Field #{klass}.#{this.name} is required"
    elsif this.optional && !this.many && obj.nil? then
      return
    elsif this.optional && this.many && obj == [] then
      return
    elsif this.many && !obj.is_a?(Array) then
      @errors << "Field #{klass}.#{this.name} is many but did not get array"
    elsif !this.many && obj.is_a?(Array) then
      @errors << "Field #{klass}.#{this.name} is not many but got an array"
    elsif this.many && !this.optional && obj == [] then
      @errors << "Field #{klass}.#{this.name} is non-optional many but got empty array"
    elsif this.inverse then
      # ???
    else
      recurse(this.type, obj)
    end
  end

  
end
