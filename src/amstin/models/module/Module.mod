module Module: Grammar

start Module

Module ::= "module" name:Name ":" syntax:mod imports:Import* contents:Contents
  
Import ::= [One] "import" name:mod ";" | [All] "import" name:mod "." "*" ";"
  
  // unspecified (the syntax defined by the Rule referred to in "syntax")
Contents ::= 

   
