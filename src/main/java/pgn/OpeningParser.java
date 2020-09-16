package pgn;

import engine.Move;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class OpeningParser {
    public static final int BOOK_ENTRY_SIZE = 10;
    public static final String OPENING_FILE_PATH = "openings/openings.txt";
    public static void main(String[] args) throws IOException {
        parse();
    }
    private static void parse() throws IOException {
        File dir = new File("games/");
        File[] files = dir.listFiles();
        HashSet<String> openings = new HashSet<>();
        if (files == null)
            throw new RuntimeException("invalid directory");
        int i = 0;
        for (File file : files) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.equals("") && !line.matches(PGNUtils.PGNTagPattern.pattern())) {
                    if (line.startsWith("1."))  {
                        i++;
                        StringBuilder builder = new StringBuilder();
                        builder.append(line);

                        while (!(line = br.readLine()).equals("")) {
                            builder.append(line);
                        }
                        String opening = parseOpening(new String(builder));
                        if (!opening.equals(""))
                            openings.add(opening);
                    }
                }
            }
        }
        List<String> openingList = new ArrayList<>(openings);
        Collections.sort(openingList);
        BufferedWriter bw = new BufferedWriter(new FileWriter(OPENING_FILE_PATH));
        bw.write(String.join("\n", openingList));
        bw.close();
        System.out.println("Parsed " + i + " openings");
        System.out.println("Parsed " + openings.size() + " openings");
    }

    private static String parseOpening(String game) {
//        System.out.println(game);
        List<Move> opening = new ArrayList<>();
        game = game.replaceAll("\\d+\\.","");
        String[] moves = game.split(" ");
        if (moves.length - 1 < BOOK_ENTRY_SIZE)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < BOOK_ENTRY_SIZE; i++) {
            sb.append(moves[i]).append(" ");
        }
        return new String(sb);
    }
}
