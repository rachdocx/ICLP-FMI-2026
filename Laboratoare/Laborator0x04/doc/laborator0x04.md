# Laborator 0x04

## pb1.java: Future si Callable

Problema prezinta folosirea interfetei `Callable` impreuna cu `FutureTask` pentru executarea unui task care returneaza un rezultat.

### Future si Callable

```java
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class Main {
    public static void main(String[] args) {
      // Callable<String> task = new MyTask();
      Callable<String> task = () -> {
        Thread.sleep(2000);
        return "Task completed";
      };

      FutureTask<String> futureTask = new FutureTask<>(task);

      Thread thread = new Thread(futureTask);
      thread.start();

      try {
        System.out.println("Main...");
        String result = futureTask.get();
        System.out.println("Result is: " + result);
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
}

class MyTask implements Callable<String> {
  @Override
  public String call() {
    try {
      Thread.sleep(2000);
      return "Task completed";
    }
    catch (InterruptedException ex) {
      ex.printStackTrace();
      return "ERROR";
    }
  }
}
```

## pb2.java: Crawler cu Callable si FutureTask

scriem un Crawler cu Callable si FutureTask
vrem sa accesam continutul unor site-uri si sa afisam acest continut la stdout

```java
// scriem un Crawler cu Callable si FutureTask
// vrem sa accesam continutul unor site-uri si sa afisam acest continut la stdout

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import java.net.URL;
import java.net.HttpURLConnection;

import java.util.List;
import java.util.ArrayList;


public class Main {
    public static void main(String[] args) {
      List<String> urls = List.of(
        "https://fmi.unibuc.ro",
        "https://jsonplaceholder.typicode.com/posts/1",
        "https://jsonplaceholder.typicode.com/posts/2",
        "https://jsonplaceholder.typicode.com/posts/3",
        "https://google.com"
      );

      List<FutureTask<String>> futureTasks = new ArrayList<>();

      for (String url : urls) {
        Callable<String> task = new CrawlerTask(url);
        FutureTask<String> futureTask = new FutureTask<>(task);
        futureTasks.add(futureTask);
        Thread thread = new Thread(futureTask);
        thread.start();
      }

      for (FutureTask<String> futureTask : futureTasks) {
        try {
          String content = futureTask.get();
          System.out.println("Page content:\n" + content);
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
  }
}

class CrawlerTask implements Callable<String> {
  private String url;

  public CrawlerTask(String url) {
    this.url = url;
  }

  @Override
  public String call() {
    StringBuilder content = new StringBuilder();

    try {
      System.out.println("Accessing: " + url);

      // pregatim requestul http
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);

      int status = connection.getResponseCode();

      if (status == 200) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
          content.append(line).append("\n");
        }
        reader.close();
      }
      else {
        content.append("Cannot acces ").append(url).append(status);
      }
      connection.disconnect();
    }
    catch (Exception ex) {
      ex.printStackTrace();
      return "Error " + url;
    }

    return content.toString();
  }
}
```

## pb3.java: CompletableFuture si calculul numerelor Fibonacci

Problema prezinta executarea asincrona a functiei Fibonacci folosind `CompletableFuture`.

```java
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
      int n = 20;
      System.out.println("Fibonacci for n = " + n);

      CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> fibonacci(n));
      future.thenAccept(result -> {
        System.out.println("Fibonacci(" + n + ") = " + result);
      });

      System.out.println("MAIN");

      future.join();
    }

    public static long fibonacci(int n) {
      if (n <= 1) {
        return n;
      }
      return fibonacci(n - 1) + fibonacci(n - 2);
    }
}
```

## pb4.java: Incrementarea unui contor cu variabile atomice

incrementarea unui contor cu variabile atomice

```java
// incrementarea unui contor cu variabile atomice

import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {
      Thread t1 = new Thread(new Runnable() {
        public void run() {
          for (int i = 0; i < 1000; i++) {
            counter.incrementAndGet();
          }
        }
      });

      Thread t2 = new Thread(new Runnable() {
        public void run() {
          for (int i = 0; i < 1000; i++) {
            counter.incrementAndGet();
          }
        }
      });

      t1.start();
      t2.start();

      t1.join();
      t2.join();

      System.out.println("Counter is " + counter);
    }
}
```

## pb5.java: CountDownLatch si ExecutorService

Problema prezinta folosirea unui `CountDownLatch` impreuna cu un `ExecutorService` si un contor atomic.

apelul este blocant pana cand toate cele trei thread-uri create dau semnalul countDown

```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
      CountDownLatch controller = new CountDownLatch(3);

      ExecutorService pool = Executors.newCachedThreadPool();

      AtomicInteger counter = new AtomicInteger(0);

      for (int i = 0; i < 3; i++) {
        pool.execute(new MyTask(controller, counter));
      }

      controller.await();       // apelul este blocant pana cand toate cele trei thread-uri create dau semnalul countDown

      System.out.println("Counter ***** await: " + counter);

      pool.shutdown();
      try {
        if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
          pool.shutdownNow();
        }
      }
      catch (InterruptedException ex) {
        ex.printStackTrace();
        pool.shutdownNow();
      }

      System.out.println("Counter la final: " + counter);
    }
}

class MyTask implements Runnable {
  private AtomicInteger counter;
  private CountDownLatch controller;

  public MyTask(CountDownLatch controller, AtomicInteger counter) {
    this.controller = controller;
    this.counter = counter;
  }

  @Override
  public void run() {
    for (int i = 0; i < 100; i++) {
      counter.incrementAndGet();
    }
    controller.countDown();
    for(int i = 0; i < 1000; i++) {
      counter.incrementAndGet();
    }
  }
}
```

## pb6.java: CyclicBarrier si sincronizarea thread-urilor

Problema prezinta sincronizarea mai multor thread-uri in doua etape folosind `CyclicBarrier`.

```java
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) {
      CyclicBarrier barrier = new CyclicBarrier(3);

      for (int i = 0; i < 3; i++) {
        int id = i;

        new Thread(() -> {
          try {
            System.out.println("Thread " + id + " - stage 1");
            Thread.sleep(1000 * (id + 1));

            barrier.await();

            System.out.println("Thread " + id + " - stage 2");
            barrier.await();

            System.out.println("Thread " + id + " finalized");
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }).start();
      }
    }
}
```

## pb7.hs: Recapitulare Haskell si NetworkWriter

recapitulare Haskell.

([], (:))

[] :: List a
(:) :: a -> List a -> List a

"ab32bas2a" => 12

```haskell
import Data.Char
import Data.Maybe
import Control.Monad
import Control.Applicative
import Data.List

-- recapitulare Haskell.
-- ([], (:))
-- [] :: List a
-- (:) :: a -> List a -> List a
removeOddHalfEven :: [Int] -> [Int]
removeOddHalfEven [] = []
removeOddHalfEven (x:xs)
    | even x = x `div` 2 : removeOddHalfEven xs
    | otherwise = removeOddHalfEven xs

removeOddHalfEvenHOF :: [Int] -> [Int]
removeOddHalfEvenHOF list = map (\x -> x `div` 2) $ filter even list

removeOddHalfEvenComp :: [Int] -> [Int]
removeOddHalfEvenComp list = [ x `div` 2 | x <- list, even x ]

-- "ab32bas2a" => 12
multDigits :: String -> Int
multDigits str = foldr (*) 1 [digitToInt ch | ch <- str, isDigit ch]

doubleOddLTn :: [Int] -> Int -> [Int]
doubleOddLTn = foldr f i
    where
        f = \x u n -> if (odd x && x < n) then 2 * x : u n else u n
        i = \_ -> []

data LList a = Nil | Cons a (LList a)

l1 :: LList Int
l1 = Cons 1 $ Cons 2 $ Cons 3 Nil

l2 :: LList Int
l2 = Cons 4 $ Cons 5 Nil

instance Eq a => Eq (LList a) where
    Nil == Nil              = True
    Cons x xs == Cons y ys  = x == y && xs == ys
    _ == _                  = False

instance Show a => Show (LList a) where
    show Nil = "[]"
    show (Cons x xs) = "[" ++ show x ++ " | " ++ show xs ++ "]"

lAppend :: LList a -> LList a -> LList a
lAppend Nil l2 = l2
lAppend (Cons x xs) l2 = Cons x $ lAppend xs l2

instance Semigroup (LList a) where
    (<>) = lAppend

instance Monoid (LList a) where
    mempty = Nil

lFilter :: (a -> Bool) -> LList a -> LList a
lFilter _ Nil = Nil
lFilter p (Cons x xs)
    | p x = Cons x $ lFilter p xs
    | otherwise = lFilter p xs

lMap :: (a -> b) -> LList a -> LList b
lMap _ Nil = Nil
lMap f (Cons x xs) = Cons (f x) $ lMap f xs

lFoldr :: (a -> b -> b) -> b -> LList a -> b
lFoldr _ i Nil = i
lFoldr f i (Cons x xs) = f x $ lFoldr f i xs

instance Foldable LList where
    foldr = lFoldr

lToList :: LList a -> [a]
lToList Nil = []
lToList (Cons x xs) = x : (lToList xs)

normalListToLList :: [a] -> LList a
normalListToLList [] = Nil
normalListToLList (x:xs) = Cons x $ normalListToLList xs

data Point a b = Point a b

instance (Eq a, Eq b) => Eq (Point a b) where
    Point x y == Point x' y' = x == x' && y == y'

instance (Show a, Show b) => Show (Point a b) where
    show (Point x y) = "(" ++ show x ++ ", " ++ show y ++ ")"

lZip :: LList a -> LList b -> LList (Point a b)
lZip Nil _ = Nil
lZip _ Nil = Nil
lZip (Cons x xs) (Cons y ys) = Cons (Point x y) $ lZip xs ys

instance Functor LList where
    fmap = lMap

-------------



type IPAddress = String
data EquipmentType = Router | Switch | Host | Server
data NetworkEquipment = NetworkEquipment
    { eqName :: String,
      eqType :: EquipmentType,
      ipAddresses :: [IPAddress]
    }

instance Show EquipmentType where
    show Router = "Router"
    show Switch = "Switch"
    show Host = "Host"
    show Server = "Server"

instance Eq EquipmentType where
    Router == Router = True
    Switch == Switch = True
    Host == Host = True
    Server == Server = True
    _ == _ = False

instance Show NetworkEquipment where
    show (NetworkEquipment name eqType ipAddresses) = "NE { name = " ++ show name
        ++ ", type = " ++ show eqType
        ++ ", ipAddresses = " ++ show ipAddresses ++ " }"

instance Eq NetworkEquipment where
    (NetworkEquipment name1 eqType1 ipAddresses1) == (NetworkEquipment name2 eqType2 ipAddresses2) =
        name1 == name2 && eqType1 == eqType2 && ipAddresses1 == ipAddresses2

type Log = [String]
data NetworkWriter a = NetworkWriter { runNetworkWriter :: (a, Log) }
    deriving Show

instance Functor NetworkWriter where
    fmap f (NetworkWriter (x, log)) = NetworkWriter (f x, log)

instance Applicative NetworkWriter where
    pure x = NetworkWriter (x, [])
    (NetworkWriter (f, logf)) <*> (NetworkWriter (x, logx)) = (NetworkWriter (f x, logf ++ logx))

instance Monad NetworkWriter where
    return = pure
    (NetworkWriter (x, logx)) >>= f =
        let (NetworkWriter (y, logy)) = f x
        in (NetworkWriter (y, logx ++ logy))

main :: IO ()
main = do
    putStrLn "Hello, world!"
```