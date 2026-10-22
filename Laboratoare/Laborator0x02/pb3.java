/*
    Scrieti un model de producer-consumer prin care producatorii 
    parcurg o ierarhie de foldere, citesc continutul fisierelor .txt si 
    adauga continutul acestora intr-un buffer. Consumatorii citesc din 
    buffer si afiseaza la STDOUT continutul cu o modificare asupra lui, 
    la alegere (de exemplu, toUpper pe text)
*/

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


class FileProducer implements Runnable{
  private BlockingQueue<String> fileBuffer;

  public FileProducer(BlockingQueue<String> fileBuffer){
    this.fileBuffer = fileBuffer;
  }

  @Override
  public void run(){
    try{
      readFolder(new File("."));
      fileBuffer.put("NoMoreFiles");
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
                    String content = Files.readString(file.toPath());
                    fileBuffer.put(content);
                }
            }
        }
    }
}

class FileConsumer implements Runnable{
  private BlockingQueue<String> fileBuffer;

  public FileConsumer(BlockingQueue<String> fileBuffer){
    this.fileBuffer = fileBuffer;
  }

  @Override 
  public void run(){
    try{
      while(true){
        String content = fileBuffer.take();

        if(content.equals("NoMoreFiles")){
          break;
        }

        System.out.println(content.toUpperCase());
      }
    }catch(InterruptedException e){
      e.printStackTrace();
    }
  }
}
class Main{
  public static void main(String[] args){
    BlockingQueue globalFileBuffer = new LinkedBlockingQueue<>();

    Thread producer = new Thread(new FileProducer(globalFileBuffer));
    Thread consumer1 = new Thread(new FileConsumer(globalFileBuffer));
    Thread consumer2 = new Thread(new FileConsumer(globalFileBuffer));
    Thread consumer3 = new Thread(new FileConsumer(globalFileBuffer));

    producer.start();
    consumer1.start();
    consumer2.start();
    consumer3.start();
  }
}







