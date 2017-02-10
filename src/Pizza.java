import java.util.List;
import java.util.Arrays;

public class Pizza {
  public List<Slice> slices;
  private int rows, columns;
  
  private char[][] pizza;
  private int min_topping;

  private int pizza_size;
  private int slices_size;
  private int comparisons;

  public boolean isValid = false;
  public double fitness = 0.0;

  public Pizza(int rows, int columns, List<Slice> slices, char[][] pizza, int min_topping) {
    this.rows = rows;
    this.columns = columns;
    this.pizza_size = rows * columns;
    
    this.slices = slices;
    this.slices_size = slices.size();
    this.comparisons = (int)((slices_size * (slices_size - 1)) / 2);
    
    this.pizza = pizza;
    this.min_topping = min_topping;

    this.fitness = fitness();
    this.isValid = isValid();
  }

  private boolean isValid() {
    return intersections() == 0 &&
      invalidSlices() == 0 &&
      notCovered() == 0;
  }

  // Fitness
  private double fitnessIntersect() {
    if (this.comparisons == 0) {
      return 0;
    }

    return 1.0 - (intersections() / this.comparisons);
  }

  // private double fitnessScore() {
  //   return 1.0 - (Math.abs((pizza_size) - score()) / (pizza_size));
  // }

  private double fitnessValidity() {
    return 1.0 - (invalidSlices() / slices_size);
  }

  private double fitnessCovered() {
    return 1.0 - (notCovered() / (pizza_size));
  }

  private double fitness() {
    if (slices_size == 0) {
      return 0;
    }

    // return (fitnessIntersect() + fitnessValidity() + fitnessCovered() + fitnessScore()) / 4.0;
     return ((4.0 * fitnessIntersect()) + (2.0 * fitnessValidity()) + (1.0 * fitnessCovered())) / (4.0 + 2.0 + 1.0);
  }
  
  private long invalidSlices() {
    Boolean[] valid = new Boolean[slices_size];
    int countM, countT;

    for (int s = 0; s < slices_size; s++) {
      Slice slice = slices.get(s);
      valid[s] = false;

      if (slice.toY >= rows || slice.toX >= columns) {
        continue;
      }

      // Reset count
      countM = 0;
      countT = 0;

      for (int y = slice.fromY; y <= slice.toY; y++) {
        for (int x = slice.fromX; x <= slice.toX; x++) {
          if (pizza[y][x] == 'M') {
            countM++;
          } else if(pizza[y][x] == 'T') {
            countT++;
          }

          if (countM >= min_topping && countT >= min_topping) {
            valid[s] = true;
            break;
          }
        }
      }
    }

    return Arrays.stream(valid).map((b) -> b ? 0 : 1).reduce(0, Integer::sum);
  }

  private int intersections() {
    int intersects = 0;

    for (int i = 0; i < slices_size; i++) {
      Slice iSlice = slices.get(i);

      for (int j = i + 1; j < slices_size; j++) {
        Slice jSlice = slices.get(j);

        if (iSlice.intersect(jSlice)) {
          intersects++;
        }
      }
    }

    return intersects;
  }

  private int notCovered() {
    Boolean[] covered = new Boolean[pizza_size];

    for (Slice slice : slices) {
      for (int y = slice.fromY; y <= slice.toY; y++) {
        for (int x = slice.fromX; x <= slice.toX; x++) {
          if (x < columns && y < rows) {
            covered[(y * columns) + x] = true;
          }
        }
      }
    }

    return Arrays.stream(covered).map((b) -> (b != null && b) ? 0 : 1).reduce(0, Integer::sum);
  }

  public String outputString() {
    String result = String.format("%d\n", slices_size);

    for(Slice s : slices) {
      result += s.outputString();
    }

    return result;
  }

  public int score() {
    return slices.stream().map((s) -> s.score).reduce(0, Integer::sum);
  }
}