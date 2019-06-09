package bg.sofia.uni.fmi.mjt.sentiment;

import bg.sofia.uni.fmi.mjt.sentiment.interfaces.SentimentAnalyzer;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

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
        initialize(stopWordsInput, reviewsInput, reviewsOutput);
    }

    private void initialize(InputStream stopWordsInput, InputStream reviewsInput,
                            OutputStream reviewsOutput) {
        wordsFromReviews = new HashMap<>();
        stopWords = new HashSet<>();
        reviews = new HashSet<>();
        setReaders(stopWordsInput, reviewsInput);
        streamForAppend = new PrintStream(reviewsOutput);
    }

    private void setReaders(InputStream stopWordsInput, InputStream reviewsInput) {
        try (BufferedReader bufferForStopWords = new BufferedReader(new InputStreamReader(stopWordsInput));
             BufferedReader bufferForReviews = new BufferedReader(new InputStreamReader(reviewsInput))) {
            addStopWordsToSet(bufferForStopWords);
            getReviewForTeach(bufferForReviews);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addStopWordsToSet(BufferedReader bufferForStopWords) throws IOException {
        while (isBufferReady(bufferForStopWords)) {
            String lineOfStopWords = bufferForStopWords.readLine();
            stopWords.add(lineOfStopWords.toLowerCase());
        }
    }

    private boolean isBufferReady(BufferedReader buffer) throws IOException {
        return buffer.ready();
    }

    private void getReviewForTeach(BufferedReader bufferForReviews) throws IOException {
        while (isBufferReady(bufferForReviews)) {
            String lineOfReview = bufferForReviews.readLine();
            teachTheAlgorithm(lineOfReview);
        }
    }

    private void teachTheAlgorithm(String review) {
        int rating = getRatingForReview(review);
        int reviewLength = review.length();
        reviews.add(review.substring(2, reviewLength));
        List<String> wordsFromReview = splitTheWords(review.substring(2, reviewLength).toLowerCase());
        getOnlyWordsWithMeaning(wordsFromReview);
        increaseRatingOfWords(wordsFromReview, rating);
    }

    private int getRatingForReview(String review) {
        return Character.getNumericValue(review.charAt(0));
    }

    private List<String> splitTheWords(String review) {
        final String REGEX = "[^A-Za-z0-9]";
        List<String> words = new ArrayList<>(Arrays.asList(review.split(REGEX)));
        return words;
    }

    private void getOnlyWordsWithMeaning(List<String> wordsFromReview) {
        wordsFromReview.removeAll(stopWords);
        wordsFromReview.removeAll(Arrays.asList(""));
    }

    private void increaseRatingOfWords(List<String> wordsFromReview, int rating) {
        for (String wordFromReview : wordsFromReview) {
            if (isWordNotContainedInWordFromReview(wordFromReview)) {
                wordsFromReviews.put(wordFromReview, new Word());
            }
            wordsFromReviews.get(wordFromReview).increasing(rating);
        }
    }

    private boolean isWordNotContainedInWordFromReview(String wordFromReview) {
        return !wordsFromReviews.containsKey(wordFromReview);
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
            return raiting / (double) counter;
        }
        return UNKNOWN;
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
        for (String review : reviews) {
            if (isReviewMatch(review, sentimentValue)) {
                return review;
            }
        }
        return null;
    }

    private boolean isReviewMatch(String review, double sentimentValue) {
        double result = getReviewSentiment(review) - sentimentValue;
        return Math.abs(result) < EPSILON;
    }

    @Override
    public Collection<String> getMostFrequentWords(int n) {
        checkIsNCorrect(n);
        return wordsFromReviews
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .map(e -> e.getKey())
                .limit(n)
                .collect(Collectors.toList());
    }

    private void checkIsNCorrect(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Positive number expected");
        }
    }

    @Override
    public Collection<String> getMostPositiveWords(int n) {
        checkIsNCorrect(n);
        return wordsFromReviews.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue(Comparator.comparing(Word::getWordWight))))
                .map(e -> e.getKey()).limit(n).collect(Collectors.toList());
    }

    @Override
    public Collection<String> getMostNegativeWords(int n) {
        checkIsNCorrect(n);
        return wordsFromReviews.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparing(Word::getWordWight)))
                .map(e -> e.getKey())
                .limit(n)
                .collect(Collectors.toList());
    }

    @Override
    public void appendReview(String review, int sentimentValue) {
        reviews.add(review);
        List<String> wordsFromReview = splitTheWords(review.toLowerCase());
        getOnlyWordsWithMeaning(wordsFromReview);
        increaseRatingOfWords(wordsFromReview, sentimentValue);
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
