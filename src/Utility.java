import java.util.Set;
import java.util.HashSet;
import java.util.SplittableRandom;

public class Utility {
  private static int range_size;
  private static SplittableRandom random = new SplittableRandom();

  public static void updateRange(int size) {
    range_size = size;
  }

  public static int[] randomRange(int amount) {
    return random
      .ints(0, range_size)
      .distinct()
      .limit(amount)
      .toArray();
  }

  public static Set<Pair<Integer, Integer>> factors(int rows, int columns, int min_size, int max_size) {
    Set<Pair<Integer, Integer>> set = new HashSet<>();
    Pair<Integer, Integer> factor;

    for(int x = 1; x <= columns; x++) {
      for(int y = 1; y <= rows; y++) {
        int size = (x * y);

        if (size >= min_size && size <= max_size) {
          factor = new Pair<Integer, Integer>(x, y);
          set.add(factor);
        }
      }
    }

    return set;
  }
}