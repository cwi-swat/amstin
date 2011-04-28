
require 'schema/factory'
require 'grammar/tokenschema'

require 'strscan'

class Tokenize
  IDPATTERN = "[\\\\]?[a-zA-Z_$][a-zA-Z_$0-9]*"

  # TODO: make literals firstclass part of grammar?

  def initialize(literals, factory = Factory.new(TokenSchema.schema))
    @literals = Regexp.new("^(#{literals})", Regexp::MULTILINE)
    @factory = factory
    @id =  Regexp.new("^(#{IDPATTERN})(\\.#{IDPATTERN})*", Regexp::MULTILINE)
  end

  def tokenize(path, src)
    @stream = @factory.Stream(path)
    @scan = StringScanner.new(src)
    @src = src
    @line = 0
    while !@scan.eos? do
      skip_ws
      break if @scan.eos?
      next if match(/true|false/, :bool)
      next if match(@literals, :lit)
      next if match(@id, :sym)  
      next if match(/[0-9]+/, :int)
      next if match(/"(\\\\.|[^"])*"/, :str)
      next if match(/'(\\\\.|[^'])*'/, :sqstr)
      next if match(/[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?/, :real)
      raise "Could not match '#{@src[@scan.pos..@src.length]}'"
    end
    return @stream
  end  

  private

  def match(re, kind)
    pos = @scan.pos
    x = @scan.scan(re)
    if x then
      #puts "MATCHED: #{x} at  #{pos}"
      token(x, kind, pos)
    end
  end

  def token(str, kind, pos)
    l = str.length
    #puts "TOKEN: #{str}, #{kind}, line = #{@line}, start = #{pos}, end = #{@scan.pos}"
    # do this here?
    if kind == :sym then
      #puts "UNESCAPING: #{str}"
      str.gsub!(/\\/, '')
    end
    t = @factory.Token(@stream, @line, pos, @scan.pos, l, kind.to_s, str)
    @stream.tokens << t
    return t
  end
  
  def skip_ws
    # todo: comments, linenumbers
    x = @scan.scan(/\s+/)
    if @stream.tokens.empty? then
      @stream.layout = x || ''
    else
      @stream.tokens.last.layout = x || ''
    end
  end

end

require 'tools/print'

if __FILE__ == $0 then
  t = Tokenize.new("begin|end")
  m = t.tokenize("bla", "\n\n\n\nbegin 4 4 true false   \n true false end \n\n\n ")  
  p m
  Print.new.recurse(m, { :tokens => {} })
end
