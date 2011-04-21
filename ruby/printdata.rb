
require 'data'
require 'model'

require 'cyclicmap'


class PrintDataModel < CyclicMap
  def MetaModel(obj, result)
    puts "METAMODEL #{obj.name}"
    obj.classes.each { |x| recurse(x) }
  end

  def Class(obj, result)
    puts "CLASS #{obj.name} #{obj.parent ? obj.parent.type.name : ""}"
    obj.fields.each { |x| recurse(x) }
  end

  def Field(obj, result)
    puts "\tFIELD #{obj.name} #{obj.type.name} #{obj.mult.klass}"
  end
end

dm = dataData()

pdm = PrintDataModel.new(dm)
pdm.run
