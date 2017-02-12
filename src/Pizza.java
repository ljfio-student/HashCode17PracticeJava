import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Pizza {
  public List<Slice> slices;;
  private int slices_size;

  // Validity & Fitness
  public boolean isValid = false;
  public int fitness = 0;

  private int intersections = 0;
  private int invalidSlices = 0;
  private int notCovered = 0;

  public Pizza(List<Slice> slices) {
    this.slices = slices;
    this.slices_size = slices.size();

    // Validity & Fitness
    intersections = intersections();
    invalidSlices = invalidSlices();
    notCovered = notCovered();

    isValid = isValid();
    fitness = fitness();
  }

  private boolean isValid() {
    return intersections == 0 &&
      invalidSlices == 0 &&
      notCovered == 0;
  }

  // Fitness
  private int fitness() {
    return intersections + invalidSlices + notCovered;
  }

  private int invalidSlices() {
    int[] valid = new int[slices_size];
    int countM, countT;
    boolean done = false;

    for (int s = 0; s < slices_size; s++) {
      Slice slice = slices.get(s);
      valid[s] = slice.score;
      done = false;

      // Reset count
      countM = 0;
      countT = 0;

      for (int y = slice.fromY; y <= slice.toY && !done; y++) {
        for (int x = slice.fromX; x <= slice.toX && !done; x++) {
          if (HashCodeSolver.pizza[y][x] == 'M') {
            countM++;
          } else if(HashCodeSolver.pizza[y][x] == 'T')  {
            countT++;
          }

          if (countM >= HashCodeSolver.min_topping && countT >= HashCodeSolver.min_topping) {
            valid[s] = 0;
            done = true;
          }
        }
      }
    }

    return Arrays.stream(valid).reduce(0, Integer::sum);
  }

  private int intersections() {
    int intersects = 0;

    for (int i = 0; i < slices_size; i++) {
      Slice iSlice = slices.get(i);

      for (int j = i + 1; j < slices_size; j++) {
        Slice jSlice = slices.get(j);

        if (iSlice.intersect(jSlice)) {
          intersects += iSlice.score + jSlice.score;
        }
      }
    }

    return intersects;
  }

  private int notCovered() {
    int[] covered = new int[HashCodeSolver.pizza_size];

    for (Slice slice : slices) {
      for (int y = slice.fromY; y <= slice.toY; y++) {
        for (int x = slice.fromX; x <= slice.toX; x++) {
          covered[(y * HashCodeSolver.columns) + x] = -1;
        }
      }
    }

    return Arrays.stream(covered).reduce(HashCodeSolver.pizza_size, Integer::sum);
  }

  public String outputString() {
    String result = String.format("%d\n", slices_size);

    for(Slice s : slices) {
      result += s.outputString();
    }

    return result;
  }

  @Override
  public String toString() {
    String sliceString = slices.stream()
      .map((s) -> s.toString())
      .collect(Collectors.joining(", "));

    return String.format("Pizza { %d %s }", score(), sliceString);
  }

  public int score() {
    return slices.stream().map((s) -> s.score).reduce(0, Integer::sum);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Pizza) {
      Pizza p = (Pizza) o;

      return p.slices.size() == slices_size &&
        this.slices.stream()
          .map((s) -> p.slices.stream()
            .map((e) -> s.equals(e) ? 1 : 0).reduce(0, Integer::sum))
          .reduce(0, Integer::sum) == slices_size;
    }

    return false;
  }

  @Override
  public int hashCode() {
    return this.slices.stream().map((s) -> s.hashCode()).reduce(0, Integer::sum);
  }
}