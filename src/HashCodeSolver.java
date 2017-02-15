import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;;

public class HashCodeSolver {
  public static int rows, columns, min_topping, max_size, pizza_size;
  public static char[][] pizza = new char[0][0];
  List<Pizza> valid = new ArrayList<>();

  private SplittableRandom rng = new SplittableRandom();

  public static void main(String[] args) {
    new HashCodeSolver(args[0], args[1]);
  }

  public HashCodeSolver(String in, String out) {
    System.out.print("i: ");
    loadFile(in);
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
    Pair<Pizza, Pizza> pizzas = populate(slices);

    Pizza firstPizza = pizzas.x;
    Pizza secondPizza = pizzas.y;

    System.out.printf("p{%d}\n", valid.size());

    while (!finished) {
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
        changed = false;
      }
    }

    // completed solution
    Pizza solution = firstPizza.isValid ? firstPizza : secondPizza;

    System.out.printf("\nScore: %d\n", solution.score());
    System.out.print(solution.outputString());

    storeFile(out, solution.outputString());
  }

  private Slice[] slices(Set<Pair<Integer, Integer>> factors) {
    Set<Slice> inner = new HashSet<Slice>();
    Set<Slice> outer = new HashSet<Slice>();

    boolean[][] options = {
      new boolean[] { true, false }, // Case it is rectangle
      new boolean[] { false } // Case it is square
    };

    for (Pair<Integer, Integer> factor : factors) {
      boolean[] opts = options[!factor.x.equals(factor.y) ? 0 : 1];

      for(int b = 0; b < opts.length; b++) {
        inner.clear();

        // Generate possible positions on pizza
        for (int y = 0; y < rows; y++) {
          for (int x = 0; x < columns; x++) {
            Slice slice = new Slice(x, y, factor, opts[b]);

            // Ensure the slice is inside the bounds and it is valid
            if (slice.insideBounds() && slice.isValid()) {
              inner.add(slice);
            }
          }
        }

        // Add it to the list of valid slices and create a pizza
        valid.add(new Pizza(new ArrayList<>(inner)));
        outer.addAll(inner);
      }
    }

    return outer.toArray(new Slice[outer.size()]);
  }

  private Pair<Pizza, Pizza> populate(Slice[] random) {
    Pizza x = valid.get(rng.nextInt(valid.size()));
    Pizza y = valid.get(rng.nextInt(valid.size()));

    return new Pair<Pizza, Pizza>(x, y);
  }

  private Pizza mutate(Pizza original, Slice[] random) {
    List<Slice> newSlices =  new ArrayList<Slice>(original.slices);

    int original_size = newSlices.size();
    int amount = 0;

    // Remove slices
    amount = rng.nextInt(original_size + 1);

    for(int r = 0; r < amount; r++) {
      newSlices.remove(rng.nextInt(newSlices.size()));
    }

    // Add slices
    int mix = rng.nextInt(amount + 1);
    int shake = rng.nextInt(3);
    int adding = amount + (shake == 2 ? (shake == 1 ? mix : -mix) : 0);

    int[] randomRange = Utility.randomRange(adding);

    for (int index : randomRange) {
      newSlices.add(random[index]);
    }

    return new Pizza(newSlices);
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
      new Pizza(newFirstSlice),
      new Pizza(newSecondSlice));
  }

  public void storeFile(String name, String output) {
    BufferedWriter bw = null;

    try {
      bw = new BufferedWriter(new FileWriter(name));
      bw.write(output);
      bw.flush();
    } catch(Exception ex) {
      System.err.println(ex.getMessage());
    } finally {
      if (bw != null) {
        try {
          bw.close();
        } catch(Exception ex) {
          System.err.println(ex.getMessage());
        }
      }
    }
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
}
