

Schema ::=  V[tables]

Table ::= H[KW["create"] KW["table"] VAR[name] "(" I[V sep=H hs=0 [_ ","] [columns]] ")"]

Column ::= H[VAR[name] type modifiers]

Type ::= [Int] KW["int"]
       | [VarChar] H hs=0[KW["varchar"] "(" NUM[length] ")"]
       | [Real] KW["real"]
       | [Bool] KW["bool"]

Modifier ::= [NotNull] H[KW["not"] KW["null"]]
          | [Ref] H[KW["references"] VAR[table]]
          | [Key] H[KW["primary" "key"]]
  
  
