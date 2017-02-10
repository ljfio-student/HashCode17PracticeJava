import java.util.List;
import java.util.Arrays;

public class Pizza {
  public List<Slice> slices;
  private int rows, columns;
  private char[][] pizza;
  private int min_topping;
  private int pizza_size;

  public Pizza(int rows, int columns, List<Slice> slices, char[][] pizza, int min_topping) {
    this.rows = rows;
    this.columns = columns;
    this.pizza_size = rows * columns;
    
    this.slices = slices;
    this.pizza = pizza;
    this.min_topping = min_topping;
  }

  public boolean isValid() {
    return intersections() == 0 &&
      invalidSlices() == 0 &&
      notCovered() == 0;
  }

  // Fitness
  private double fitnessIntersect() {
    int comparisons = (int)((slices.size() * (slices.size() - 1)) / 2);

    if (comparisons == 0) {
      return 0;
    }

    return 1.0 - (intersections() / comparisons);
  }

  // private double fitnessScore() {
  //   return 1.0 - (Math.abs((pizza_size) - score()) / (pizza_size));
  // }

  private double fitnessValidity() {
    return 1.0 - (invalidSlices() / slices.size());
  }

  private double fitnessCovered() {
    return 1.0 - (notCovered() / (pizza_size));
  }

  public double fitness() {
    if (slices.size() == 0) {
      return 0;
    }

    // return (fitnessIntersect() + fitnessValidity() + fitnessCovered() + fitnessScore()) / 4.0;
     return ((4.0 * fitnessIntersect()) + (2.0 * fitnessValidity()) + (1.0 * fitnessCovered())) / (4.0 + 2.0 + 1.0);
  }
  
  private long invalidSlices() {
    Boolean[] valid = new Boolean[slices.size()];

    for (int s = 0; s < slices.size(); s++) {
      Slice slice = slices.get(s);
      valid[s] = false;

      if (slice.toY >= rows || slice.toX >= columns) {
        continue;
      }

      int countM = 0, countT = 0;

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

    for (int i = 0; i < slices.size(); i++) {
      Slice iSlice = slices.get(i);

      for (int j = i + 1; j < slices.size(); j++) {
        Slice jSlice = slices.get(j);

        if (iSlice.intersect(jSlice)) {
          intersects++;
        }
      }
    }

    return intersects;
  }

  // private void updateCovered() {
  //   covered = new boolean[rows][columns];

  //   for (Slice slice : slices) {
  //     for (int y = slice.fromY; y <= slice.toY; y++) {
  //       for (int x = slice.fromX; x <= slice.toX; x++) {
  //         if (x < columns && y < rows) {
  //           covered[y][x] = true;
  //         }
  //       }
  //     }
  //   }
  // }

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
    // int number = 0;

    // for (int x = 0; x < columns; x++) {
    //   for (int y = 0; y < rows; y++) {
    //     if (!covered[y][x]) {
    //       number++;
    //     }
    //   }
    // }

    // return number;
  }

  public String outputString() {
    String result = String.format("%d\n", slices.size());

    for(Slice s : slices) {
      result += s.outputString();
    }

    return result;
  }

  public int score() {
    return slices.stream().map((s) -> s.score).reduce(0, Integer::sum);
  }
}