import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.SplittableRandom;

public class HashCodeSolver {
  private int rows, columns, min_topping, max_size;
  private char[][] pizza = new char[0][0];
  private SplittableRandom rng = new SplittableRandom();

  public static void main(String[] args) {
    new HashCodeSolver(args[0]);
  }

  public HashCodeSolver(String name) {
    System.out.print("i: ");

    loadFile(name);
    System.out.printf("l{%d %d %d %d} ", rows, columns, min_topping, max_size);

    // Calculate the factors
    int min_size = min_topping * 2; // Required to have minimum of both toppings on slice
    Set<Pair<Integer, Integer>> factors = Utility.factors(rows, columns, min_size, max_size);
    System.out.printf("f{%d} ", factors.size());

    // Storage
    Slice[] slices = slices(factors);
    Utility.updateRange(slices.length);
    System.out.printf("s{%d} ", slices.length);

    boolean finished = false, changed = true;

    // populate p
    Deque<Pizza> pizzas = populate(slices);
    System.out.print("p\n");

    Iterator<Pizza> strongest = pizzas.iterator();

    Pizza firstPizza = strongest.next();
    Pizza secondPizza = strongest.next();

    while (!finished) {
      // select parents
      changed = false;

      // breed
      Pair<Pizza, Pizza> children = breed(firstPizza, secondPizza);

      // mutate
      Pizza firstChild = mutate(children.x, slices);
      Pizza secondChild = mutate(children.y, slices);

      // update p
      if (Integer.compare(firstChild.fitness, secondPizza.fitness) < 0 && Integer.compare(firstPizza.fitness, secondPizza.fitness) < 0) {
        secondPizza = firstChild;
        changed = true;
      } else if (Integer.compare(firstChild.fitness, firstPizza.fitness) < 0) {
        firstPizza = firstChild;
        changed = true;
      }

      if (Integer.compare(secondChild.fitness, secondPizza.fitness) < 0 && Integer.compare(firstPizza.fitness, secondPizza.fitness) < 0) {
        secondPizza = secondChild;
        changed = true;
      } else if (Integer.compare(secondChild.fitness, firstPizza.fitness) < 0) {
        firstPizza = secondChild;
        changed = true;
      }

      finished = firstPizza.isValid || secondPizza.isValid;

      // information
      if (changed) {
        System.out.printf("\r%d-%d", firstPizza.fitness, secondPizza.fitness);
      }
    }

    // completed solution
    Pizza solution = firstPizza.isValid ? firstPizza : secondPizza; // pizzas.stream().filter((p) -> p.isValid).findFirst().get();

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

  private Deque<Pizza> populate(Slice[] random) {
    Deque<Pizza> pizzas = new ArrayDeque<>();

    for (int i = 0; i < 2; i++) {
      List<Slice> slices = new ArrayList<Slice>();

      // int index = (int)(Math.random() * random.length);
      int[] range = Utility.randomRange(2);

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
      int removing = rng.nextInt(original_size);

      for(int r = 0; r < removing; r++) {
        newSlices.remove(rng.nextInt(newSlices.size()));
      }

      // Add slices
      int mix = removing > 0 ? rng.nextInt(removing) : 0;
      boolean shake = rng.nextBoolean();
      int adding = removing + (removing > mix ? (shake ? 0 : -mix) : (shake ? 0 : mix));

      int[] randomRange = Utility.randomRange(adding);

      for (int index : randomRange) {
        newSlices.add(random[index]);
      }
    }

    return new Pizza(rows, columns, newSlices, pizza, min_topping);
  }

  private Pair<Pizza, Pizza> breed(Pizza firstPizza, Pizza secondPizza) {
    int firstSliceSize = firstPizza.slices.size();
    int secondSliceSize = secondPizza.slices.size();

    int sumSizes = firstSliceSize + secondSliceSize;

    int middle = sumSizes > 2 ? rng.nextInt(sumSizes / 2) : 1;

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
}
