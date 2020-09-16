package engine;

public enum Allegiance {
    WHITE, BLACK;

    @Override
    public String toString() {
        return this == WHITE ? "W" : "B";
    }
    public String fullString() {
        return this == WHITE ? "White" : "Black";
    }

    public static Allegiance not(Allegiance alg) {
        return alg == WHITE ? BLACK : WHITE;
    }
}
