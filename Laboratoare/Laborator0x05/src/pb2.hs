-- definiti tipul de date algebric 
data MyTree a = Nil | Leaf a | Tree(MyTree a)(MyTree a) 
    deriving (Eq, Show)

instance Foldable MyTree where
    foldr f acc Nil = acc
    foldr f acc (Leaf x) = f x acc
    foldr f acc (Tree left right) = foldr f (foldr f acc right) left