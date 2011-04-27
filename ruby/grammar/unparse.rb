

require 'cyclicmap'

class Unparse < CyclicCollect 
  def initialize(output)
    super()
    @output = output
  end

  def ParseTree(this)
    @output << this.layout
    recurse(this.top)
  end

  def Sequence(this)
    this.elements.each do |arg|
      recurse(arg)
    end
  end

  def Create(this)
    recurse(this.arg)
  end

  def Field(this)
    recurse(this.arg)
  end

  def Code(this)
  end

  def Value(this)
    # todo: escaping for str, sqstr and sym
    # how to insert \ for syms again?
    # do we need the literal regexp?
    @output << this.value
    @output << this.layout
  end

  def Lit(this)
    @output << this.value
    @output << this.layout
  end

  def Ref(this)
    # todo: escaping for sym
    @output << this.name
    @output << this.layout
  end

  def Key(this)
    # todo: escaping for sym
    @output << this.name
    @output << this.layout
  end

  def Regular(this)
    this.args.each do |arg|
      recurse(arg)
    end
  end 

end


