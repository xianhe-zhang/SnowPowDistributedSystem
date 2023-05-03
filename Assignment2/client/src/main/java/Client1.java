import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Client1 {

  protected static CountDownLatch latchToPhase2 = new CountDownLatch(1);
  protected static Counter counter = new Counter();


  public static void main(String[] args) throws InterruptedException, IOException {

    boolean test = false;
    if(test){
      long startTest = System.currentTimeMillis();
      CountDownLatch latchTest = new CountDownLatch(1);
      doPhase("test", 1, 5, latchTest);
      latchTest.await();
      long endTest = System.currentTimeMillis();
      long wallTime = endTest - startTest;
      long throughPut = 1000*(counter.getSuccessfulPosts() + counter.getFailedPosts()) / wallTime;
      System.out.println(counter.getSuccessfulPosts());
      System.out.println("The total throughput per Sec: " + throughPut);
      return;
    }

    //  Phase I
    long start = System.currentTimeMillis();
    int numP1Threads = 32;
    int numP1Requests = 1000;
    CountDownLatch curLatch1 = new CountDownLatch(numP1Threads);
    doPhase("phase1", numP1Threads, numP1Requests, curLatch1);

    //begin Phase II
    latchToPhase2.await();
    // Phase II
//    int numP2Threads = 112;
    int numP2Threads = 56;
    int numP2Requests = 3000;
    CountDownLatch curLatch2 = new CountDownLatch(numP2Threads);
    doPhase("phase2", numP2Threads, numP2Requests,curLatch2);

    //To decide if all threads are complete.
    curLatch1.await();
    long end = System.currentTimeMillis();
    curLatch2.await();
    //System.out.println("test suc2");
    long end2 = System.currentTimeMillis();



    // Calculation
    long wallTime = end - start;
    long wallTime2 = end - start;
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
    long throughPut2 = 1000*(success + failed) / wallTime;
    System.out.println("The total throughput per Sec: " + throughPut2);


//    System.out.println("\ntest duration: "+ (endTest-startTest));
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
