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
  private int rows = 0, columns = 0, min_topping = 0, max_size = 0, line_no = 0, max_pizzas = 1000;
  private char[][] pizza = new char[0][0];

  public static void main(String[] args) {
    new HashCodeSolver(args[0]);
  }

  public HashCodeSolver(String name) {
    loadFile(name);
    Set<Pair<Integer, Integer>> factors = factors(rows, columns, max_size);
    // factors.forEach((c) -> { System.out.println(c.toString()); });

    List<Pizza> pizzas = new ArrayList<>(max_pizzas);
    Set<Slice> slices = new HashSet<>();

    boolean finished = false;

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

    // slices.stream().forEach((s) -> { System.out.println(s.toString()); });
    // System.out.printf("%df - %ds\n\n", factors.size(), slices.size());

    // populate p
    int count = 0;
    Slice[] randomSlice = slices.toArray(new Slice[0]);

    populate(pizzas, randomSlice, max_pizzas);
    
    while (!finished) {
      // Collections.shuffle(pizzas);
      Collections.sort(pizzas, (a, b) -> Double.compare(b.fitness(), a.fitness()));

      // select parents
      Pizza firstPizza = pizzas.get(0);
      Pizza secondPizza = pizzas.get(1);

      // breed
      Pair<Pizza, Pizza> children = breed(firstPizza, secondPizza);
      // pizzas.add(children.x);
      // pizzas.add(children.y);

      // mutate first
      // pizzas.add(mutate(children.x, randomSlice));
      Pizza firstChild = mutate(children.x, randomSlice);
     
      // mutate second 
      // pizzas.add(mutate(children.y, randomSlice));
      Pizza secondChild = mutate(children.y, randomSlice);

      // update p
      if (Double.compare(firstChild.fitness(), firstPizza.fitness()) >= 0) {
        pizzas.add(firstChild);
      }

      if (Double.compare(secondChild.fitness(), firstPizza.fitness()) >= 0) {
        pizzas.add(secondChild);
      }

      if (pizzas.size() >= max_pizzas) {
        int top10 = (int)(max_pizzas * 0.1);

        List<Pizza> new_pizzas = pizzas.stream()
          // .filter((p) -> p.slices.size() < rows * columns)
          .sorted((a, b) -> Double.compare(b.fitness(), a.fitness()))
          .limit(top10)
          .collect(Collectors.toList());

        pizzas.clear();
        pizzas.addAll(new_pizzas);
      }

      finished = pizzas.stream().anyMatch((p) -> p.isValid());

      // information
      int max_score = pizzas.stream()
        .map((p) -> p.score())
        .reduce(Integer::max)
        .get();

      int max_size = pizzas.stream()
        .map((p) -> p.slices.size())
        .reduce(Integer::max)
        .get();

      double max_fitness = pizzas.stream()
        .map((p) -> p.fitness())
        .reduce(Double::max)
        .get();
        
      System.out.printf("\r%dc : %ds : %dms : %dmp : %.4fmf", ++count, pizzas.size(), max_size, max_score, max_fitness);
    }

    Pizza solution = pizzas.stream().filter((p) -> p.isValid()).findFirst().get();
    // pizzas.forEach((p) -> { System.out.println(p.isValid()); });
    System.out.printf("\nScore: %d\n", solution.score());
    System.out.print(solution.outputString());
  }

  private void populate(List<Pizza> pizzas, Slice[] random, int max_pizzas) {
    int randomPizzas = (int)(Math.random() * max_pizzas) + 2;

    for (int i = 0; i < randomPizzas; i++) {
      List<Slice> slices = new ArrayList<Slice>();
      int[] randomRange = randomRange(random.length, 4);

      for(int index : randomRange) {
        slices.add(random[index]);
      }

      pizzas.add(new Pizza(rows, columns, slices, pizza, min_topping));
    }
  }

  private Pizza mutate(Pizza original, Slice[] random) {
    int amount = (int)(Math.random() * rows * columns);

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