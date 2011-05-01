-- The pretty printer

infixr 6 :<>
infixr 6 <>

data DOC = NIL
  | DOC :<> DOC
  | NEST Int DOC
  | TEXT String
  | LINE String
  | GROUP DOC  --  :<|> DOC

x <> y = x :<> y

-- group x = flatten x :<|> x

flatten :: DOC -> DOC
flatten NIL = NIL
flatten (x :<> y) = flatten x :<> flatten y
flatten (NEST i x) = NEST i (flatten x)
flatten (TEXT s) = TEXT s
flatten (LINE s) = TEXT s
flatten (GROUP x) = flatten x  -- (x :<|> y) = flatten x

repeatn :: (Enum a, Num a) => a -> t -> [t]
repeatn i x = [ x | _ <- [1..i] ]

{--
best :: Int -> Int -> DOC -> Doc
best w k x = be w k [(0,x)]

be :: Int -> Int -> [(Int, DOC)] -> Doc
be w k [] = Nil
be w k ((i,NIL):z) = be w k z
be w k ((i,x :<> y):z) = be w k ((i,x):(i,y):z)
be w k ((i,NEST j x):z) = be w k ((i+j,x):z)
be w k ((i,TEXT s):z) = s `Text` be w (k+length s) z
be w k ((i,LINE s):z) = i `Line` be w i z
be w k ((i,GROUP x):z) = better w k (be w k ((i,flatten x):z))
                                     (be w k ((i,x):z))
--}
best :: Int -> Int -> DOC -> [[Char]]
best w k x = fst(be w k 0 x)

be :: Int -> Int -> Int -> DOC -> ([[Char]], Int)
be w k i NIL = ([""],0)
be w k i (x :<> y) = 
  let (a,ai) = be w k i x in
  let (b,bi) = be w (k + ai) i y in
    (a ++ b, bi)
be w k i (NEST j x) = be w k (i+j) x
be w k i (TEXT s) = ([s], k+length s)
be w k i (LINE s) = (['\n' : repeatn i ' '], i)
be w k i (GROUP x) = better w k (be w k i (flatten x))
                                (be w k i x)

--better :: Int -> Int -> Doc -> Doc -> Doc
better w k x y = if fits (w-k) x then x else y

-- fits :: Int -> Doc -> Bool
fits w (x,n) = sum [ length s | s <- x] < w
--fits w x | w < 0 = False
--fits w Nil = True
--fits w (s `Text` x) = fits (w - length s) x
--fits w (i `Line` x) = True

pretty :: Int -> DOC -> [Char]
pretty w x = layout (best w 0 x)

layout x = foldl (++) "" x

-- Tree example

data Tree = Node String [Tree]
showTree (Node s ts) = GROUP (TEXT s <> NEST (length s) (showBracket ts))
showBracket [] = NIL
showBracket ts = TEXT "[" <> NEST (0) (showTrees ts) <> TEXT "]"
showTrees [t] = showTree t
showTrees (t:ts) = showTree t <> LINE "" <> TEXT ", " <> showTrees ts

-- format as 
showTree' (Node s ts) = TEXT s <> showBracket' ts
showBracket' [] = NIL
showBracket' ts = GROUP (TEXT "[" <>
                       NEST 2 (LINE " " <> (showTrees ts)) <>
                       LINE " " <> TEXT "]")

tree = Node "aaa" [
   Node "bbbbb" [
   Node "ccc" [],
   Node "dd" []
   ],
   Node "eee" [],
   Node "ffff" [
   Node "gg" [],
   Node "hhh" [],
   Node "ii" []
   ]
   ]
testtree w = do
  putStr (pretty w (showTree tree))
  putStr "\n"
testtree' w = do
  putStr (pretty w (showTree' tree))
  putStr "\n"

