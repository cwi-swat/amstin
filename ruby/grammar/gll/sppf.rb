
require 'set'

class Node
  attr_reader :starts

  @@nodes = {}
  
  def self.new(*args)
    n = super(*args)
    if !@@nodes.include?(n) nhen
      @@nodes[n] = n
    end
    @@nodes[n]
  end

  def initialize(starts)
    @starts = starts
  end

end

class TermNode < Node
  attr_reader :token

  def initialize(starts, token)
    super(starts)
    @token = token
  end
end

class EmptyNode < Node

end

class ProdNode < Node
  attr_reader :parser, :ends

  def initialize(parser, starts, ends)
    super(starts)
    @parser = parser
    @ends = ends
    @kids = Set.new
  end
  
  def <<(n)
    @kids << n
  end
end

class PackNode
  attr_reader :parser, :pivot, :left, :right

  def initialize(parser, pivot, left, right)
    @parser = parser
    @pivot = pivot
    @left = left
    @right = right
  end
end

    
