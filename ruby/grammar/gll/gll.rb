
# this assumes absence of sharing in the parser combinator graph
# equality is identity equality

require 'set'


require 'todo'
require 'gss'
require 'sppf'


class GLL

  def parse(top, tokens, eval)
    @start = GSS.new(top, 0)
    add(top, 0, nil)
    while !@todo.empty? do
      desc = @todo.shift
      desc.parse(tokens, eval, self)
    end
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

  def create(parser, cu, cn, pos)
    w = cn
    v = GSS.new(parser, pos)
    if v.add_edge(w, cu) then
      if @toPop[v] then
        @toPop[v].each do |z|
          x = prod_node(parser, w, z)
          add(parser, cu, z.starts, z)
        end
      end
    end
  end

  def prod_node(parser, current, nxt)
    # item.dot == 1 && item.arity > 1
    return nxt if current.nil?
    k = nxt.starts
    i = nxt.ends
    j = k

    j = current.i if current
    # was: symbolnode if item.at_end?
    y = ProdNode.new(parser, j, i)
    y << PackNode.new(parser, k, current, nxt)
    return y
  end      


end

class GLLParser < CyclicMap # no need to be cyclic?

  def parse(grammar, tokens)
    gll = GLL.new
    gll.parse(grammar, tokens, 0, self)
  end
  
  def Rule(this, tokens, pos, cu, cn, gll, cont)
    recurse(this.pattern, tokens, pos, cu, cn, gll, cont)
    gll.pop(cu, cn, pos)
  end

  def Seq(this, tokens, pos, cu, cn, gll, cont)
    recurse(this.left, tokens, pos, cu, cn, gll, cont.unshift(this.right))
  end

  def Or(this, tokens, pos, cu, cn, gll, cont)
    cu = gll.create(cont, cu, cn, pos)
    gll.add(this.left, cu, pos, null)
    gll.add(this.right, cu, pos, null)
  end

  def Empty(this, tokens, pos, cu, cn, gll, cont)
    cr = EmptyNode.new(pos)
    cn = gll.prod_node(this, cn, cr)
  end

  def Term(this, tokens, pos, cu, cn, gll, cont)
    tk = tokens[pos] # eof?
    if check(this, tk) then
      cr = TermNode.new(this, tk, pos)
      nxt = cont.shift
      cn = gll.prod_node(nxt, cn, cr)
      recurse(nxt, tokens, pos + 1, cu, cn, gll, cont)
    end
  end


end
