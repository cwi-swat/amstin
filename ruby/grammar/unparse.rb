

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
    #separated(this.args, ' ')
    this.args.each do |arg|
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

  private
  
  def separated(lst, sep)
    return if lst.empty?
    recurse(lst.first)
    1.upto(lst.length - 1) do |i|
      @output << sep 
      recurse(lst[i])
    end
  end

end


