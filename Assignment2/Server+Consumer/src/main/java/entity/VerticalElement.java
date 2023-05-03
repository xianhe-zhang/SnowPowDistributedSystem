package entity;

public class VerticalElement {
  private String seasonID;
  private Integer totalVert;

  public VerticalElement(String seasonID, Integer totalVert){
    this.seasonID = seasonID;
    this.totalVert = totalVert;
  }

  public Integer getTotalVert() {
    return totalVert;
  }

  public String getSeasonID() {
    return seasonID;
  }
}
