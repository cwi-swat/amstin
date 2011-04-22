
require 'schemagen'
require 'check'

if __FILE__ == $0 then
  ss = SchemaSchema.schema
  puts "****** SCHEMA: #{ss.name} *******"
  ss.classes.each do |c|
    puts "CLASS #{c.name}  (#{c._id})"
    if c.super then
      puts "\tSuper: #{c.super.name}  (#{c.super._id})"
    end
    c.subtypes.each do |s|
      puts "\tSubtype: #{s.name} (#{s._id})"
    end
    puts "\tInstanceof: #{c.instance_of}"
    c.fields.each do |f|
      puts "\tFIELD #{f.name} (#{f._id})"
      puts "\t\ttype #{f.type.name} (#{f.type._id})"
      puts "\t\toptional #{f.optional}"
      puts "\t\tmany #{f.many}"
      puts "\t\tinverse #{f.inverse ? f.inverse.name : nil} (#{f.inverse ? f.inverse._id : nil})"

    end
  end

  puts "Checking #{ss.name}"

  check = Conformance.new(ss, ss)
  check.run
  p check.errors
end

