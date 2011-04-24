class Todo
  def initialize
    @todo = []
    @done = {}
  end

  def add(parser, u, pos, w)
    done[pos] ||= Set.new
    conf = Conf.new(parser, u, w)
    unless @done[pos].include?(conf)
      @done[pos] << conf
      @todo << Desc.new(conf, pos)
    end
  end

  def shift
    @todo.shift
  end

  class Conf
    attr_reader :parser, :gss, :node

    def initialize(parser, gss, node)
      @parser = parser
      @gss = gss
      @node = node
    end

    def parse(tokens, pos, eval, gll)
      eval.recurse(parser, tokens, pos, gss, node, gll)
    end

    def ==(o)
      return true if self.equal?(o)
      return false unless o.is_a?(Conf)
      return parser == o.parser && gss == o.gss && node == o.node
    end

    def hash
      parser.hash * 17 + gss.hash * 19 + node.hash * 11
    end
  end

  class Desc
    attr_reader :conf, :pos

    def initialize(conf, pos)
      @conf = conf
      @pos = pos
    end

    def parse(tokens, eval, gll)
      conf.parse(tokens, pos, eval, gll)
    end

    def ==(o)
      return true if self.equal?(o)
      return false unless o.is_a?(Desc)
      return conf == o.conf && pos == o.pos
    end

    def hash
      conf.hash * 7 + token.hash * 13
    end
  end
end
