package org.fao.geonet.kernel.search.spatial;

/**
 * Just a container of 2 elements. Good for returning 2 values.
 * 
 * @author jesse
 */
public class Pair<R, L> {
    public static <R, L> Pair<R, L> read(R one, L two) {
        return new Pair<R, L>(one, two);
    }

    public static <R, L> Pair<R, L> write(R one, L two) {
        return new Writeable<R, L>(one, two);
    }

    private R one;
    private L two;

    protected Pair() {}
    private Pair(R one, L two) {
        super();
        this.one = one;
        this.two = two;
    }

    public R one() {
        return one;
    }

    public L two() {
        return two;
    }

    public static class Writeable<R, L> extends Pair<R, L> {
        public Writeable(R one, L two) {
            super(one, two);
        }

        public Writeable<R, L> one(R newVal) {
            super.one = newVal;
            return this;
        }

        public Writeable<R, L> two(L newVal) {
            super.two = newVal;
            return this;
        }
    }

    @Override
    public String toString()
    {
        return "["+one+","+two+"]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((one == null) ? 0 : one.hashCode());
        result = prime * result + ((two == null) ? 0 : two.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pair other = (Pair) obj;
        if (one == null) {
            if (other.one != null)
                return false;
        } else if (!one.equals(other.one))
            return false;
        if (two == null) {
            if (other.two != null)
                return false;
        } else if (!two.equals(other.two))
            return false;
        return true;
    }
    
    
}
