package ist.cnv;

public class Fraction {
    private long num;
    private long den;

    public Fraction(long num, long den) {
        this.num = num;
        this.den = den;
        toIrreducible();
    }

    public long getNum() { return num; }

    public long getDen() { return den; }

    /*
     * Additions
     */

    public Fraction add(Fraction toAdd) {
        long tmpNum = num * toAdd.getDen() + toAdd.getNum() * den;
        long tmpDen = den * toAdd.getDen();
        Fraction res = new Fraction(tmpNum, tmpDen);
        res.toIrreducible();
        return res;
    }

    public Fraction add(long n) {
        return add(new Fraction(n, 1));
    }

    /*
     * Subtractions
     */

    public Fraction sub(Fraction toSub) {
        long tmpNum = num * toSub.getDen() - toSub.getNum() * den;
        long tmpDen = den * toSub.getDen();
        Fraction res = new Fraction(tmpNum, tmpDen);
        res.toIrreducible();
        return res;
    }

    public Fraction sub(long n) {
        Fraction toSub = new Fraction(n, 1);
        return sub(toSub);
    }

    public Fraction inverseSub(long n) {
        Fraction toSub = new Fraction(n, 1);
        return toSub.sub(this);
    }

    /*
     * Multiplication
     */

    public Fraction mul(Fraction toMul) {
        Fraction res = new Fraction(num * toMul.getNum(), den * toMul.getDen());
        res.toIrreducible();
        return res;
    }

    public Fraction mul(long n) {
        Fraction res = new Fraction(num * n, den);
        res.toIrreducible();
        return res;
    }

    /*
     * Other operations
     */

    public void toIrreducible() {
        long n = gcd(num, den);
        num /= n;
        den /= n;
    }

    // It will only remain the decimal part
    public Fraction removeInteger() {
        num = num % den;
        return this;
    }

    public double toDouble() {
        return (double) num / den;
    }

    @Override
    public String toString() {
        if(num == 0)
            return "0";
        if(den == 1)
            return String.valueOf(num);

        return String.valueOf(num) + "/" + den;
    }

    /*
     * Aux functions
     */

    private long gcd(long a, long b) {
        if (b == 0)
            return a;
        return gcd(b, a % b);
    }
}