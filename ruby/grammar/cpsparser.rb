
require 'ostruct'

class CPSParser

  def initialize(input)
    @input = input
    @table = Table.new
  end

  def run(grammar, &block)
    recurse(grammar, &block)
  end

  def recurse(obj, *args, &block)
    send(obj.klass, obj, *args, &block)
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
    #puts "Parsing rule: #{pos}"
    entry = @table[this, pos]
    if entry.conts.empty? then
      #puts "First "
      entry.conts << block
      #p entry.conts
      recurse(this.pattern, pos) do |pos1, tree1|
        if !entry.subsumed?(pos1) then
          entry.results[pos1] = tree1
          entry.conts.each do |c|
            c.call(pos1, {this.name.to_sym => tree1})
          end
        end
      end
    else
      #puts "Else"
      entry.conts << block
      entry.results.each do |pos1, tree1|
        block.call(pos1, tree1)
      end
    end
  end

  def Seq(this, pos, &block)
    #puts "Parsing seq: #{pos}"
    #puts "seq = #{this.inspect}"
    recurse(this.left, pos) do |pos1, tree1|
      #puts "Parsied left of seq: #{pos1}"
      recurse(this.right, pos1) do |pos2, tree2|
        #puts "Parsied right of seq: #{pos1}"
        block.call(pos2, flatten1([tree1, tree2]))
      end
    end
  end

  def flatten1(list)
    list.inject([]) do |l, x|
      l + (x.is_a?(Array) ? x : [x])
    end
  end

  def Or(this, pos, &block)
    #puts "Parsing or: #{pos}"
    #puts "Left: #{this.left.inspect}"
    #puts "Parsing or-left"
    recurse(this.left, pos, &block)
    #puts "Parsing or-right"
    recurse(this.right, pos, &block)
    #puts "Bot or hands failed"
  end

  def Opt(this, pos, &block)
    block.call(pos, [])
    recurse(this.arg, pos, &block)
  end

  def Iter(this, pos, &block)
    recurse(this.arg, pos) do |pos1, tree1|
      recurse(this, pos) do |pos2, tree2|
        block.call(pos2, [:cons1, tree1, tree2])
      end
    end
    recurse(this.arg, pos, &block)
  end

  def IterStar(this, pos, &block)
    recurse(this, pos) do |pos1, tree1|
      recurse(this.arg, pos) do |pos2, tree2|
        block.call(pos2, [:cons, tree1, tree2])
      end
    end
    block.call(pos, [])
  end

  def IterSep(this, pos, &block)
    recurse(this.arg, pos) do |pos1, tree1|
      recurse(this.sep, pos1) do |pos2, tree2|
        recurse(this, pos) do |pos3, tree3|
          block.call(pos3, [tree1, tree2, tree3])
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

  def Token(this, pos, &block)
    return if eof?(pos)
    tk = token(pos)
    if tk.type == this.type then
      block.call(pos + 1, tk)
    end
  end

  def Lit(this, pos, &block)
    return if eof?(pos)
    tk = token(pos)
    if tk.type == "Lit" && tk.value == this.value then
      block.call(pos + 1, tk)
    end
  end

  def CiLit(this, pos, &block)
    return if eof?(pos)
    tk = token(pos)
    if tk.type == "Lit" && tk.value.downcase == this.value.downcase then
      block.call(pos + 1, tk)
    end
  end
end


require 'grammar/tokenize'

g = OpenStruct.new
exp = OpenStruct.new
exp.klass = "Rule"
exp.name = "Exp"

int = OpenStruct.new
int.klass = "Int"


plus = OpenStruct.new
plus.klass = "Lit"
plus.value = "+"

seq1 = OpenStruct.new
seq1.klass = "Seq"
seq1.left = plus
seq1.right = exp

seq = OpenStruct.new
seq.klass = "Seq"
seq.left = exp
seq.right = seq1

alts = OpenStruct.new
alts.klass = "Or"
alts.right = int
alts.left = seq
exp.pattern = alts

g.klass = "Grammar"
g.start = exp


t = Tokenize.new("\\+")
input = t.tokenize("bla", "1 + 2 + 3")  
require 'tools/print'

#Print.recurse(input, { :tokens => { :type => {} } })

p = CPSParser.new(input)
p.run(g) do |pos, tree|
  p pos
  p tree
end

