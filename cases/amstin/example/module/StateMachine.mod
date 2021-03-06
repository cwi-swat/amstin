module StateMachine: Grammar

  start StateMachine

  StateMachine ::= decls:Decl*

  Decl ::= State | Commands | Events

  State ::= "state" name:key actions:Actions? transitions:Trans* "end"

  Actions ::= "actions" "{" commands:Command^* "}"

  Trans ::= event:Event^ "=>" state:State^

  Commands ::= "commands" commands:Command+ "end"

  Command ::= name:key code:id

  Events ::= "events" events:Event+ "end"

  Event ::= name:key code:id

  