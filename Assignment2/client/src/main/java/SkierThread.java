import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class SkierThread extends Thread {
  private static int RETRY_TIMES = 5;

  private Integer numberOfRequests;
  private CountDownLatch curLatch;


  // TODO: 检查是否正确
  public SkierThread(Integer numberOfRequests, CountDownLatch curLatch) {
    this.numberOfRequests = numberOfRequests;
    this.curLatch = curLatch;
  }

  @Override
  public void run() {
    SkiersApi apiInstance = new SkiersApi();
    ApiClient client = apiInstance.getApiClient();
//    client.setBasePath("http://localhost:8080/a1server_war_exploded/");
    client.setBasePath("http://6650ServerLB1-917956156.us-west-2.elb.amazonaws.com:8080/a1server_war");
//    client.setBasePath("http://35.90.118.182:8080/a1server_war");
    Random rand = new Random();

    // Send Request
    for (int i = 0; i < numberOfRequests; i++) {
      LiftRide ride = new LiftRide().time(rand.nextInt(360) + 1).liftID(rand.nextInt(40) + 1);
      SkierEvent skierEvent = new SkierEvent();

      // body, resortID, seasonID, dayID, skierID
      for (int j = 0; j < RETRY_TIMES; j++) {
        try {
          long startTime = System.currentTimeMillis();
          ApiResponse<Void> res = apiInstance
              .writeNewLiftRideWithHttpInfo(ride, skierEvent.getResortID(), skierEvent.getSeasonID(), skierEvent.getDayID(), skierEvent.getSkierID());
          long endTime = System.currentTimeMillis();
          // This is Client 2 Code.
          RecordProcessor.records.add(new Record(startTime, "POST", endTime-startTime,res.getStatusCode()));
          Client1.counter.incrementSuccessfulPost(1);
          break;
        } catch (ApiException e) {
          Client1.counter.incrementFailedPost(1);
          System.err.println("Exception when calling SkierApi#writeNewLiftRide, tried " + j + " times");
          e.printStackTrace();
        }
      }
//       System.out.println("thread complete");
      // Any thread of Phase I finishing sending post will activate our Phase II
    }
    try {
      Client1.latchToPhase2.countDown();
      curLatch.countDown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
