package com.practise.algorithms;

/**
 * Given a string containing letters, spaces, and sentence-ending punctuation
 * (. ! ?), split it into sentences and return the highest word count found
 * in any single sentence.
 */
public class MaxWordsInSentence {

    public static int maxWords(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        String[] sentences = text.split("[.!?]"); // split wherever a sentence-ending mark appears
        int max = 0;

        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (trimmed.isEmpty()) {
                continue; // skip empty fragments (e.g. trailing punctuation, "..", etc.)
            }
            int wordCount = trimmed.split("\\s+").length; // split on any run of whitespace
            max = Math.max(max, wordCount);
        }

        return max;
    }

    public static void main(String[] args) {
        String text = "Hello there! How are you doing today? I am doing great, thanks for asking. See you soon.";
        System.out.println("Max words in a sentence: " + maxWords(text)); // -> 6 ("How are you doing today")
    }
}
