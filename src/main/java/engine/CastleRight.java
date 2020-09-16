package engine;

public enum CastleRight {

    ALL("KQ"), LEFT("Q"), RIGHT("K"), NONE("");

    public final String notation;

    CastleRight(String notation) {
        this.notation = notation;
    }

    public static CastleRight valueOfLabel(String label) {
        for (CastleRight e : values()) {
            if (e.notation.equals(label)) {
                return e;
            }
        }
        return null;
    }
}
