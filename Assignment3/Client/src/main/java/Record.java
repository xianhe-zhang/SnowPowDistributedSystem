/**
 * This is Client 2 Code.
 */
public class Record implements Comparable<Record> {
  private long startTime;
  private String requestType;
  private long latency;
  private int responseCode;

  public Record(long startTime, String requestType, long latency, int responseCode) {
    this.startTime = startTime;
    this.requestType = requestType;
    this.latency = latency;
    this.responseCode = responseCode;
  }

  @Override
  public String toString() {
    return startTime + "," + requestType + "," + latency + "," + responseCode + "\n";
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public String getRequestType() {
    return requestType;
  }

  public void setRequestType(String requestType) {
    this.requestType = requestType;
  }

  public long getLatency() {
    return latency;
  }

  public void setLatency(long latency) {
    this.latency = latency;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }

  @Override
  public int compareTo(Record o) {
    return (int) (this.getLatency() - o.getLatency());
  }
}
