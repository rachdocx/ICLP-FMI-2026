/*
    1.⁠ ⁠Implementati un counter in Java, folosind sincronizare atat cu synchronized, 
    cat si cu reentrantLock-uri (ca in problema de mai sus)
*/

import java.util.concurrent.locks.ReentrantLock;

class MyCounterSync {
  private int COUNTER = 0;

  public synchronized void inc(){ // <- fara synchronized counter nu e 300000 la final
    this.COUNTER++;
  }

  public int getCounter(){
    return this.COUNTER;
  }
}

class MyCounterLock{
  private ReentrantLock lock = new ReentrantLock();
  private int COUNTER = 0;

  public void inc(){
    lock.lock(); // <- fara lock/unlock counter nu e 300000 la final
    try{
      this.COUNTER++;
    }
    catch(Exception e){
      e.printStackTrace();
    }
    finally{
      lock.unlock();
    }
  }
  public int getCounter(){
    return this.COUNTER;
  }
}

class SyncConsumer implements Runnable{
  private MyCounterSync myCounterSync;

  public SyncConsumer(MyCounterSync myCounterSync){
    this.myCounterSync = myCounterSync;
  }
  @Override
  public void run(){
    for(int i = 0; i < 100000; ++i){
      myCounterSync.inc();
      // System.out.println("The thread " + Thread.currentThread().threadId() + " incremented the counter to " + COUNTER.getCounter());
    }
  }
}

class LockConsumer implements Runnable{
  private MyCounterLock myCounterLock;

  public LockConsumer(MyCounterLock myCounterLock){
    this.myCounterLock = myCounterLock;
  }

  @Override
  public void run(){
    for(int i = 0; i < 100000; ++i){
      myCounterLock.inc();
    }
  }

}
class Main{
  public static void main(String[] args){
    MyCounterSync globalCounterSync = new MyCounterSync();
    MyCounterLock globalCounterLock = new MyCounterLock();

    Thread myThread1 = new Thread(new SyncConsumer(globalCounterSync));
    Thread myThread2 = new Thread(new SyncConsumer(globalCounterSync));
    Thread myThread3 = new Thread(new SyncConsumer(globalCounterSync));

    myThread1.start();
    myThread2.start();
    myThread3.start();

    try{
      Thread.sleep(3000);
    }
    catch(InterruptedException e){
      e.printStackTrace();
    }
    System.out.println("The counter should be 300000. The counter is:" + globalCounterSync.getCounter());

    Thread myThread4 = new Thread(new LockConsumer(globalCounterLock));
    Thread myThread5 = new Thread(new LockConsumer(globalCounterLock));
    Thread myThread6 = new Thread(new LockConsumer(globalCounterLock));

    myThread4.start();
    myThread5.start();
    myThread6.start();

    try{
      Thread.sleep(3000);
    }
    catch(InterruptedException e){
      e.printStackTrace();
    }

    System.out.println("The counter should be 300000. The counter is:" + globalCounterLock.getCounter());

  }
}