package bg.sofia.uni.fmi.mjt.sentiment;
 		
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.sentiment.interfaces.SentimentAnalyzer;

public class MovieReviewSentimentAnalyzer implements SentimentAnalyzer {

	private static final double UNKNOWN = -1.0;
	private static final double EPSILON = 0.01;
	private static final double NEGATIVE = 0.5;
	private static final double SOMEWHAT_NEGATIVE = 1.5;
	private static final double NEUTRAL = 2.5;
	private static final double SOMEWHAT_POSITIVE = 3.5;

	private Map<String, Word> wordsFromReviews;
	private Set<String> stopWords;
	private Set<String> reviews;
	private PrintStream streamForAppend;

	public MovieReviewSentimentAnalyzer(InputStream stopWordsInput, InputStream reviewsInput,
			OutputStream reviewsOutput) {

		wordsFromReviews = new HashMap<>();
		stopWords = new HashSet<>();
		reviews = new HashSet<>();

		try (BufferedReader bufferForStopWords = new BufferedReader(new InputStreamReader(stopWordsInput));
				BufferedReader bufferForReviews = new BufferedReader(new InputStreamReader(reviewsInput))) {
			addStopWordsToSet(bufferForStopWords);
			getReviewForTeach(bufferForReviews);
		} catch (IOException e) {
			e.printStackTrace();
		}
		streamForAppend = new PrintStream(reviewsOutput);
	}

	private void addStopWordsToSet(BufferedReader bufferForStopWords) throws IOException {
		while (bufferForStopWords.ready()) {
			String lineOfStopWords = bufferForStopWords.readLine();
			stopWords.add(lineOfStopWords.toLowerCase());
		}
	}

	private void getReviewForTeach(BufferedReader bufferForReviews) throws IOException {
		while (bufferForReviews.ready()) {
			String lineOfReview = bufferForReviews.readLine();
			teachTheAlgorithm(lineOfReview);
		}

	}

	private int getRatingOfReview(String review) {
		return Character.getNumericValue(review.charAt(0));
	}

	private List<String> splitTheWords(String review) {
		return new ArrayList<>(Arrays.asList(review.split("[^A-Za-z0-9]")));
	}

	private void getOnlyWordsWithMeaning(List<String> wordsFromReview) {
		wordsFromReview.removeAll(stopWords);
		wordsFromReview.removeAll(Arrays.asList(""));
	}

	private void increasingRatingOfWords(List<String> wordsFromReview, int rating) {
		for (String wordFromReview : wordsFromReview) {

			if (!wordsFromReviews.containsKey(wordFromReview)) {
				wordsFromReviews.put(wordFromReview, new Word());
			}
			wordsFromReviews.get(wordFromReview).increasing(rating);
		}
	}

	private void teachTheAlgorithm(String review) {

		int rating = getRatingOfReview(review);
		reviews.add(review.substring(2, review.length()));
		List<String> wordsFromReview = splitTheWords(review.substring(2, review.length()).toLowerCase());
		getOnlyWordsWithMeaning(wordsFromReview);
		increasingRatingOfWords(wordsFromReview, rating);

	}

	private double computeTheReview(List<String> wordsOfReview) {
		int counter = 0;
		double raiting = 0;
		for (String word : wordsOfReview) {
			if (wordsFromReviews.containsKey(word)) {
				++counter;
				raiting += wordsFromReviews.get(word).getWordWight();
			}
		}
		if (counter != 0) {
			return (double) raiting / (double) counter;
		}
		return UNKNOWN;
	}

	@Override
	public double getReviewSentiment(String review) {
		if (review == null) {
			return UNKNOWN;
		}
		List<String> wordsOfReview = splitTheWords(review.toLowerCase());
		getOnlyWordsWithMeaning(wordsOfReview);
		return computeTheReview(wordsOfReview);
	}

	@Override
	public String getReviewSentimentAsName(String review) {
		double reviewRating = getReviewSentiment(review);
		if (reviewRating == UNKNOWN) {
			return "unknown";
		}
		if (reviewRating < NEGATIVE) {
			return "negative";
		} else if (reviewRating < SOMEWHAT_NEGATIVE) {
			return "somewhat negative";
		} else if (reviewRating < NEUTRAL) {
			return "neutral";
		} else if (reviewRating < SOMEWHAT_POSITIVE) {
			return "somewhat positive";
		}
		return "positive";
	}

	@Override
	public double getWordSentiment(String word) {
		if (wordsFromReviews.containsKey(word.toLowerCase())) {
			return wordsFromReviews.get(word.toLowerCase()).getWordWight();
		}
		return UNKNOWN;
	}

	@Override
	public String getReview(double sentimentValue) {
		for (String s : reviews) {
			if (Math.abs(getReviewSentiment(s) - sentimentValue) < EPSILON) {
				return s;
			}
		}
		return null;
	}

	private void isNCorrect(int n) {
		if (n < 0) {
			throw new IllegalArgumentException("Positive number expected");
		}
	}

	@Override
	public Collection<String> getMostFrequentWords(int n) {
		isNCorrect(n);
		return wordsFromReviews.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.map(e -> e.getKey()).limit(n).collect(Collectors.toList());
	}

	@Override
	public Collection<String> getMostPositiveWords(int n) {
		isNCorrect(n);
		return wordsFromReviews.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue(Comparator.comparing(Word::getWordWight))))
				.map(e -> e.getKey()).limit(n).collect(Collectors.toList());
	}

	@Override
	public Collection<String> getMostNegativeWords(int n) {
		isNCorrect(n);
		return wordsFromReviews.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.comparing(Word::getWordWight))).map(e -> e.getKey())
				.limit(n).collect(Collectors.toList());
	}

	@Override
	public void appendReview(String review, int sentimentValue) {

		reviews.add(review);

		List<String> wordsFromReview = splitTheWords(review.toLowerCase());
		getOnlyWordsWithMeaning(wordsFromReview);
		increasingRatingOfWords(wordsFromReview, sentimentValue);
		StringBuffer newReview = new StringBuffer(sentimentValue + " " + review + System.lineSeparator());
		streamForAppend.append(newReview.toString());
		streamForAppend.flush();
	}

	@Override
	public int getSentimentDictionarySize() {
		return wordsFromReviews.size();
	}

	@Override
	public boolean isStopWord(String word) {
		return stopWords.contains(word.toLowerCase());
	}

}
