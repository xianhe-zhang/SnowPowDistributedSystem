public class Counter {
  private int successfulPosts;
  private int failedPosts;

  public Counter() {
    this.successfulPosts = 0;
    this.failedPosts = 0;
  }

  public synchronized void incrementSuccessfulPost(int increment) {
    this.successfulPosts += increment;
  }

  public synchronized void incrementFailedPost(int increment) {
    this.failedPosts += increment;
  }

  public int getSuccessfulPosts() {
    return this.successfulPosts;
  }

  public int getFailedPosts() {
    return this.failedPosts;
  }
}
