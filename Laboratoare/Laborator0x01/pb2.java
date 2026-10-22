import java.util.*;

public class Main {
    public static void main(String[] args) {
      

      //Thread myThread = new Thread(new MyThreadExtendsThread());
      //myThread.start();
      (new Thread(new MyThreadExtendsThread())).start();
      (new Thread(new MyThreadImplementsRunnable())).start();

      for (int i = 0; i < 100; i++) {
        System.out.println("[Main] Hello from " + Thread.currentThread().threadId());
      }

      (new Thread(() -> {
        for (int i = 0; i < 100; i++) {
          System.out.println("[Anonymous] Hello from " + Thread.currentThread().threadId());
        }
      })).start();
    }
}

// putem extinde clasa Thread 

class MyThreadExtendsThread extends Thread {
  @Override 
  public void run() {
    for (int i = 0; i < 100; i++) {
      System.out.println("[Thread] Hello from " + Thread.currentThread().threadId());
    }
  }
}

// putem implementa interfata Runnable 

class MyThreadImplementsRunnable implements Runnable {
  @Override 
  public void run() {
    for (int i = 0; i < 100; i++) {
      System.out.println("[Runnable] Hello from " + Thread.currentThread().threadId());
    }
  }
}