package Modele.UtilIA;

import java.util.Objects;

public class Position2D {
    public int l;
    public int c;

    public Position2D(int l, int c) {
        this.l = l;
        this.c = c;
    }

    public int getL() {return l;}

    public int getC() {return c;}
    
    public void move(int l, int c) {
        this.l = l;
        this.c = c;
    }

    public double distance(Position2D other) {
        double dl = this.l - other.l;
        double dc = this.c - other.c;
        return Math.sqrt(dl * dl + dc * dc);
    }

    @Override
    public Position2D clone() {
        return new Position2D(l, c);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) { 
            return true;
        }

        if (!(other instanceof Position2D)) {
            return false;
        }

        Position2D o = (Position2D) other;

        return (o.l == this.l) && (o.c == this.c);
    }

    @Override
    public int hashCode() {
        return Objects.hash(l, c);
    }

    @Override
    public String toString() {
        return "(" + l + "," + c + ")";
    }
}
