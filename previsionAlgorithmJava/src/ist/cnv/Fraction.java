package ist.cnv;

public class Fraction {
    private long num;
    private long den;

    public Fraction(long num, long den) {
        this.num = num;
        this.den = den;
    }

    public long getNum() { return num; }

    public long getDen() { return den; }

    public Fraction add(Fraction toAdd) {
        num = num * toAdd.getDen() + toAdd.getNum() * den;
        den = den * toAdd.getDen();
        return this;
    }
}