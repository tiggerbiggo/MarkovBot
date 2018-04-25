package io.tobylarone.markov;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import io.tobylarone.markov.Markov;

/**
 * MarkovTest Class
 */
public class MarkovTest {

    /**
     * 
     */
    @Test
    public void removeUnwantedStringsEdited() {
        String testString = "this is a test string(edited) containing some @words that http://www.example.com are https://www.example.com really bad";
        Markov markov = new Markov(testString);
        assertTrue(markov.getUniqueWords().contains("string"));
    }

    @Test
    public void removeUnwantedStringsEditedWord() {
        String testString = "this is a (edited) string(edited) that cont(edited)ains words";
        Markov markov = new Markov(testString);
        assertFalse(markov.getUniqueWords().contains("(edited)"));
    }

    @Test
    public void removeUnwantedStringsUserTags() {
        String testString = "this is a test string(edited) conta@ining some @words @Toby Łarone";
        Markov markov = new Markov(testString);
        assertTrue(markov.getUniqueWords().contains("containing"));
    }

    @Test
    public void removeUnwantedStringsUserTagsMultiple() {
        String testString = "this is a test string(edited) @ cont@ining some @words @Toby Łarone";
        Markov markov = new Markov(testString);
        assertFalse(markov.getUniqueWords().contains("@"));
    }

    @Test
    public void isValidUrl() {
        String testString = "this has a url http://www.example.com";
        Markov markov = new Markov(testString);
        assertFalse(markov.getUniqueWords().contains("http://www.example.com"));
    }

    @Test
    public void isValidUrlSecure() {
        String testString = "this has a url https://www.example.com";
        Markov markov = new Markov(testString);
        assertFalse(markov.getUniqueWords().contains("https://www.example.com"));
    }

    @Test
    public void isValidUrlMultiple() {
        String testString = "this has http://www.example.com multiple urls http://www.example.com";
        Markov markov = new Markov(testString);
        assertFalse(markov.getUniqueWords().contains("http://www.example.com"));
    }

    @Test
    public void isValidUrlSecureMultiple() {
        String testString = "this has https://www.example.com multiple urls https://www.example.com";
        Markov markov = new Markov(testString);
        assertFalse(markov.getUniqueWords().contains("https://www.example.com"));
    }

}
