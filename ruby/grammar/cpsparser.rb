
require 'cyclicmap'
require 'grammar/grammargrammar'

class CollectLiterals < CyclicExecOtherwise
  def initialize
    super
    @literals = []
  end

  def pattern
    @literals.join("|")
  end

  def ci_pattern(cl)
    re = "("
    cl.each_char do |c|
      re += "[#{c.upcase}#{c.downcase}]"
    end
    re + ")";
  end


  def Lit(this)
    @literals << Regexp.escape(this.value)
  end

  def CiLit(this)
    @literals << ci_pattern(this.value)
  end

  def _(this)
    this.schema_class.fields.each do |f|
      v = this[f.name]
      # todo: check on f.type
      recurse(this[f.name]) if v && v.is_a?(CheckedObject)
      if f.many then
        v.each do |elt|
          recurse(elt) if elt && elt.is_a?(CheckedObject)
        end
      end
    end
  end

end


class CPSParser

  def initialize(input)
    @input = input
    @table = Table.new
  end

  def run(grammar, &block)
    recurse(grammar, &block)
  end

  def recurse(obj, *args, &block)
    send(obj.schema_class.name, obj, *args, &block)
  end

  def token(pos)
    @input.tokens[pos]
  end

  def eof?(pos)
    pos == @input.tokens.length
  end

  class Table
    def initialize
      @table = {}
    end

    def [](cont, pos)
      key = cont.to_s
      @table[key] ||= {}
      @table[key][pos] ||= Entry.new
    end
  end

  class Entry
    attr_accessor :conts, :results
    def initialize
      @conts = []
      @results = {}
    end

    def subsumed?(pos)
      @results.include?(pos)
    end
  end
  
	
  def Grammar(obj, &block)
    recurse(obj.start, 0, &block)
  end

  def Rule(this, pos, &block)
    entry = @table[this, pos]
    if entry.conts.empty? then
      entry.conts << block
      this.alts.each do |alt|
        recurse(alt, pos) do |pos1, tree1|
          return if entry.subsumed?(pos1) 
          entry.results[pos1] = tree1
          entry.conts.each do |c|
            c.call(pos1, {this.name.to_sym => tree1})
          end
        end
      end
    else
      entry.conts << block
      # NB: keys to prevent modifying hash during iterations
      entry.results.keys.each do |pos1|
        block.call(pos1, entry.results[pos1])
      end
    end
  end

  def Sequence(this, pos, &block)
    f = lambda do |i, pos, lst| 
      if i == this.elements.length then
        block.call(pos, lst)
      else
        recurse(this.elements[i], pos) do |pos1, tree|
          f.call(i + 1, pos1, [*lst, tree])
        end
      end
    end
    f.call(0, pos, [])
  end

  def Create(this, pos, &block)
    f = lambda do |i, pos, lst| 
      if i == this.elements.length then
        block.call(pos, {this.name => lst})
      else
        recurse(this.elements[i], pos) do |pos1, tree|
          f.call(i + 1, pos1, [*lst, tree])
        end
      end
    end
    f.call(0, pos, [])
  end

  def Field(this, pos, &block)
    recurse(this.arg, pos) do |pos1, tree|
      block.call(pos1, {this.name => tree})
    end
  end

  %w(Int Str SqStr Real Bool).each do |tk|
    class_eval %Q{
      def #{tk}(this, pos, &block)
        return if eof?(pos)
        tk = token(pos)
        block.call(pos + 1, tk) if tk.kind == "#{tk}"
      end
    }
  end

  %w(Id Key Ref Call).each do |tk|
    class_eval %Q{
      def #{tk}(this, pos, &block)
        puts "Parsing id"
        return if eof?(pos)
        tk = token(pos)
        block.call(pos + 1, tk) if tk.kind == "Id"
      end
    }
  end

  def Lit(this, pos, &block)
    puts "Parsing lit: #{this.value}"
    return if eof?(pos)
    tk = token(pos)
    if tk.kind == "Lit" && tk.value == this.value then
      puts "Parsed lit: #{tk}"
      block.call(pos + 1, tk)
    end
  end

  def CiLit(this, pos, &block)
    return if eof?(pos)
    tk = token(pos)
    if tk.kind == "Lit" && tk.value.downcase == this.value.downcase then
      block.call(pos + 1, tk)
    end
  end


  def Opt(this, pos, &block)
    block.call(pos, [])
    recurse(this.arg, pos, &block)
  end

  ## NB: iters are right-recursive (otherwise we have to memoize them)
  ## This also means that iter'ed symbols should not be nullable

  def Iter(this, pos, &block)
    recurse(this.arg, pos) do |pos1, tree1|
      recurse(this, pos) do |pos2, tree2|
        block.call(pos2, [tree1, tree2])
      end
    end
    recurse(this.arg, pos, &block)
  end

  def IterStar(this, pos, &block)
    puts "Parsing iterstar pos = #{pos}, this = #{this}"
    recurse(this.arg, pos) do |pos1, tree1|
      recurse(this, pos) do |pos2, tree2|
        block.call(pos2, [tree1, tree2])
      end
    end
    block.call(pos, [])
  end

  def IterSep(this, pos, &block)
    recurse(this.arg, pos) do |pos1, tree1|
      recurse(this.sep, pos1) do |pos2, _|
        recurse(this, pos2) do |pos3, tree3|
          block.call(pos3, [tree1, tree3])
        end
      end
    end
    recurse(this.arg, pos, &block)
  end

  def IterStarSep(this, pos, &block)
    # pretend it's an IterSep
    send(:IterSep, this, pos, &block)
    # and add empty alternative
    block.call(pos, [])
  end

end


require 'grammar/tokenize'
require 'tools/print'


G = GrammarGrammar.grammar

c = CollectLiterals.new
c.recurse(G)
puts c.pattern

t = Tokenize.new(c.pattern)
input = t.tokenize("bla", "grammar Grammar start Grammar
  Exp ::= [Int] int
       |  [Add] Exp \"+\" Exp
")  

#Print.recurse(input, { :tokens => { :type => {} } })

p = CPSParser.new(input)
p.run(G) do |pos, tree|
  p pos
  p tree
end

