package org.a1;

import java.util.Random;

public class SkierEvent {
  private Integer resortID;
  private String seasonID;
  private String dayID;
  private Integer skierID;

  public SkierEvent() {
    Random rand = new Random();
    this.resortID = rand.nextInt(10)+1;
    this.seasonID = "2022";
    this.dayID = "1";
    this.skierID = rand.nextInt(100000)+1;
  }

  public Integer getResortID() {
    return resortID;
  }

  public void setResortID(Integer resortID) {
    this.resortID = resortID;
  }

  public String getSeasonID() {
    return seasonID;
  }

  public void setSeasonID(String seasonID) {
    this.seasonID = seasonID;
  }

  public String getDayID() {
    return dayID;
  }

  public void setDayID(String dayID) {
    this.dayID = dayID;
  }

  public Integer getSkierID() {
    return skierID;
  }

  public void setSkierID(Integer skierID) {
    this.skierID = skierID;
  }

}
