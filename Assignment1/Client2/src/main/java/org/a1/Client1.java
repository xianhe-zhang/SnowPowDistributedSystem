package org.a1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Client1 {

  protected static CountDownLatch latchToPhase2 = new CountDownLatch(1);
  protected static Counter counter = new Counter();


  public static void main(String[] args) throws InterruptedException, IOException {

    // Test
    long startTest = System.currentTimeMillis();
    CountDownLatch latchTest = new CountDownLatch(1);
    doPhase("test", 1, 50, latchTest);
    latchTest.await();
    long endTest = System.currentTimeMillis();


    //  Phase I
    long start = System.currentTimeMillis();
    int numP1Threads = 32;
    int numP1Requests = 1000;
    CountDownLatch curLatch1 = new CountDownLatch(numP1Threads);
    doPhase("phase1", numP1Threads, numP1Requests, curLatch1);

    //begin Phase II
    latchToPhase2.await();
    // Phase II
    int numP2Threads = 112;
    int numP2Requests = 1500;
    CountDownLatch curLatch2 = new CountDownLatch(numP2Threads);
    doPhase("phase2", numP2Threads, numP2Requests,curLatch2);

    //To decide if all threads are complete.
    curLatch1.await();
    curLatch2.await();
    //System.out.println("test suc2");
    long end = System.currentTimeMillis();


    // Calculation
    long wallTime = end - start;
    int success = counter.getSuccessfulPosts();
    int failed = counter.getFailedPosts();
    long throughPut = 1000*(success + failed) / wallTime;
    System.out.println("\nClient Part 1 Result:");
    System.out.println("-----------------------------------------------");
    System.out.println("Number of successful requests sent: " + success);
    System.out.println("Number of unsuccessful requests: " + failed);
    System.out.println("The total run time(wall time): " + wallTime + " milliseconds");
    System.out.println("The total throughput per Sec: " + throughPut);


    System.out.println("\nClient Part 2 Result:");
    System.out.println("-----------------------------------------------");
    new RecordProcessor("./output.csv").calculateOutput();


    System.out.println("\ntest duration: "+ (endTest-startTest));
    System.out.println("phase duration: "+ (end-start));
  }

  private static void doPhase(String phaseName, int numberOfThreads, int numOfReqs, CountDownLatch curLatch) throws InterruptedException {
    for(int i = 0; i < numberOfThreads; i++){
      SkierThread skierThread = new SkierThread(numOfReqs, curLatch);
      skierThread.start();
    }
    curLatch.await();
    System.out.println(phaseName + " has already completed " + numOfReqs*numberOfThreads + " requests");
  }
}
