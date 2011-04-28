
require 'cyclicmap'

class CollectLiterals < CyclicExecOtherwise
  def initialize
    super
    @literals = []
  end

  def pattern
    # TODO: sort the on length
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
    if this.case_sensitive then
      if this.value =~ /[\\a-zA-Z_][a-zA-Z0-9_$]/ then
        @literals << "(" + Regexp.escape(this.value) + "(?![a-zA-Z0-9_$])" + ")"
      else
        @literals << Regexp.escape(this.value)
      end
    else
      @literals << "(" + ci_pattern(this.value) + "(?![a-zA-Z0-9_$])" + ")"
    end
  end

  def Regular(this)
    if this.sep then
      @literals << Regexp.escape(this.sep)
    end
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

  def initialize(input, factory, gf = Factory.new(GrammarSchema.schema))
    @input = input
    @table = Table.new
    @factory = factory
    @grammar_factory = gf 
  end

  def run(grammar)
    begin
      recurse(grammar, 0) do |pos, tree|
        if pos == @input.tokens.length then
          pt = @factory.ParseTree(@input.path, tree, @input.layout)
          raise Success.new(pt)
        end
      end
    rescue Success => e
      return e.tree
    end
    nil
  end

  def recurse(obj, *args, &block)
    ##puts "Sending #{obj.schema_class.name}"
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
    #puts "Parsing rule #{this.name}"
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
    #puts "Sequence"
    f = lambda do |i, pos, lst| 
      if i == this.elements.length then
        #puts "#SEQUENCE: #{lst}"
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
    #debug(this, pos)
    recurse(this.arg, pos) do |pos1, tree|
      #puts "CREATE: #{tree}"
      block.call(pos1, @factory.Create(this.name, tree))
    end
  end

  def Field(this, pos, &block)
    #debug(this, pos)
    recurse(this.arg, pos) do |pos1, tree|
      block.call(pos1, @factory.Field(this.name, tree))
    end
  end

  def Value(this, pos, &block)
    #debug(this, pos)
    return if eof?(pos)
    tk = token(pos)
    if tk.kind == this.kind then
      block.call(pos + 1, @factory.Value(this.kind, tk.value, tk.layout))
    end
  end

  def Code(this, pos, &block)
    block.call(pos, @factory.Code(this.code))
  end


  def Key(this, pos, &block)
    #debug(this, pos)
    return if eof?(pos)
    tk = token(pos)
    if tk.kind == "sym" then
      block.call(pos + 1, @factory.Key(tk.value, tk.layout))
    end
  end

  def Ref(this, pos, &block)
    #debug(this, pos)
    return if eof?(pos)
    tk = token(pos)
    if tk.kind == "sym" then
      block.call(pos + 1, @factory.Ref(tk.value, tk.layout))
    end
  end

  def Call(this, pos, &block)
    #debug(this, pos)
    recurse(this.rule, pos, &block)
  end

  def Lit(this, pos, &block)
    #debug(this, pos)
    return if eof?(pos)
    tk = token(pos)
    if tk.kind == "lit" then
      if this.case_sensitive then
        if tk.value == this.value then
          block.call(pos + 1, @factory.Lit(tk.value, true, tk.layout)) 
        end
      else
        if tk.value.downcase == this.value.downcase then
          block.call(pos + 1, @factory.Lit(tk.value, false, tk.layout)) 
        end
      end
    end
  end

  ## NB: iters are right-recursive (otherwise we have to memoize them)
  ## This also means that iter'ed symbols should not be nullable

  def Regular(this, pos, &block)
    regular(this, pos) do |pos1, trees|
      t = @factory.Sequence
      trees.each do |k|
        t.elements << k
      end
#       t.optional = this.optional
#       t.many = this.many
#       t.sep = this.sep ? this.sep.value : nil
      block.call(pos1, t)
    end
  end

  def regular(this, pos, &block)
    if this.optional && !this.many && !this.sep then
      recurse(this.arg, pos) do |pos1, tree|
        block.call(pos1, [tree])
      end
      block.call(pos, [])
    elsif !this.optional && this.many && !this.sep then
      recurse(this.arg, pos) do |pos1, tree1|
        regular(this, pos) do |pos2, trees|
          block.call(pos2, [tree1, *trees])
        end
      end
      recurse(this.arg, pos) do |pos1, tree|
        block.call(pos1, [tree])
      end
    elsif this.optional && this.many && !this.sep then
      recurse(this.arg, pos) do |pos1, tree1|
        regular(this, pos1) do |pos2, trees|
          block.call(pos2, [tree1, *trees])
        end
      end
      block.call(pos, [])
    elsif !this.optional && this.many && this.sep then
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


require 'grammar/tokenize'
require 'grammar/parsetree'
require 'grammar/grammargrammar'
require 'grammar/unparse'
require 'grammar/instantiate'

require 'tools/print'

def grammar_grammar

#   grammar = GrammarGrammar.grammar
#   coll_lits = CollectLiterals.new
#   coll_lits.recurse(grammar)
#   tokenizer = Tokenize.new(coll_lits.pattern)
#   path = 'grammar/grammar.grammar'
#   src = File.read(path)
#   input = tokenizer.tokenize(path, src) 
#   parse = CPSParser.new(input, Factory.new(ParseTreeSchema.schema))
#   tree = parse.run(grammar)

  grammar = GrammarGrammar.grammar
  tree = parse_it('grammar/grammar.grammar', grammar)

  unparse = Unparse.new($stdout)
  unparse.recurse(tree)

  inst = Instantiate.new(Factory.new(GrammarSchema.schema))
  grammar2 = inst.run(tree)

  File.open('g1.txt', 'w') do |f|
    Print.new(f).recurse(grammar, GrammarSchema.print_paths)
  end
  File.open('g2.txt', 'w') do |f|
    Print.new(f).recurse(grammar2, GrammarSchema.print_paths)
  end
  
  system "diff g1.txt g2.txt"
  
  #Print.recurse(tree)
end


def schema_grammar
  puts "Parsing schema.grammar"
  grammar = GrammarGrammar.grammar
  tree = parse_it('schema/schema.grammar', grammar)
  unparse = Unparse.new($stdout)
  unparse.recurse(tree)

  inst = Instantiate.new(Factory.new(GrammarSchema.schema))
  grammar2 = inst.run(tree)

  #Print.new.recurse(grammar2, GrammarSchema.print_paths)
  
  # now we have a grammar for schemas, let's parse with it
  puts "Parsing schema.schema"

  tree = parse_it('schema/schema.schema', grammar2)
  p tree
  unparse = Unparse.new($stdout)
  unparse.recurse(tree)

  inst2 = Instantiate.new(Factory.new(SchemaSchema.schema))
  schema_schema = inst2.run(tree)

  #Print.new.recurse(schema_schema, SchemaSchema.print_paths)
  File.open('s1.txt', 'w') do |f|
    Print.new(f).recurse(SchemaSchema.schema, SchemaSchema.print_paths)
  end
  File.open('s2.txt', 'w') do |f|
    Print.new(f).recurse(schema_schema, SchemaSchema.print_paths)
  end

  system "diff s1.txt s2.txt"
  
end

def parse_it(path, grammar)
  coll_lits = CollectLiterals.new
  puts "Collecting literals"
  coll_lits.recurse(grammar)
  puts coll_lits.pattern
  tokenizer = Tokenize.new(coll_lits.pattern)
  src = File.read(path)
  puts "Tokenizing #{path}"
  input = tokenizer.tokenize(path, src) 
  parse = CPSParser.new(input, Factory.new(ParseTreeSchema.schema))
  parse.run(grammar)
end

if __FILE__ == $0 then
  #grammar_grammar
  schema_grammar
end


