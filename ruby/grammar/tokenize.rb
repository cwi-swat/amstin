
require 'schema/bootfactory'
require 'grammar/tokenschema'


# class Literals < CyclicShyCollect
#   def initialize(grammar)
#     super(grammar)
#   end

#   def Lit(this)
#   end

#   def CiLit(this)
#   end

# end


class Tokenize
  IDPATTERN = "[\\\\]?[a-zA-Z_$][a-zA-Z_$0-9]*"

  def initialize(literals, factory = Factory.new(TokenSchema.schema))
    @literals = Regexp.new("^(#{literals})")
    @factory = factory
    @id =  Regexp.new("^(#{IDPATTERN})(\\.#{IDPATTERN})*")
  end

  def tokenize(path, src)
    @stream = @factory.Stream(path, [])
    @src = src
    @pos = 0
    @line = 0
    while !@src.empty? do
      skip_ws
      break if @src.empty?
      next if match(/^(true|false)/, :Bool)
      next if match(@literals, :Lit)
      next if match(@id, :Id)  
      next if match(/^[0-9]+/, :Int)
      next if match(/^"(\\\\.|[^"])*"/, :Str)
      next if match(/^'(\\\\.|[^'])*'/, :SqStr)
      next if match(/^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?/, :Real)
      raise "Could not match #{@src}"
    end
    return @stream
  end  

  def match(re, type)
    if @src =~ re then
      token($&, type)
    end
  end

  def token(str, type)
    l = str.length
    puts "TOKEN: #{str}, #{type}, line = #{@line}, start = #{@pos}, end = #{@pos + l}"
    t = @factory.Token(@stream, @line, @pos, @pos += l, l, @factory.send(type), str)
    skip(l)
    @stream.tokens << t
    return t
  end

  def skip(l)
    # ugh, I don't like this
    @src = @src[l..@src.length]
  end
  
  def skip_ws
    # todo: comments
    #puts "SOURCE----------> '#{@src}'"
    i = 0
    while @src[i] =~ /\s/ do
      if @src[i].chr == "\n" then
        @line += 1
      end
      @pos += 1
      i += 1
    end
    skip(i)
  end

end


if __FILE__ == $0 then
  t = Tokenize.new("begin|end")
  p t.tokenize("bla", "\n\n\n\nbegin 4 4 true false   \n true false end ")
  
end
