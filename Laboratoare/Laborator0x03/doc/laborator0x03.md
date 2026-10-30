# Laborator 0x03

## pb1.java

### Reader-Writer

implementam scenariul Reader-Writer
avem un scenariu concurent cu doua roluri:
- **Reader**
- **Writer**

vrem sa facem arbitrajul intr-o zona critica de memorie astfel incat:
- putem avea oricati Readeri simultan; (operatia de citire nu este blocanta)
- putem avea un singur Writer la un moment de timp;

pentru Readeri, am grija ca operatiile pe care le fac sa fie thread-safe (incrementarea numarului)

arbitram prin intermediul unui semafor care schimba prioritatea de la Readeri la Writeri
fiecare rol face un acquire

**Readeri:** permitem primului sa intre, iar apoi permitem oricator alti Readeri sa intre
iar cand iese ultimul Reader, facem un release pe semafor, ca sa poata intra Writerul

de cate ori permitem unui reader sa intre, il contorizam
daca este primul care intra, atunci inseamna ca el este cel care trebuie sa ceara permisiunea asupra zonei
permitem altor readeri sa intre
iar pe masura ce readerii ies, decrementam, iar cand ajungem la ultimul, facem un release pe semaforul pt writer

avem readeri care vor sa intre sa citeasca in zona critica

**Writer:**
- `wrt.acquire()` — incearca sa acceseze zona critica de memorie
- face o actiune in zona critica
- `wrt.release()` — iese din zona critica si permite altor thread-uri sa intre

```java
import java.util.concurrent.Semaphore;


public class Main {
    public static void main(String[] args) {
      Semaphore wrt = new Semaphore(1);
      Semaphore mutex = new Semaphore(1);

      (new Thread(new Writer(wrt))).start();
      (new Thread(new Reader(wrt, mutex))).start();
      (new Thread(new Writer(wrt))).start();
      (new Thread(new Writer(wrt))).start();
      (new Thread(new Reader(wrt, mutex))).start();
      (new Thread(new Reader(wrt, mutex))).start();
      (new Thread(new Reader(wrt, mutex))).start();
      (new Thread(new Reader(wrt, mutex))).start();
      (new Thread(new Writer(wrt))).start();
    }
}

// implementam scenariul Reader-Writer 
// avem un scenariu concurent cu doua roluri 
//  Reader 
//  Writer 

// vrem sa facem arbitrajul intr-o zona critica de memorie 
// astfel incat:
// - putem avea oricati Readeri simultan; (operatia de citire nu este blocanta)
// - putem avea un singur Writer la un moment de timp;
// pentru Readeri, am grija ca operatiile pe care le fac sa fie thread-safe (incrementarea numarului)

// arbitram prin intermediul unui semafor care schimba prioritatea de la Readeri la Writeri 
// fiecare rol face un acquire 
// Readeri: permitem primului sa intre, iar apoi permitem oricator alti Readeri sa intre 
// iar cand iese ultimul Reader, facem un release pe semafor, ca sa poata intra Writerul 

// de cate ori permitem unui reader sa intre, il contorizam 
// daca este primul care intra, atunci inseamna ca el este cel care trebuie sa ceara permisiunea asupra zonei 
// permitem altor readeri sa intre 
// iar pe masura ce readerii ies, decrementam, iar cand ajungem la ultimul, facem un release pe semaforul pt writer 
class Reader implements Runnable {
  private Semaphore wrt; 
  private Semaphore mutex; 

  public FileReader(Semaphore wrt, Semaphore mutex) {
    this.wrt = wrt;
    this.mutex = mutex; 
  }

  @Override 
  public void run() {
    while (true) {
      try {
        // avem readeri care vor sa intre sa citeasca in zona critica 

        mutex.acquire();
        ReaderCounter.inc();

        if (ReaderCounter.get() == 1) {
          wrt.acquire();
        }
        mutex.release();

        System.out.println(Thread.currentThread().threadId() + " reads");
        Thread.sleep(1000);

        mutex.acquire();
        ReaderCounter.dec();

        if (ReaderCounter.get() == 0) {
          wrt.release();
        }
        mutex.release();
      }
      catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }
}

class Writer implements Runnable {
  private Semaphore wrt; 

  public Writer(Semaphore wrt) {
    this.wrt = wrt; 
  }

  @Override 
  public void run() {
    while (true) {
      try {
        wrt.acquire();    // incearca sa acceseze zona critica de memorie 

        Thread.sleep(800);
        // face o actiune in zona critica 
        System.out.println(Thread.currentThread().threadId() + " writes");

        wrt.release();    // iese din zona critica si permite altor thread-uri sa intre 
        Thread.sleep(2000);
      }
      catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }
}

class ReaderCounter {
  private static int readcnt = 0;

  public static int get() {
    return readcnt;
  }

  public static void inc() {
    readcnt++;
  }

  public static void dec() {
    readcnt--;
  }
}
```
