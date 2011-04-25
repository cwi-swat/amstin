# to find tests:
#  fgrep -R __FILE__ . | fgrep -v svn

JRUBY = ENV['JRUBY_HOME']

def test(file, dest, diff)
  file =~ /(.*)\/[^\/]*/
  dir = $1
  puts "Processing #{file}"
  
  %x[mkdir -p #{dest}/#{dir}]
  %x[#{JRUBY}/bin/jruby --1.9 #{file} > #{dest}/#{file}.out]

  if diff == "full"
    `diff tests/out/#{file}.out tests/valid/#{file}.out 1>&2`
  elsif diff
    out = `diff tests/out/#{file}.out tests/valid/#{file}.out -q`
    if out != ""
      puts "  **TEST FAILED**"
    end
  end
end

def process(dest, diff)
  test "grammar/derivative.rb", dest, diff
  test "grammar/grammargrammar.rb", dest, diff
  test "grammar/grammarschema.rb", dest, diff
  test "grammar/tokenize.rb", dest, diff
  test "schema/checkschema.rb", dest, diff
  test "schema/schemaschema.rb", dest, diff
  test "tools/copy.rb", dest, diff
  test "tools/print.rb", dest, diff
end

if ARGV[0] == "-valid"
  puts "creating valid outputs"
  process("tests/valid", false)
elsif ARGV[0]
  test ARGV[0], "tests/out", "full"
else
  puts "checking all"
  process("tests/out", true)
end
