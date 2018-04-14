package io.tobylarone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Markov class
 */
public class Markov {
    private String[] input;
    private Hashtable<String, ArrayList<String>> index;
    private Random rand = new Random();
    private ArrayList<String> uniqueWords = new ArrayList<>();
    
    /**
     * Markov constructor
     * Takes a {@link String} input, splits into a {@link String} array
     * 
     * @param input String to create a markov chain
     */
    public Markov(String input) {
        this.input = input.split(" ");
        index = new Hashtable<>();
        buildIndex();
    }

    /**
     * Builds the index of words. First checks that words are valid
     */
    private void buildIndex() {
        for (int i = 0; i < input.length; i++) {
            if (i < input.length - 1) {
                String word = input[i];
                String nextWord = input[i + 1];
                word = removeUnwantedStrings(word);
                nextWord = removeUnwantedStrings(nextWord);
                if (!isValid(word)) {
                    continue;
                }
                if (!isValid(nextWord)) {
                    continue;
                }
    
                if (!uniqueWords.contains(word)) {
                    uniqueWords.add(word);
                }
    
                ArrayList<String> chain = index.get(word);
                if (chain == null) {
                    chain = new ArrayList<String>();
                }
                chain.add(nextWord);
                index.put(word, chain);
            } else {
                // Here we are adding the very last word to the index, if it is valid
                String word = input[i];
                word = removeUnwantedStrings(word);
                if (!isValid(word)) {
                    continue;
                }
                if (!uniqueWords.contains(word)) {
                    uniqueWords.add(word);
                }
                index.put(word, new ArrayList<String>());
            }
        }
    }

    /**
     * Unused, could be used to reindex 
     * Note: index should be cleared before rebuild
     * 
     * @param input additional messages to add to index
     */
    public void rebuildIndex(String input) {
        this.input = Stream.concat(Arrays.stream(this.input), Arrays.stream(input.split(" "))).toArray(String[]::new);
        buildIndex();
    }

    /**
     * Sometimes strings can contain unwanted text. This can be
     * used to remove them
     * 
     * @param input String to check
     * @return phrase as {@link String}
     */
    private String removeUnwantedStrings(String input) {
        input = input.replaceAll("\\(edited\\)", "");
        input = input.replaceAll("@", "");
        return input;
    }

    /**
     * Checks if a word is valid, urls and user tags are removed
     * aswell as some single characters and the (edited) text
     * that can be found in edited messages
     * 
     * @param input String to validate
     * @return boolean
     */
    private boolean isValid(String input) {
        if (input.startsWith("http://")) {
            return false;
        }
        if (input.startsWith("https://")) {
            return false;
        }
        switch (input) {
            case "":
                return false;
            case "#":
                return false;
            case "-":
                return false;
            case "@":
                return false;
            case "(edited)":
                return false;
        }
        return true;
    }
        
    /**
     * Gets the number of unique words in the markov chain
     * @return integer value of number of unique words
     */
    public int getUniqueWordCount() {
        return uniqueWords.size();
    }

    /**
     * Generates a sentence from a markov chain up to 
     * the specified length. If chains are short and infrequently
     * used in the origin text then sentences can be lower than 
     * the specified length
     * 
     * @param int length of sentence to generate
     * @return generated sentence as {@link String}
     */
    public String generateSentence(int length) {
        List<String> sentence = new ArrayList<>();
        int target = length;
        String next = "";
        String startingPoint = uniqueWords.get(rand.nextInt(uniqueWords.size()));
        ArrayList<String> start = index.get(startingPoint);

        next = start.get(rand.nextInt(start.size()));
        sentence.add(next);
        target -= next.length();

        while (target > 0) {
            if (index.get(next) != null) {
                ArrayList<String> wordChain = index.get(next);
                if (wordChain.isEmpty()) {
                    next = uniqueWords.get(rand.nextInt(uniqueWords.size()));
                    break;
                }
                next = wordChain.get(rand.nextInt(wordChain.size()));
                sentence.add(next);
                target -= next.length() + 1;
            } else {
                break;
            }
        }
        return toString(sentence);
    }

    /**
     * Calls {@link #generateSentence(int)} with the default length
     * of 140 characters
     * 
     * @return generated sentence as {@link String}
     */
    public String generateSentence() {
        return generateSentence(140);
    }

    /**
     * Converts a {@link List} of strings to a single {@link String}
     * 
     * @param s List of strings to convert
     * @return input {@link List} as {@link String}
     */
    private String toString(List<String> s) {
        return String.join(" ", s);
    }

    /**
     * Returns the list of unique words
     * 
     * @return list of unique words
     */
    public List<String> getUniqueWords() {
        return this.uniqueWords;
    }

}
