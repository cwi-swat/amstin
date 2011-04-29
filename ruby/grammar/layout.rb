
require 'cyclicmap'
require 'grammar/layoutschema'

class FormatWidth < MemoBase
  def Sequence(obj)
    n = 0
    obj.elements.map do |x|
      n += recurse(x)
    end
    return n
  end

  def Group(obj)
    recurse(obj.arg)
  end

  def Nest(obj)
    recurse(obj.arg)
  end

  def Break(obj)
    obj.indent = -1 # reset the break positions
    obj.sep ? obj.sep.length : 0
  end

  def Text(obj)
    obj.value.length
  end
end

# computes the position of all the breaks
# indent: current indent level
# current: current output position
# output: final output position
class FormatChoice < Dispatch
  def initialize(width)
    super()
    @width = width
    @computed = FormatWidth.new
  end

  def run(obj)
    recurse(obj, 0, 0)
  end

  def Sequence(obj, indent, current)
    obj.elements.each do |sub|
      current = recurse(sub, indent, current)
    end
    return current
  end

  def Group(obj, indent, current)
    subwidth = @computed.recurse(obj.arg)
    if current + subwidth <= @width
      return current + subwidth
    else
      puts "BREAKING"
      return recurse(obj.arg, indent, current) # flip the bits
    end
  end

  def Nest(obj, indent, current)
    recurse(obj.arg, indent + obj.indent, current)
  end

  def Break(obj, indent, current)
    obj.indent = indent
    return indent
  end

  def Text(obj, indent, current)
    return current + obj.value.length
  end
end


class DisplayFormat < Dispatch
  def initialize(output)
    super()
    @output = output
  end
  
  def Sequence(obj)
    obj.elements.each do |x|
      recurse(x)
    end
  end

  def Group(obj)
    recurse(obj.arg)
  end

  def Nest(obj)
    recurse(obj.arg)
  end

  def Break(obj)
    if obj.indent == -1
      @output << obj.sep if obj.sep
    else
      @output << "\n"
      @output << (" " * obj.indent)
    end
  end

  def Text(obj)
    @output << obj.value
  end
end


def main
  require 'schema/factory'
  require 'grammar/layoutschema'
  require 'grammar/render'
  require 'tools/print'
  require 'grammar/grammargrammar'

  render = Render.new(Factory.new(LayoutSchema.schema))
  
  layout = render.recurse(GrammarGrammar.grammar, GrammarGrammar.grammar)
  
  puts "WIDTH = #{FormatWidth.new.recurse(layout)}"
  
  FormatChoice.new(80).run(layout)
  DisplayFormat.new($stdout).recurse(layout)
  $stdout << "\n"
end

if __FILE__ == $0 then
  main
end