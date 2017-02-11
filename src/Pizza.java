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
    updateValidityFitness();
  }

  private void updateValidityFitness() {
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
    return ((intersections * HashCodeSolver.max_size * 2) + (invalidSlices * HashCodeSolver.max_size) + notCovered);
  }

  private int invalidSlices() {
    Boolean[] valid = new Boolean[slices_size];
    int countM, countT;

    for (int s = 0; s < slices_size; s++) {
      Slice slice = slices.get(s);
      valid[s] = false;

      if (slice.toY >= HashCodeSolver.rows || slice.toX >= HashCodeSolver.columns) {
        continue;
      }

      // Reset count
      countM = 0;
      countT = 0;

      for (int y = slice.fromY; y <= slice.toY; y++) {
        for (int x = slice.fromX; x <= slice.toX; x++) {
          if (HashCodeSolver.pizza[y][x] == 'M') {
            countM++;
          } else if(HashCodeSolver.pizza[y][x] == 'T') {
            countT++;
          }

          if (countM >= HashCodeSolver.min_topping && countT >= HashCodeSolver.min_topping) {
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
    Boolean[] covered = new Boolean[HashCodeSolver.pizza_size];

    for (Slice slice : slices) {
      for (int y = slice.fromY; y <= slice.toY; y++) {
        for (int x = slice.fromX; x <= slice.toX; x++) {
          if (x < HashCodeSolver.columns && y < HashCodeSolver.rows) {
            covered[(y * HashCodeSolver.columns) + x] = true;
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

  @Override
  public String toString() {
    String sliceString = slices.stream()
      .map((s) -> s.toString())
      .collect(Collectors.joining(", "));

    return String.format("Pizza { %d %s }", score(), sliceString);
  }

  public int score() {
    return slices.stream().map((s) -> s.score()).reduce(0, Integer::sum);
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