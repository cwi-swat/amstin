
require 'schema/factory'
require 'grammar/tokenschema'


class Tokenize
  IDPATTERN = "[\\\\]?[a-zA-Z_$][a-zA-Z_$0-9]*"

  def initialize(literals, factory = Factory.new(TokenSchema.schema))
    @literals = Regexp.new("^(#{literals})")
    @factory = factory
    @id =  Regexp.new("^(#{IDPATTERN})(\\.#{IDPATTERN})*")
  end

  def tokenize(path, src)
    @stream = @factory.Stream(path)
    @src = src
    @pos = 0
    @line = 0
    while !@src.empty? do
      skip_ws
      break if @src.empty?
      next if match(/^(true|false)/, :bool)
      next if match(@literals, :Lit)
      next if match(@id, :sym)  
      next if match(/^[0-9]+/, :int)
      next if match(/^"(\\\\.|[^"])*"/, :str)
      next if match(/^'(\\\\.|[^'])*'/, :sqstr)
      next if match(/^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?/, :real)
      raise "Could not match #{@src}"
    end
    return @stream
  end  

  def match(re, kind)
    if @src =~ re then
      token($&, kind)
    end
  end

  def token(str, kind)
    l = str.length
    puts "TOKEN: #{str}, #{kind}, line = #{@line}, start = #{@pos}, end = #{@pos + l}"
    t = @factory.Token(@stream, @line, @pos, @pos += l, l, kind.to_s, str)
    skip(l)
    @stream.tokens << t
    return t
  end

  def skip(l)
    # ugh, I don't like this
    skipped = @src[0..l-1]
    @src = @src[l..@src.length]
    return skipped
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
    s = skip(i)
    if @stream.tokens.empty? then
      @stream.layout = s # wrong, somehow???
    else
      @stream.tokens.last.layout = s
    end
  end

end

require 'tools/print'

if __FILE__ == $0 then
  t = Tokenize.new("begin|end")
  m = t.tokenize("bla", "\n\n\n\nbegin 4 4 true false   \n true false end ")  
  p m
  Print.recurse(m, { :tokens => {} })
end
