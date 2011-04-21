
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
    
    send(obj.instance_of.name, obj, *args)
  end
end


class Conformance < CyclicCollect
  attr_reader :errors

  def initialize(schema, model)
    super(schema)
    @model = model
    @errors = []
  end

  def run
    recurse(@root, @model)
  end

  def Schema(this, model)
    puts "Visiting: #{this.name}"
    klass = this.classes.find do |c|
      c.name == model.instance_of.name
    end
    if klass then
      recurse(klass, model)
    else
      @errors << "Cannot find class #{model.instance_of.name}"
    end
  end

  def Type(this)
  end

  def Primitive(this, model)
    ok = case this.name
        when "str"  then model.is_a?(String)
        when "bool"  then model == true || model == false
        when "int"  then model.is_a?(Integer)
        end
    unless ok
      @errors << "Type mismatch: expected #{this.name}, got #{model}"
    end
  end

  def Klass(this, model)
    puts "Checking #{model} against #{this}"
    if model.is_a?(String) || model.is_a?(Integer) || model == true || model == false then
      @errors << "Expected class type, not primitive #{model}"
    elsif model.is_a?(Array) then
      # check all elements
      puts "Checking elements of #{model}"
      model.each do |elt|
        recurse(this, elt)
      end
    elsif this.name != model.instance_of.name then
      @errors << "Invalid class: expected #{this.name}, got #{model.instance_of.name}"
    else
      this.fields.each do |f|
        puts "Model = #{model}, #{f.name}"
        recurse(f, this.name, model[f.name])
      end
    end
  end

  def Field(this, klass, model)
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
    elsif this.inverse then
      # ???
    else
      recurse(this.type, model)
    end
  end

  
end
