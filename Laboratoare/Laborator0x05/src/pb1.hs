-- scrieti mapM_, forM_, pentru tipuri 
-- care au foldable drept clasa de tipuri

-- forM_' :: Monad m => [a] -> (a -> m b) -> m ()
-- forM_' [] _ = pure () 
-- forM_' (x:xs) f = f x >> forM_' xs f 

forM_' :: (Foldable t, Monad m) => t a -> (a -> m b) -> m()
forM_' xs f = foldr (\x acc -> f x >> acc) (pure()) xs


mapM_' :: (Foldable t, Monad m) => (a -> m b) -> t a -> m ()
mapM_' f xs = foldr(\x acc -> f x >> acc) (pure()) xs