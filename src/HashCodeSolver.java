import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Iterator;

public class HashCodeSolver {
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
    Slice[] slices = slices(factors);

    long count = 0;
    boolean finished = false;

    // populate p
    Deque<Pizza> pizzas = populate(slices);

    while (!finished) {
      // select parents
      Iterator<Pizza> strongest = pizzas.iterator();

      Pizza firstPizza = strongest.next();
      Pizza secondPizza = strongest.next();

      // breed
      Pair<Pizza, Pizza> children = breed(firstPizza, secondPizza);

      // mutate
      Pizza firstChild = mutate(children.x, slices);
      Pizza secondChild = mutate(children.y, slices);

      // update p
      boolean added = false;

      if (!pizzas.stream().anyMatch((p) -> p.hashCode() == firstChild.hashCode() && p.equals(firstChild))) {
        if (Long.compare(firstChild.fitness, secondPizza.fitness) < 0) {
          pizzas.addFirst(firstChild);
          added = true;
        } else {
          pizzas.addLast(firstChild);
        }
      }

      if (!pizzas.stream().anyMatch((p) -> p.hashCode() == secondChild.hashCode() && p.equals(secondChild))) {
        if ((Long.compare(secondChild.fitness, secondPizza.fitness) < 0 && !added) ||
          (Long.compare(secondChild.fitness, firstPizza.fitness) < 0 && added)) {
          pizzas.addFirst(secondChild);
        } else {
          pizzas.addLast(secondChild);
        }
      }

      finished = pizzas.stream().anyMatch((p) -> p.isValid);

      // information

      System.out.printf("\rg%dp%df%d&%d", ++count, pizzas.size(), firstPizza.fitness, secondPizza.fitness);
    }

    // completed solution
    Pizza solution = pizzas.stream().filter((p) -> p.isValid).findFirst().get();

    System.out.printf("\nScore: %d\n", solution.score);
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

  private Deque<Pizza> populate(Slice[] random) {
    Deque<Pizza> pizzas = new ArrayDeque<>();

    for (int i = 0; i < 2; i++) {
      List<Slice> slices = new ArrayList<Slice>();

      // int index = (int)(Math.random() * random.length);
      int[] range = randomRange(random.length, 2);

      for(int index : range) {
        slices.add(random[index]);
      }

      pizzas.add(new Pizza(rows, columns, slices, pizza, min_topping));
    }

    return pizzas;
  }

  private Pizza mutate(Pizza original, Slice[] random) {
    List<Slice> newSlices =  new ArrayList<Slice>(original.slices);

    int original_size = newSlices.size();

    if (original_size > 0) {
      // Remove slices
      int removing = (int)(Math.random() * original_size);

      for(int r = 0; r < removing; r++) {
        newSlices.remove((int)(newSlices.size() * Math.random()));
      }

      // Add slices
      int mix = (int)(Math.random() * removing) + 1;
      double rand = Math.random();
      int adding = removing + (removing > mix ? (rand < 0.5 ? 0 : -mix) : (rand < 0.5 ? 0 : mix));

      int[] randomRange = randomRange(random.length, adding);

      for (int index : randomRange) {
        newSlices.add(random[index]);
      }
    }

    return new Pizza(rows, columns, newSlices, pizza, min_topping);
  }

  private Pair<Pizza, Pizza> breed(Pizza firstPizza, Pizza secondPizza) {
    int firstSliceSize = firstPizza.slices.size();
    int secondSliceSize = secondPizza.slices.size();

    int middle = (firstSliceSize + secondSliceSize) / 2;

    ArrayList<Slice> newFirstSlice = new ArrayList<>();
    ArrayList<Slice> newSecondSlice = new ArrayList<>();

    for(int i = 0; i < firstSliceSize; i++) {
      if (i < middle) {
        newSecondSlice.add(firstPizza.slices.get(i));
      } else {
        newFirstSlice.add(firstPizza.slices.get(i));
      }
    }

    for(int i = 0; i < secondSliceSize; i++) {
      if (i >= middle) {
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