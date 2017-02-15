public class Slice {
  public int fromX, toX, fromY, toY, score;

  public Slice(int x, int y, Pair<Integer, Integer> factor, boolean rotate) {
    fromX = x;
    fromY = y;

    toX = x + (rotate ? factor.y : factor.x) - 1;
    toY = y + (rotate ? factor.x : factor.y) - 1;

    score = factor.x * factor.y;
  }

  public boolean intersect(Slice s) {
    return s.fromX <= this.toX &&
      s.toX >= this.fromX &&
      s.fromY <= this.toY &&
      s.toY >= this.fromY;
  }

  public boolean isValid() {
    boolean valid = false;

    // Reset count
    int countM = 0;
    int countT = 0;

    for (int y = fromY; y <= toY && !valid; y++) {
      for (int x = fromX; x <= toX && !valid; x++) {
        if (HashCodeSolver.pizza[y][x] == 'M') {
          countM++;
        } else if(HashCodeSolver.pizza[y][x] == 'T')  {
          countT++;
        }

        if (countM >= HashCodeSolver.min_topping && countT >= HashCodeSolver.min_topping) {
          valid = true;
        }
      }
    }

    return valid;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Slice) {
      Slice s = (Slice) o;

      return
        fromX == s.fromX &&
        fromY == s.fromY &&
        toX == s.toX &&
        toY == s.toY;
    }

    return false;
  }

  @Override
  public int hashCode() {
    return fromX + fromY + toX + toY;
  }

  @Override
  public String toString() {
    return String.format("Slice { (%d, %d) to (%d, %d) score %d }", fromX, fromY, toX, toY, score);
  }

  public String outputString() {
    // return toString();
    return String.format("%d %d %d %d\n", fromY, fromX, toY, toX);
  }

  public boolean insideBounds() {
    return toX < HashCodeSolver.columns && toY < HashCodeSolver.rows;
  }
}