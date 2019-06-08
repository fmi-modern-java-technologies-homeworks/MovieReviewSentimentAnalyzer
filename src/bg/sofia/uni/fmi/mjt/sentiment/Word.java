package bg.sofia.uni.fmi.mjt.sentiment;

public class Word implements Comparable<Word> {

	private int countOfRepeat;
	private int sumOf;

	public Word() {
		countOfRepeat = 0;
		sumOf = 0;
	}

	public void increasing(int number) {
		++countOfRepeat;
		sumOf += number;
	}

	public double getWordWight() {
		return (double) sumOf / (double) countOfRepeat;
	}

	public int getFrequency() {
		return countOfRepeat;
	}

	@Override
	public int compareTo(Word wordForComparing) {
		return getFrequency() - wordForComparing.getFrequency();
	}

}
