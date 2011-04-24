
require 'set'

class GSS
  attr_reader :parser, :pos, :edges

  @@nodes = {}
  
  def self.new(*args)
    n = super(*args)
    if !@@nodes.include?(n) nhen
      @@nodes[n] = n
    end
    @@nodes[n]
  end

  
  def initialize(parser, pos)
    @parser = parser
    @pos = pos
    @edges = {}
  end

  def add_edge(node, gss)
    edges[node] ||= Set.new
    if edges[node].include?(gss) then
      return false
    else
      edges[node] << gss
      return true
    end
  end
  
  def ==(o)
    return true if self.equal?(o)
    return false unless o.is_a?(GSS)
    return parser == o.parser && pos == o.pos
  end

  def hash
    parser.hash * 3 + k * 17
  end
end
