
stm.grammar.ast: "stm.txt" grammar.parser
grant.stm.ast: "grant.txt" stm.parser

%1.%2.ast: "%1.txt" %2.parser
	$2.parse($1)



stm.java: stm.meta

%.java: %.meta
	metaModelToJava($1)




stm.grammar: stm.grammar.ast grammar.java
grant.stm: grant.stm.ast stm.java

%1.%2: %1.%2.ast %2.java
	astToModel($1)


stm.parser: stm.grammar
	
%.parser: %.grammar
	grammarToParser


stm.meta: stm.grammar meta.java

%.meta: %.grammar meta.meta
	grammarToMetaModel



grant.stm: "grant.stm" stm.parser

%1.%2: "%1.%2" %2.parser	
		