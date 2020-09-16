package ai;

public class TableEntry {
    public static final byte HASH_EXACT = 0;
    public static final byte HASH_BETA = 1; // score was atleast beta, produced a cut-off
    public static final byte HASH_ALPHA = 2; // score was atmost alpha, produced a cut-off
    private long zobrist;
    private int score;
    private byte depth;
    private byte entryType;
    // TODO OTHER THINGIES

    public TableEntry(long zobrist, int score, byte depth, byte entryType) {
        this.zobrist = zobrist;
        this.score = score;
        this.depth = depth;
        this.entryType = entryType;
    }

    public long getZobrist() {
        return zobrist;
    }

    public int getScore() {
        return score;
    }

    public byte getDepth() {
        return depth;
    }

    public byte getEntryType() {
        return entryType;
    }
}