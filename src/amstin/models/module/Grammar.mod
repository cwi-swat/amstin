module Grammar: Grammar

  start Grammar

  Grammar ::= "start" Rule^ rules:Rule* 

  Rule ::= name:key "::=" alts:{Alt "|"}+

  Alt ::= type:Klass? elements:Element*
  
  Klass ::= "[" name:id "]"
  
  Element ::= label:Label? symbol:Symbol
  
  Label ::= name:id ":"
  
  Symbol ::= [Sym] rule:Rule^
          | [Int] "int"
          | [Str] "str"
          | [Id]  "id"
          | [Ref] ref:id "^"
          | [Lit] value:str
          | [Key] "key"
          | [IterStar] arg:Symbol "*"
          | [Iter] arg:Symbol "+"
          | [Opt] arg:Symbol "?"
          | [IterSepStar] "{" arg:Symbol sep:str "}" "*"
          | [IterSep] "{" arg:Symbol sep:str "}" "+"
	          
