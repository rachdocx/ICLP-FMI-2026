// Producer-Consumer in Java 
// avem thread-uri cu rol de Producator 
// avem thread-uri cu rol de Consumator 
// aceste thread-uri comunica prin intermediul unui buffer thread-safe 
// putem pune valori / mesaje in buffer-ul vid 
// putem extrage valori / mesage dintr-un buffer nevid 
// restul actiunilor sunt blocante 

import java.util.*;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
      PCDrop drop = new PCDrop();
      (new Thread(new Producer(drop))).start();
      (new Thread(new Consumer(drop))).start();
      (new Thread(new Consumer(drop))).start();
    }
}

class Producer implements Runnable {
  private PCDrop drop; 

  public Producer(PCDrop drop) {
    this.drop = drop; 
  }

  @Override 
  public void run() {
    String messages[] = { "m1", "m2", "m3", "m4", "m5", "EXIT" };
    Random random = new Random();

    for (int i = 0; i < messages.length; i++) {
      System.out.println("[Producer] " + Thread.currentThread().threadId() + " produced " + messages[i]);
      drop.put(messages[i]);
      try {
        Thread.sleep(random.nextInt(1000));
      }
      catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }
}

class Consumer implements Runnable {
  private PCDrop drop; 

  public Consumer(PCDrop drop) {
    this.drop = drop;
  }

  @Override
  public void run() {
    Random random = new Random(); 
    for (String message = drop.take(); !message.equals("EXIT"); message = drop.take()) {
      System.out.println("[Consumer] " + Thread.currentThread().threadId() + " received " + message);
      try {
        Thread.sleep(random.nextInt(5000));
      }
      catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }
}

// vom crea o structura thread-safe 

class PCDrop {
  private String message; 
  private boolean isEmpty = true; 

  // implementam doua metode: take si put 
  // take: asteptam cat timp nu avem mesaj (cat timp isEmpty) 
  // luam mesajul atunci cand !isEmpty si setam inapoi pe isEmpty 
  // put: invers: asteptam cat timp avem mesaj
  // altfel, scriem mesajul si iar se pune pe !isEmpty 

  // avem mai multe variante de sincronizare 
  // pentru a implementa metode thread-safe, putem folosi synchronized 

  // de cate ori folosim apelul wait() - acesta trebuie sa fie pus intr-un bloc try-catch 
  // catch-ul este pe InterruptedException

  public synchronized String take() {
    while (isEmpty) {
      try {
        wait();
      } 
      catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
    isEmpty = true; 
    notifyAll();
    return message; 
  }

  public synchronized void put(String message) {
    while (!isEmpty) {
      try {
        wait();
      } 
      catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
    isEmpty = false; 
    this.message = message;
    notifyAll();
  }
}