-- scrieti mapM_, forM_, pentru tipuri 
-- care au foldable drept clasa de tipuri

forM_' :: Monad m => [a] -> (a -> m b) -> m ()
forM_' [] _ = pure () 
forM_' (x:xs) f = f x >> forM_' xs f 

mapM_' :: Monad m => (a -> m b) -> [a] -> m ()
mapM_' _ [] = return () 
mapM_' f (x:xs) =  do 
    f x 
    mapM_' f xs 