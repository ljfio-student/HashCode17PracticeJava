import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

public class Utility {
  private static ArrayList<Integer> range = new ArrayList<>();

  public static void updateRange(int size) {
    range.clear();

    for (int i = 0; i < size; i++) {
      range.add(i);
    }
  }

  public static int[] randomRange(int amount) {
    Collections.shuffle(range);

    int[] result = new int[amount];

    for(int i = 0; i < amount; i++) {
      result[i] = range.get(i);
    }

    return result;
  }

  public static Set<Pair<Integer, Integer>> factors(int rows, int columns, int max_size) {
    Set<Pair<Integer, Integer>> set = new HashSet<>();
    Pair<Integer, Integer> factor;

    for(int x = 1; x <= columns; x++) {
      for(int y = 1; y <= rows; y++) {
        if ((x * y) <= max_size) {
          factor = new Pair<Integer, Integer>(x, y);
          set.add(factor);
        }
      }
    }

    return set;
  }
}