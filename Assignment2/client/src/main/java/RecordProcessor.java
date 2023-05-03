import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is Client 2 Code.
 */
public class RecordProcessor {
  private FileWriter csvWriter;
//  private String filePath;
  protected static CopyOnWriteArrayList<Record> records = new CopyOnWriteArrayList<>();

  public RecordProcessor(String filePath) throws IOException {
//    this.filePath = filePath;
//    System.out.println(filePath);
    csvWriter = new FileWriter(filePath);
    csvWriter.append("startTime,requestType,latency,responseCode\n");


  }

  public void addRecordToCSV(Record r) throws IOException {
    csvWriter.append(r.toString());
  }

  public void calculateOutput() throws IOException {
    Collections.sort(records);
    double min = 1000000, max = 0, sum = 0;
    double median = records.get((int)(0.5*records.size())).getLatency();
    double p99 = records.get((int)(0.99*records.size())).getLatency();
    for(Record r : records){
      sum += r.getLatency();
      max = Math.max(max, r.getLatency());
      min = Math.min(min, r.getLatency());
      addRecordToCSV(r);
    }
    double mean = sum / records.size();
    double throughput = records.size() / sum * 1000;
    System.out.println(records.size());
    System.out.println(sum);
    System.out.println(
        "Mean response time: " + mean + "\n"
            +"Median response time: " + median + "\n"
            +"Throughput: " + throughput + "\n"
            +"99th response time: " + p99 + "\n"
            +"min and max response time: " + "min: " + min + " , max: " + max
    );
  }

}
