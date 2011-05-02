
require 'schema/factory'
require 'grammar/tokenschema'
require 'cyclicmap'

require 'strscan'

class CollectLiterals < CyclicCollectShy
  def Lit(this, accu)
    if this.case_sensitive then
      if this.value =~ /[\\a-zA-Z_][a-zA-Z0-9_$]/ then
        accu << "(" + Regexp.escape(this.value) + "(?![a-zA-Z0-9_$])" + ")"
      else
        accu << Regexp.escape(this.value)
      end
    else
      accu << "(#{ci_pattern(this.value)}(?![a-zA-Z0-9_$]))"
    end
  end

  def Regular(this, accu)
    if this.sep then
      accu << Regexp.escape(this.sep)
    end
  end

  private 
  def ci_pattern(cl)
    re = "("
    cl.each_char do |c|
      re += "[#{c.upcase}#{c.downcase}]"
    end
    re + ")";
  end
end


class Tokenize
  IDPATTERN = "[\\\\]?[a-zA-Z_$][a-zA-Z_$0-9]*"

  def initialize(factory = Factory.new(TokenSchema.schema))
    @factory = factory
    @id = Regexp.new("^(#{IDPATTERN})(\\.#{IDPATTERN})*")
  end

  def tokenize(grammar, path, src)
    # todo: sort on length
    lits = CollectLiterals.run(grammar).join("|")
    @literals = Regexp.new("^(#{lits})")
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
require 'grammar/grammargrammar'

if __FILE__ == $0 then
  t = Tokenize.new
  m = t.tokenize(GrammarGrammar.grammar, "bla", "\n\n\n\nbegin 4 4 true false   \n true false end \n\n\n ")  
  p m
  Print.new.recurse(m, { :tokens => {} })
end
