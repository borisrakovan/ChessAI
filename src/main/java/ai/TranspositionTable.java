package ai;

public class TranspositionTable {

    private static final int TABLE_SIZE = 16777216; // 2^24 entries
    private static final long TABLE_SIZE_MASK = 0xFFFFFFL;
    private TableEntry[] tt;
    private int misses;
    private int collisions;
    private int hits;

    public TranspositionTable() {
        tt = new TableEntry[TABLE_SIZE];
    }
    public void clear() {
        tt = new TableEntry[TABLE_SIZE];
        resetCounters();
    }
    public void save(TableEntry te) {
        int hash = (int)(te.getZobrist() & TABLE_SIZE_MASK); // todo: not sure
        tt[hash] = te;
    }

    public TableEntry probe(long zobrist, byte depth) {
        int hash = (int)(zobrist & TABLE_SIZE_MASK);
        TableEntry te = tt[hash];
        if (te != null) {
            if (te.getZobrist() == zobrist) {
                if (te.getDepth() >= depth && te.getEntryType() == TableEntry.HASH_EXACT) {
                    hits++;
                    return te;
                }
            } else {
                collisions++;
            }
//            System.err.println("collision");
        }
        misses++;

        return null;
    }

    public int getMisses() {
        return misses;
    }

    public int getCollisions() {
        return collisions;
    }

    public int getHits() {
        return hits;
    }
    public void resetCounters() {
        misses = 0;
        collisions = 0;
        hits = 0;
    }

}
