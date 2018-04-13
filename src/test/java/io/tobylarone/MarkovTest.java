package io.tobylarone;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * MarkovTest Class
 */
public class MarkovTest {

    /**
     * 
     */
    @Test
    public void removeUnwantedStrings() {
        String testString = "this is a test string(edited) containing some @words that http://www.example.com are https://www.reallybad.com";
        Markov markov = new Markov(testString);
        markov.getUniqueWords();
        assertTrue(markov.getUniqueWords().contains("string"));
    }
}
