package io.tobylarone.markov;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.tobylarone.markov.util.WordStat;

/**
 * Markov class
 */
public class Markov {
    private static final Logger LOGGER = LogManager.getLogger(Markov.class);
    private static final int DEFAULT_LENGTH = 140;
    private static final int LENGTH_LENIENCY = 10;
    private String[] input;
    private Hashtable<String, ArrayList<String>> index;
    private Random rand = new Random();
    private ArrayList<String> uniqueWords = new ArrayList<>();
    private List<WordStat> uniqueWordsStats;
    
    /**
     * Markov constructor
     * Takes a {@link String} input, splits into a {@link String} array
     * 
     * @param input String to create a markov chain
     */
    public Markov(String input) {
        this.input = input.split(" ");
        index = new Hashtable<>();
        uniqueWordsStats = new ArrayList<>();
        buildIndex();
    }

    /**
     * Builds the index of words. First checks that words are valid
     */
    private void buildIndex() {
        LOGGER.debug("Building index");
        for (int i = 0; i < input.length; i++) {
            if (i < input.length - 1) {
                String word = input[i];
                String nextWord = input[i + 1];
                word = removeUnwantedStrings(word);
                nextWord = removeUnwantedStrings(nextWord);
                if (!isValid(word)) {
                    LOGGER.trace("Invalid word skipped: " + word);
                    continue;
                }
                if (!isValid(nextWord)) {
                    LOGGER.trace("Invalid word skipped: " + nextWord);
                    continue;
                }

                updateUniqueWords(word);
    
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

                updateUniqueWords(word);

                index.put(word, new ArrayList<String>());
            }
        }
        LOGGER.debug("Index created");
    }

    /**
     * 
     */
    private void updateUniqueWords(String word) {
        if (!uniqueWords.contains(word)) {
            uniqueWords.add(word);
            uniqueWordsStats.add(new WordStat(word));
        } else {
            for (WordStat w : uniqueWordsStats) {
                if (w.getWord().equals(word)) {
                    w.increment();
                }
            }
        }
    }

    /**
     * Unused, could be used to reindex 
     * Note: index should be cleared before rebuild
     * 
     * @param input additional messages to add to index
     */
    public void addToIndex(String input) {
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
        LOGGER.trace("Removing unwanted strings from: " + input);
        input = input.replaceAll("\\(edited\\)", "");
        input = input.replaceAll("@", "");
        LOGGER.trace("Cleaned string: " + input);
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
        LOGGER.trace("Validating string: " + input);
        if (input.startsWith("http://")) {
            LOGGER.trace("Url found in input: " + input);
            return false;
        }
        if (input.startsWith("https://")) {
            LOGGER.trace("Url found in input: " + input);
            return false;
        }
        switch (input) {
            case "":
                LOGGER.trace("Found empty string");
                return false;
            case "#":
                LOGGER.trace("Found string containing single #");
                return false;
            case "-":
                LOGGER.trace("Found string containing single -");
                return false;
            case "@":
                LOGGER.trace("Found string containing single @");
                return false;
            case "(edited)":
                LOGGER.trace("Found string matching `(edited)`");
                return false;
        }
        LOGGER.trace("String validated");
        return true;
    }
        
    /**
     * Gets the number of unique words in the markov chain
     * @return integer value of number of unique words
     */
    public int getUniqueWordCount() {
        return uniqueWords.size();
    }

    public List<WordStat> getTopWords() {
        List<WordStat> temp = uniqueWordsStats;
        Collections.sort(temp);
        return temp;
    }

    /**
     * Generates a sentence from a markov chain up to 
     * the specified length. If chains are short and infrequently
     * used in the origin text then sentences can be lower than 
     * the specified length
     * TODO maybe some more exception handling/verification
     * 
     * @param int length of sentence to generate
     * @return generated sentence as {@link String}
     */
    public String generateSentence(int length) {
        long startTime = System.nanoTime();
        List<String> sentence = new ArrayList<>();
        int target = length;
        int randValue = 0;
        String next = "";
        LOGGER.trace("Generating sentence");
        try {
            LOGGER.trace("Random limit: " + uniqueWords.size());
            randValue = rand.nextInt(uniqueWords.size());
            LOGGER.trace("Random value: " + randValue);
            String startingPoint = uniqueWords.get(randValue);
            ArrayList<String> start = index.get(startingPoint);
    
            LOGGER.trace("Random limit: " + start.size());
            randValue = rand.nextInt(start.size());
            LOGGER.trace("Random value: " + randValue);
            next = start.get(randValue);
            sentence.add(next);
            target -= next.length();
    
            while (target > 0) {
                if (index.get(next) != null) {
                    ArrayList<String> wordChain = index.get(next);
                    if (wordChain.isEmpty()) {
                        LOGGER.trace("Random limit: " + uniqueWords.size());
                        randValue = rand.nextInt(uniqueWords.size());
                        LOGGER.trace("Random value: " + randValue);
                        next = uniqueWords.get(randValue);
                        break;
                    }
                    LOGGER.trace("Random limit: " + wordChain.size());
                    randValue = rand.nextInt(wordChain.size());
                    LOGGER.trace("Random value: " + randValue);
                    next = wordChain.get(randValue);
                    sentence.add(next);
                    target -= next.length() + 1;
                } else {
                    if (length > DEFAULT_LENGTH && target > LENGTH_LENIENCY) {
                        LOGGER.trace("Random limit: " + uniqueWords.size());
                        randValue = rand.nextInt(uniqueWords.size());
                        LOGGER.trace("Random value: " + randValue);
                        startingPoint = uniqueWords.get(randValue);
                        start = index.get(startingPoint);
                        LOGGER.trace("Random limit: " + start.size());
                        randValue = rand.nextInt(start.size());
                        LOGGER.trace("Random value: " + randValue);
                        next = start.get(randValue);
                        sentence.add(next);
                        target -= next.length();
                    } else {
                        break;
                    }
                }
            }
            LOGGER.debug("Target final: " + target);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            String s = "Error: Something went wrong during generation.";
            sentence = new ArrayList<>(Arrays.asList(s.split(" ")));
        }
        long endTime = System.nanoTime();
        LOGGER.info("Generated sentence (" + length + "chars): " + (endTime - startTime) / 1000 + "μs");
        return toString(sentence);
    }

    /**
     * Calls {@link #generateSentence(int)} with the default length
     * of 140 characters
     * 
     * @return generated sentence as {@link String}
     */
    public String generateSentence() {
        return generateSentence(DEFAULT_LENGTH);
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