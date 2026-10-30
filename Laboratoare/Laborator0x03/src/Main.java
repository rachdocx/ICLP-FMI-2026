package src;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Semaphore;

/* 
  Problema: avem un path care contine o ierarhie de foldere. 
  Scrieti un program care parcurge recursiv ierarhia de foldere, 
  identifica fisierele .txt, citeste continutul acestora prin thread-urile Reader, 
  respectiv modifica mereu prin Writer continutul acestora, 
  adaugand un nou log de forma "Writer-ul <writerId> a scris" sau alt mesaj mai inspirat. \
*/

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

class FileReader implements Runnable{
  private Semaphore wrt; 
  private Semaphore mutex;

  public FileReader(Semaphore wrt, Semaphore mutex) {
    this.wrt = wrt;
    this.mutex = mutex; 
  }

  @Override
  public void run(){
    try{
      readFolder(new File("."));     
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  private void readFolder(File dir) throws IOException, InterruptedException {
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    readFolder(file);
                } else if (file.getName().endsWith(".txt")) {
                    
                    try{
                        mutex.acquire();
                        ReaderCounter.inc();

                        if (ReaderCounter.get() == 1) {
                            wrt.acquire();
                        }

                        String content = Files.readString(file.toPath());
                        
                        mutex.release();
                        System.out.println("we found: " + content);
                        // Thread.sleep(1000);

                        mutex.acquire();
                        ReaderCounter.dec();

                        if (ReaderCounter.get() == 0) {
                          wrt.release();
                        }

                        mutex.release();

                    }catch(Exception e){
                      e.printStackTrace();
                    }
                    
                }
            }
        }
    }
}

class FileWriter implements Runnable{
  private Semaphore wrt; 

  public FileWriter(Semaphore wrt) {
    this.wrt = wrt; 
  }

  @Override 
  public void run() {
    try{
      writeFile(new File("."));
    }catch(Exception e){
      e.printStackTrace();
    }
  }

private void writeFile(File dir) {
    File[] files = dir.listFiles();

    if (files != null) {
      for (File file : files) {

        if (file.isDirectory()) {
          writeFile(file);
        } 
        else if (file.getName().endsWith(".txt")) {
          try {

            wrt.acquire();
            
            try {
              String content = Files.readString(file.toPath());
              
              content = content.toUpperCase();
              
              Files.writeString(file.toPath(), content);
              // Thread.sleep(1000); 

            } finally {
              wrt.release();
            }
            
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
public class Main {
  public static void main(String[] args){
    Semaphore wrt = new Semaphore(1);
    Semaphore mutex = new Semaphore(1);

    Thread reader = new Thread(new FileReader(wrt, mutex));
    Thread writer = new Thread(new FileWriter(wrt));

    reader.start();
    writer.start();
  }
}