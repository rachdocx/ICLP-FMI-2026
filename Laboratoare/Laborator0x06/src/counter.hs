-- faceti un counter fara data race uri un thread pe main si un thread scris

import Control.Concurrent
import Control.Concurrent.MVar
import Control.Monad

takeprint :: MVar String -> IO ()
takeprint stdo = do 
    msg <- takeMVar stdo
    putStrLn msg

increment :: MVar Int -> MVar String -> IO ()
increment counter stdo =
    replicateM_ 10 $ do
        value <- takeMVar counter
        let nextValue = value + 1
        putMVar counter nextValue
        
        putMVar stdo (show nextValue)

main :: IO ()
main = do 
    stdo <- newEmptyMVar
    counter <- newMVar 0

    forkIO $ increment counter stdo

    replicateM_ 10 $ takeprint stdo
