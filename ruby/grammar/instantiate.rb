
class Instantiate

  def initialize(factory)
    super()
    @factory = factory
    @defs = {}
    @fixes = []
  end

  def run(pt)
    # ugh: @root is set in recurse...
    recurse(pt, nil, nil, 0)
    @fixes.each do |fix|
      fix.apply(@defs)
    end
    return @root
  end

  def recurse(this, *args)
    send(this.schema_class.name, this, *args)
  end

  def update(owner, field, pos, x)
    if field && field.many then
      owner[field.name] << x
      return pos + 1
    elsif field then
      owner[field.name] = x
    end
    return pos
  end
      

  def ParseTree(this, owner, field, pos)
    recurse(this.top, owner, field, pos)
  end

  def Sequence(this, owner, field, pos)
    this.elements.inject(pos) do |pos1, arg|
      recurse(arg, owner, field, pos1)
    end
  end
  
  def Create(this, owner, field, pos)
    puts "Creating #{this.name}"
    current = @factory.send(this.name)
    # ugly
    @root = current unless owner
    recurse(this.arg, current, nil, 0)
    update(owner, field, pos, current)
  end

  def Field(this, owner, field, pos)
    puts "Field #{this.name} in #{owner}"
    f = owner.schema_class.fields[this.name]
    recurse(this.arg, owner, f, 0)
  end

  def Code(this, owner, field, pos)
    owner.instance_eval(this.code)
  end

  def Value(this, owner, field, pos)
    return pos unless field # values without field????
    puts "Value: #{this} for #{field}"
    # todo: escaping for str, sqstr and sym
    v = this.value
    case this.kind 
    when "str" then 
      #puts "VVVV = #{v}"
      v.gsub!(/\\"/, '"')
      #puts "VVVVsub = #{v}"
      v = v[1..-2]
      #puts "VVVVslice = #{v}"
    when "sqstr" then
      v.gsub!(/\\'/, "'")
      v = v[1..-2]
    when "bool" then
      v = (v == "true")
    when "int" then
      v = Integer(v)
    when "real" then
      v = Float(v)
    when "sym" then
      v.sub!(/\\/, '')
    else
      raise "Don't know kind #{this.kind}"
    end
    #puts "VVVVVVV = #{v}"
    update(owner, field, pos, v)
  end

  def Lit(this, owner, field, pos)
    if field && !field.many then
      # don't add literals to lists
      puts "Parsing Lit #{this.value} for #{field.name}"
      owner[field.name] = this.value
    end
    pos
  end

  def Ref(this, owner, field, pos)
    puts "Stubbing ref #{this.name} in #{owner}"
    stub = Stub.new(@factory, field)
    @fixes << Fix.new(this.name, owner, field, pos)
    update(owner, field, pos, stub)
  end

  def Key(this, owner, field, pos)
    puts "--------> Defining key #{this.name} to #{owner}"
    owner[field.name] = this.name
    @defs[this.name] = owner
    # todo: assert field is never many
    update(owner, field, pos, this.name)
  end

  class Stub
    def initialize(factory, field)
      @factory = factory
      # field should not be primitive
      @schema_class = field.type
    end
    def _factory
      @factory
    end

    def schema_class
      @schema_class
    end
  end

  class Fix
    def initialize(name, this, field, pos)
      @name = name
      @this = this
      @field = field
      @pos = pos
    end

    def apply(defs)
      puts "FIXING: #{@name} in #{@this} in field #{@field.name}"
      if @field.many then
        puts "\tResolving at pos #{@pos} to #{defs[@name]}" 
        @this[@field.name][@pos] = defs[@name]
      else
        @this[@field.name] = defs[@name]
      end
    end
  end
end

