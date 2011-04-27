
# this assumes absence of sharing in the parser combinator graph
# equality is identity equality

require 'set'


require 'grammar/gll/todo'
require 'grammar/gll/gss'
require 'grammar/gll/sppf'
require 'grammar/gll/item'


########################################
##### TODO: make cu and cn instance vars
########################################

class GLL
  def initialize(input)
    @input = input
    @todo = Todo.new
    @toPop = {}
  end

  def parse(top, eval)
    item = Item.new(top, 1)
    @start = GSS.new(item, 0)
    eval.recurse(top.start, 0, @start, nil, self, item)
    while !@todo.empty? do
      desc = @todo.shift
      desc.parse(eval, self)
    end
  end

  def eof?(pos)
    pos == @input.tokens.length
  end

  def token(pos)
    @input.tokens[pos]
  end

  def add(parser, cu, pos, cn)
    @todo.add(parser, cu, pos, cn)
  end

  def pop(cu, cn, pos)
    return if cu == @start
    @toPop[cu] ||= Set.new
    @toPop[cu] << cn
    cnt = cu.parser
    cu.edges.each do |w, gs|
      gs.each do |u|
        x = prod_node(cnt, w, cn)
        add(cnt, u, pos, x)
      end
    end
  end

  def create(item, cu, cn, pos)
    w = cn
    v = GSS.new(item, pos)
    if v.add_edge(w, cu) then
      if @toPop[v] then
        @toPop[v].each do |z|
          x = prod_node(item, w, z)
          add(item, cu, z.starts, z)
        end
      end
    end
  end

  def prod_node(item, current, nxt)
    # item.dot == 1 && item.arity > 1
    return nxt if current.nil?
    k = nxt.starts
    i = nxt.ends
    j = k

    j = current.i if current
    # was: symbolnode if item.at_end?
    y = ProdNode.new(item, j, i)
    y << PackNode.new(item, k, current, nxt)
    return y
  end     
end
    

class GLLParser

  def recurse(this, *args)
    puts "Sending #{this.schema_class.name}"
    send(this.schema_class.name, this, *args)
  end

  def self.parse(grammar, tokens)
    gll = GLL.new(tokens)
    gll.parse(grammar, self.new)
  end

  def Sequence(this, pos, cu, cn, gll, &block)
    item = Item.new(this.elements, 0)
    if this.elements.empty? then
      cr = gll.EmptyNode.new(pos), pos
      gll.prod_node(item, cn, cr)
      gll.pop(cu, cn, pos)
    else
      recurse(item, pos, cu, cn, gll, &block)
    end
  end

  def BinarySeq
    
    

  def Item(this, pos, cu, cn, gll)
    return gll.pop(cu, cn, pos) if this.at_end? 
    
    cu = gll.create(nxt, cu, cn, pos)
    # can we pass the new cu here?
    recurse(this.elt, cu, cn, gll) do |cr|
      # in the block so a terminal
      nxt = this.move
      cn = gll.prod_node(nxt, cn, cr)
      return recurse(nxt, cu, cn, pos + 1, gll)
    end

    # if we come here


    
  end
        
  def Empty(this, pos, cu, cn, gll)
    item = yield gll.EmptyNode.new(pos), pos
  end

  def Call(this, pos, cu, cn, gll, &block)
    recurse(this.rule, cu, cn, gll, &block)
  end

  def Rule(this, pos, cu, cn, gll, &block)
    recurse(this.arg, pos, cu, cn, gll)
  end

  def Alt(this, pos, cu, cn, gll, &block)
    this.alts.each do |alt|
      gll.add(alt, cu, pos, nil)
    end
  end

  def Create(this, pos, cu, cn, gll, &block)
    # todo: something with this.name
    recurse(this.arg, pos, cu, cn, gll, &block)
  end

  def Field(this, pos, cu, cn, gll, &block)
    # todo: something with this.name
    recurse(this.arg, pos, cu, cn, &block)
  end

 
  def Value(this, pos, cu, cn, gll)
    return if gll.eof?(pos)
    tk = gll.token(pos)
    if tk.kind == this.kind then
      yield TermNode.new(tk, pos), pos + 1
    end
  end

  def Lit(this, pos, cu, cn, gll, item)
    return if gll.eof?(pos)
    tk = gll.token(pos)
    if tk.kind == "Lit" then
      if (this.case_sensitive && tk.value == this.value) ||
          (!this.case_sensitive && tk.value.downcase == this.value.downcase) then
        yield TermNode.new(tk, pos), pos + 1
      end
    end
  end

  def Key(this, pos, cu, cn, gll, item)
    return if gll.eof?(pos)
    tk = gll.token(pos)
    if tk.kind == "Sym" then
      yield TermNode.new(this, tk, pos), pos + 1
    end
  end

  def Ref(this, pos, cu, cn, gll, item)
    return if gll.eof?(pos)
    tk = gll.token(pos)
    if tk.kind == "Sym" then
      yield TermNode.new(this, tk, pos), pos + 1
    end
  end

  def Regular(this, pos, cu, cn, gll)
    if !this.many && this.optional then
      gll.add(Empty.new, cu, pos, nil)
      gll.add(this.arg, cu, pos, nil)
    elsif this.many && !this.optionla && !this.sep then
      gll.add(this.arg, cu, pos, nil)
      gll.add(Item.new([this.arg, this]), cu, pos, nil)
    elsif this.many && this.optional && !this.sep then
      gll.add(Empty.new)
      gll.add(Item.new([this.arg, this]), cu, pos, nil)
    elsif this.many && !this.optional && this.sep then
      gll.add(this.arg, cu, pos, nil)
      gll.add(Item.new([this.arg, this.sep, this]), cu, pos, nil)
    elsif this.many && this.optional && this.sep then
      # ???
    end
  end

end


if __FILE__ == $0 then
  require 'grammar/gamma2'
  require 'grammar/tokenize'

  tok = Tokenize.new("b")
  src = "b b b b b"
  input = tok.tokenize("S", src)

  gamma2 = Gamma2.grammar
  GLLParser.parse(gamma2, input)
end
  
