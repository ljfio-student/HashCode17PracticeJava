public class Pair<X, Y> {
  public X x;
  public Y y;

  public Pair(X x, Y y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Pair) {
      Pair<?, ?> p = (Pair<?, ?>) o;

      return (p.x.equals(this.x) && p.y.equals(this.y)) ||
        (p.x.equals(this.y) && p.y.equals(this.x));
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return this.x.hashCode() * this.y.hashCode();
  }

  @Override
  public String toString() {
    return String.format("Pair { X = %s, Y = %s }", this.x.toString(), this.y.toString());
  }
}