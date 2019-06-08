package bg.sofia.uni.fmi.mjt.sentiment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class MovieReviewSentimentAnalyzerTest {

    private static final double DELTA = 0.01;

    private static final int NUMBER_OF_WORDS = 114;

    private static final int POSITIVE_NEGATIVE = 3;

    private static final int FREQUENT = 2;

    private static final double SENTIMENT_VALUE = 1.0;

    private static final int EXCEPTIONAL_VALUE = -3;

    private static final double VALUE_NULL = 2;

    private static final double UNKNOWN = -1.0;

    private static final double SENTIMENT = 4.0;

    private static final String REVIEW = "But it does n't leave you with much .	";

    private MovieReviewSentimentAnalyzer analyzer;

    private InputStream reviewsStream;
    private InputStream stopWordsStream;
    private OutputStream resultStream;

    @Before
    public void init() throws FileNotFoundException {
        stopWordsStream = new FileInputStream("resources/stopWords.txt");
        reviewsStream = new FileInputStream("resources/reviews.txt");
        resultStream = new FileOutputStream("resources/reviews.txt", true);
        analyzer = new MovieReviewSentimentAnalyzer(stopWordsStream, reviewsStream, resultStream);
    }

    @Test
    public void testIsUnknownReviewSentimental() {
        String review = new String("The film was outstanding");
        assertEquals(UNKNOWN, analyzer.getReviewSentiment(review), DELTA);
    }

    @Test
    public void testIsCorrectReviewSentimental() {
        String review = new String("You could hate or like it");
        assertEquals(2.0, analyzer.getReviewSentiment(review), DELTA);
    }

    @Test
    public void testIsNeutralReviewSentimentalAsName() {
        String review = new String("You could hate or like it");
        assertEquals("neutral", analyzer.getReviewSentimentAsName(review));
    }

    @Test
    public void testIsSomewhatPositiveReviewSentimentalAsName() {
        String review = new String("Proportions earnest juicy.");
        assertEquals("somewhat positive", analyzer.getReviewSentimentAsName(review));
    }

    @Test
    public void testIsNegativeReviewSentimentalAsName() {
        String review = new String("Would have a hard time sitting through this one, hate it .");
        assertNotEquals("somewhat positive", analyzer.getReviewSentimentAsName(review));
    }

    @Test
    public void testIsCorrectWordSentiment() {
        assertEquals(SENTIMENT, analyzer.getWordSentiment("absolute"), DELTA);
    }

    @Test
    public void testIsUnknownWordSentiment() {
        assertEquals(UNKNOWN, analyzer.getWordSentiment("disgusting"), DELTA);
    }

    @Test
    public void testIsNullGetReview() {
        assertNull(analyzer.getReview(VALUE_NULL));
    }

    @Test
    public void testGetReview() {
        assertEquals(REVIEW, analyzer.getReview(SENTIMENT_VALUE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionIllegalArgumentException() {
        analyzer.getMostFrequentWords(EXCEPTIONAL_VALUE);
    }

    @Test
    public void testGetMostPositiveWords() {
        List<String> strings = (List<String>) analyzer.getMostPositiveWords(POSITIVE_NEGATIVE);
        Set<String> set = new HashSet<>();
        set.add("undergoing");
        set.add("character");
        set.add("epic");
        assertTrue(strings.containsAll(set));
    }

    @Test
    public void testGetMostNegativeWords() {
        List<String> strings = (List<String>) analyzer.getMostNegativeWords(POSITIVE_NEGATIVE);
        Set<String> set = new HashSet<>();
        set.add("reason");
        set.add("none");
        set.add("youth");
        assertTrue(strings.containsAll(set));
    }

    @Test
    public void testGetMostFrequentWords() {
        List<String> strings = (List<String>) analyzer.getMostFrequentWords(FREQUENT);
        Set<String> set = new HashSet<>();
        set.add("much");
        set.add("s");
        assertTrue(strings.containsAll(set));
    }

    @Test
    public void testGetSentimentDictionarySize() {
        assertEquals(analyzer.getSentimentDictionarySize(), NUMBER_OF_WORDS);
    }

    @Test
    public void testIsStopWordNegativeFromDictionary() {
        String assertMessage = "A word should not be incorrectly identified as a stop word,"
                + " if it is not part of the stop words list";
        assertFalse(assertMessage, analyzer.isStopWord("effects"));
    }

    @Test
    public void testIsStopWordNegativeNotFromDictionary() {
        String assertMessage = "A word should not be incorrectly identified as a stop word,"
                + " if it is not part of the stop words list";
        assertFalse(assertMessage, analyzer.isStopWord("stoyo"));
    }

    @Test
    public void testIsStopWordPositive() {
        assertTrue("Stop word not counted as stop word", analyzer.isStopWord("a"));
    }

    @After
    public void terminate() {
        try {
            reviewsStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            stopWordsStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            resultStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}