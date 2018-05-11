package io.tobylarone.markov.util;

/**
 * WordStat class
 */
public class WordStat implements Comparable<WordStat> {

    private String word;
    private int count;

	/**
	 * WordStat constructor
	 */
    public WordStat(String word) {
        this.word = word;
        this.count = 1;
    }

	/**
	 * Increment the counter for the word
	 */
    public void increment() {
        this.count++;
    }

    @Override
    public String toString() {
        return word + ":" + count;
    }

    @Override
	public int compareTo(WordStat o) {
		return Integer.compare(o.getCount(), count);
	}

	/**
	 * @return the word
	 */
	public String getWord() {
		return word;
	}

	/**
	 * @param word the word to set
	 */
	public void setWord(String word) {
		this.word = word;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}
}
