import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.List;

public class HashCodeSolver {
  private final int max_pizzas = 1000;

  private int rows, columns, min_topping, max_size, pizza_size;
  private char[][] pizza = new char[0][0];

  public static void main(String[] args) {
    new HashCodeSolver(args[0]);
  }

  public HashCodeSolver(String name) {
    loadFile(name);

    // Calculate the factors
    Set<Pair<Integer, Integer>> factors = factors(rows, columns, max_size);

    // Storage
    Set<Pizza> pizzas = new HashSet<>();
    Slice[] slices = slices(factors);

    long count = 0;
    boolean finished = false;

    // populate p
    populate(pizzas, slices, max_pizzas);

    while (!finished) {
      // Collections.shuffle(pizzas);
      List<Pizza> sorted = pizzas.stream()
        .sorted((a, b) -> Double.compare(b.fitness, a.fitness))
        .limit(2)
        .collect(Collectors.toList());

      // select parents
      Pizza firstPizza = sorted.get(0);
      Pizza secondPizza = sorted.get(1);

      // breed
      Pair<Pizza, Pizza> children = breed(firstPizza, secondPizza);

      // mutate
      Pizza firstChild = mutate(children.x, slices);
      Pizza secondChild = mutate(children.y, slices);

      // update p
      if (Double.compare(firstChild.fitness, firstPizza.fitness) >= 0) {
        pizzas.add(firstChild);
      }

      if (Double.compare(secondChild.fitness, firstPizza.fitness) >= 0) {
        pizzas.add(secondChild);
      }

      finished = pizzas.stream().anyMatch((p) -> p.isValid);

      // information
      System.out.printf("\r%d %d", ++count, pizzas.size());
    }

    // completed solution
    Pizza solution = pizzas.stream().filter((p) -> p.isValid).findFirst().get();

    System.out.printf("\nScore: %d\n", solution.score());
    System.out.print(solution.outputString());
  }

  private Slice[] slices(Set<Pair<Integer, Integer>> factors) {
    Set<Slice> slices = new HashSet<Slice>();

    for (Pair<Integer, Integer> factor : factors) {
      boolean[] opts = !factor.x.equals(factor.y) ?
        new boolean[] { true, false } : // Case it is rectangle
        new boolean[] { false }; // Case it is square

      for(int b = 0; b < opts.length; b++) {
        for (int y = 0; y < rows; y++) {
          for (int x = 0; x < columns; x++) {
            Slice s = new Slice(x, y, factor, opts[b]);

            if (s.insideBound(rows, columns)) {
              slices.add(s);
            }
          }
        }
      }
    }

    return slices.toArray(new Slice[0]);
  }

  private void populate(Set<Pizza> pizzas, Slice[] random, int max_pizzas) {
    for (int i = 0; i < 3; i++) {
      List<Slice> slices = new ArrayList<Slice>();

      int index = (int)(Math.random() * random.length);

      slices.add(random[index]);

      pizzas.add(new Pizza(rows, columns, slices, pizza, min_topping));
    }
  }

  private Pizza mutate(Pizza original, Slice[] random) {
    int amount = (int)(Math.random() * pizza_size * 0.5);

    // Add slices
    int[] randomRange = randomRange(random.length, amount);

    ArrayList<Slice> added_slices =  new ArrayList<Slice>(original.slices);

    for (int index : randomRange) {
      added_slices.add(random[index]);
    }

    Pizza newPizza = new Pizza(rows, columns, added_slices, pizza, min_topping);

    // Remove slices
    if (newPizza.slices.size() > 0) {
      ArrayList<Slice> removed_slices = new ArrayList<Slice>(newPizza.slices);

      int removable = (int)(Math.random() * removed_slices.size());

      for(int r = 0; r < removable; r++) {
        removed_slices.remove((int)(removed_slices.size() * Math.random()));
      }

      if (removed_slices.size() > 0) {
        newPizza = new Pizza(rows, columns, removed_slices, pizza, min_topping);
      }
    }

    return newPizza;
  }

  private Pair<Pizza, Pizza> breed(Pizza firstPizza, Pizza secondPizza) {
    int[] randomFirst = randomRange(firstPizza.slices.size(), (int)(firstPizza.slices.size() * Math.random()));
    int[] randomSecond = randomRange(secondPizza.slices.size(), (int)(secondPizza.slices.size() * Math.random()));

    ArrayList<Slice> newFirstSlice = new ArrayList<>();
    ArrayList<Slice> newSecondSlice = new ArrayList<>();

    for(int i = 0; i < firstPizza.slices.size(); i++) {
      if (Arrays.asList(randomFirst).contains(i)) {
        newSecondSlice.add(firstPizza.slices.get(i));
      } else {
        newFirstSlice.add(firstPizza.slices.get(i));
      }
    }

    for(int i = 0; i < secondPizza.slices.size(); i++) {
      if (Arrays.asList(randomSecond).contains(i)) {
        newFirstSlice.add(secondPizza.slices.get(i));
      } else {
        newSecondSlice.add(secondPizza.slices.get(i));
      }
    }

    return new Pair<Pizza, Pizza>(
      new Pizza(rows, columns, newFirstSlice, pizza, min_topping),
      new Pizza(rows, columns, newSecondSlice, pizza, min_topping));
  }

  private int[] randomRange(int size, int amount) {
    ArrayList<Integer> range = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      range.add(i);
    }

    Collections.shuffle(range);
    int[] result = new int[amount];

    for(int i = 0; i < amount; i++) {
      result[i] = range.get(i);
    }

    return result;
  }

  public void loadFile(String name) {
    BufferedReader br = null;

    try {
      br = new BufferedReader(new FileReader(name));

      int line_no = 0;

      while (br.ready()) {
        String line = br.readLine();

        // Get details
        if (rows == 0) {
          String[] info = line.split(" ");

          rows = Integer.parseInt(info[0]);
          columns = Integer.parseInt(info[1]);
          min_topping = Integer.parseInt(info[2]);
          max_size = Integer.parseInt(info[3]);

          pizza = new char[rows][columns];
          pizza_size = rows * columns;

          line_no = -1;
        } else {
            for (int x = 0; x < columns; x++) {
              pizza[line_no][x] = line.charAt(x);
            }
        }

        line_no++;

        if (line_no == rows) {
          break;
        }
      }
    } catch(Exception ex) {
      System.err.println(ex.getMessage());
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch(Exception ex) {
          System.err.println(ex.getMessage());
        }
      }
    }
  }

  public Set<Pair<Integer, Integer>> factors(int rows, int columns, int max_size) {
    Set<Pair<Integer, Integer>> set = new HashSet<Pair<Integer, Integer>>();

    for(int x = 1; x <= columns; x++) {
      for(int y = 1; y <= rows; y++) {
        if ((x * y) <= max_size) {
          Pair<Integer, Integer> factor = new Pair<Integer, Integer>(x, y);
          set.add(factor);
        }
      }
    }

    return set;
  }
}