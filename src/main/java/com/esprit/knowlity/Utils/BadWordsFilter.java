package com.esprit.knowlity.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class BadWordsFilter {
    private static final String CSV_FILE = "/bad_words.csv";
    private static Set<String> badWords = new HashSet<>();

    static {
        loadBadWords();
    }

    private static void loadBadWords() {
        try (InputStream is = BadWordsFilter.class.getResourceAsStream(CSV_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (String word : line.split(",")) {
                    badWords.add(word.trim().toLowerCase());
                }
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static String filterBadWords(String input) {
        if (input == null) return null;
        String[] words = input.split("\\b");
        StringBuilder filtered = new StringBuilder();
        for (String word : words) {
            String lowerWord = word.toLowerCase();
            boolean isBad = false;
            for (String bad : badWords) {
                if (levenshtein(lowerWord, bad) <= 1) {
                    isBad = true;
                    break;
                }
            }
            if (isBad) {
                filtered.append("****");
            } else {
                filtered.append(word);
            }
        }
        return filtered.toString();
    }

    // Levenshtein distance helper
    private static int levenshtein(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }
}
