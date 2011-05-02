
require 'grammar/grammarschema'
require 'grammar/parsetree'
require 'schema/factory'
require 'grammar/tokenize'
require 'grammar/instantiate'

class CPSParser
  def self.parse(path, grammar)
    tokenizer = Tokenize.new
    input = tokenizer.tokenize(grammar, path, File.read(path)) 
    parse = CPSParser.new(input, Factory.new(ParseTreeSchema.schema))
    parse.run(grammar)
  end
  
  def self.load_raw(path, grammar, schema)
    tree = CPSParser.parse(path, grammar)
    inst2 = Instantiate.new(Factory.new(schema))
    data = inst2.run(tree)
    return data
  end

  def self.load(path, grammar, schema)
    data = load_raw(path, grammar, schema)
    data.finalize
    return data
  end

  def initialize(input, factory, gf = Factory.new(GrammarSchema.schema))
    @input = input
    @table = Table.new
    @factory = factory
    @grammar_factory = gf 
  end

  def run(grammar)
    recurse(grammar, 0) do |pos, tree|
      if pos == @input.tokens.length then
        return @factory.ParseTree(@input.path, tree, @input.layout)
      end
    end
  end

  # todo: move to generic visit/dispatch class
  def recurse(obj, *args, &block)
    send(obj.schema_class.name, obj, *args, &block)
  end

  def token(pos)
    @input.tokens[pos]
  end

  def eof?(pos)
    pos == @input.tokens.length
  end

  def with_token(pos, kind)
    return if eof?(pos)
    tk = token(pos)
    yield tk if tk.kind == kind
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
  
  class Success < Exception
    attr_reader :tree
    def initialize(tree)
      @tree = tree
    end
  end
	
  def Grammar(obj, pos, &block)
    recurse(obj.start, pos, &block)
  end

  def Rule(this, pos, &block)
    entry = @table[this, pos]
    if entry.conts.empty? then
      entry.conts << block
      recurse(this.arg, pos) do |pos1, tree|
        return if entry.subsumed?(pos1) 
        entry.results[pos1] = tree
        entry.conts.each do |c|
          c.call(pos1, tree)
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
        s = @factory.Sequence
        lst.each do |l|
          s.elements << l
        end
        block.call(pos, s)
      else
        recurse(this.elements[i], pos) do |pos1, tree|
          f.call(i + 1, pos1, [*lst, tree])
        end
      end
    end
    f.call(0, pos, [])
  end

  def Alt(this, pos, &block)
    this.alts.each do |a|
      recurse(a, pos, &block)
    end
  end

  def Create(this, pos, &block)
    recurse(this.arg, pos) do |pos1, tree|
      block.call(pos1, @factory.Create(this.name, tree))
    end
  end

  def Field(this, pos, &block)
    recurse(this.arg, pos) do |pos1, tree|
      block.call(pos1, @factory.Field(this.name, tree))
    end
  end

  def Value(this, pos, &block)
    with_token(pos, this.kind) do |tk|
      block.call(pos + 1, @factory.Value(this.kind, tk.value, tk.layout))
    end
  end

  def Code(this, pos, &block)
    block.call(pos, @factory.Code(this.code))
  end


#   def Key(this, pos, &block)
#     with_token(pos, 'sym') do |tk|
#       block.call(pos + 1, @factory.Key(tk.value, tk.layout))
#     end
#   end

  def Ref(this, pos, &block)
    with_token(pos, 'sym') do |tk|
      block.call(pos + 1, @factory.Ref(tk.value, tk.layout))
    end
  end

  def Call(this, pos, &block)
    recurse(this.rule, pos, &block)
  end

  def Lit(this, pos, &block)
    with_token(pos, 'lit') do |tk|
      if this.case_sensitive then
        return unless tk.value == this.value
      else 
        return unless tk.value.downcase == this.value.downcase 
      end
      block.call(pos + 1, @factory.Lit(tk.value, this.case_sensitive, tk.layout)) 
    end
  end

  ## NB: iters are right-recursive (otherwise we have to memoize them)
  ## This also means that iter'ed symbols should not be nullable
  ## otherwise they become (hidden) left-recursive as well.

  def Regular(this, pos, &block)
    regular(this, pos) do |pos1, trees|
      t = @factory.Sequence
      trees.each do |k|
        t.elements << k
      end
      block.call(pos1, t)
    end
  end

  # a helper function that produces normal lists of trees
  def regular(this, pos, &block)
    if this.optional && !this.many && !this.sep then
      # X?
      recurse(this.arg, pos) do |pos1, tree|
        block.call(pos1, [tree])
      end
      block.call(pos, [])

    elsif !this.optional && this.many && !this.sep then
      # X+
      recurse(this.arg, pos) do |pos1, tree1|
        regular(this, pos) do |pos2, trees|
          block.call(pos2, [tree1, *trees])
        end
      end
      recurse(this.arg, pos) do |pos1, tree|
        block.call(pos1, [tree])
      end

    elsif this.optional && this.many && !this.sep then
      # X*
      recurse(this.arg, pos) do |pos1, tree1|
        regular(this, pos1) do |pos2, trees|
          block.call(pos2, [tree1, *trees])
        end
      end
      block.call(pos, [])

    elsif !this.optional && this.many && this.sep then
      # {X ","}+
      recurse(this.arg, pos) do |pos1, tree1|
        lit = @grammar_factory.Lit(this.sep, true)
        recurse(lit, pos1) do |pos2, sep|
          regular(this, pos2) do |pos3, trees|
            block.call(pos3, [tree1, sep, *trees])
          end
        end
      end
      recurse(this.arg, pos) do |pos1, tree|
        block.call(pos1, [tree])
      end

    elsif this.optional && this.many && this.sep then
      # pretend it's an IterSep
      iter = this.clone
      iter.optional = false
      recurse(iter, pos, &block)
      # and add empty alternative
      block.call(pos, [])
    else
      raise "Inconsistent Regular: #{this}"
    end
  end

end


