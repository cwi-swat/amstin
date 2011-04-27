
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

  def ParseTree(this, owner, field, pos)
    recurse(this.top, owner, field, pos)
  end

  def Sequence(this, owner, field, pos)
    this.elements.each do |arg|
      pos = recurse(arg, owner, field, pos)
    end
    pos
  end
  
  def Create(this, owner, field, pos)
    puts "Creating #{this.name}"
    current = @factory.send(this.name)
    # ugly
    @root = current unless owner
    recurse(this.arg, current, nil, pos)
    if field && field.many then
      owner[field.name] << current
      return pos + 1
    end
    if field && !field.many then
      owner[field.name] = current
    end
    pos
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
    if field.many then
      # todo: escaping for str, sqstr and sym
      owner[field.name] << this.value
      return pos + 1
    end
    owner[field.name] = this.value
    pos
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
    if field.many then
      puts "\tstubbing at #{pos}"
      owner[field.name] << stub
      return pos + 1
    end
    owner[field.name] = stub
    pos
  end

  def Key(this, owner, field, pos)
    puts "--------> Defining key #{this.name} to #{owner}"
    # todo: assert field is never many
    owner[field.name] = this.name
    @defs[this.name] = owner
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

