
require 'ostruct'

class Item

  # Pretend to be a model
  @@schema_class = OpenStruct.new
  @@schema_class.name = "Item"


  def initialize(elts, dot = 0)
    @elts = elts
    @dot = dot
  end

  def move
    Item.new(@elts, dot + 1)
  end

  def each(&block)
    @dot.upto(@elts.length - 1) do |i|
      yield @elts[i], Item.new(@elts, i + 1)
    end
  end

  def final?
    @dot == @elts.length
  end

  def schema_class
    @@schema_class
  end



end
