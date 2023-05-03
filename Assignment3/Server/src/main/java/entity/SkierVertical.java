package entity;

import java.util.List;

public class SkierVertical {

    private List<VerticalElement> skiervertical;
    public SkierVertical(List<VerticalElement> skiervertical) {
      this.skiervertical = skiervertical;
    }

    public List<VerticalElement> getSkierResorts() {
      return skiervertical;
    }
}
