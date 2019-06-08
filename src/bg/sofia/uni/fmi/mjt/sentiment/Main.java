package bg.sofia.uni.fmi.mjt.sentiment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Main {

	public static void main(String[] args) {

		try (InputStream stopWords = new FileInputStream("resources/stopWords.txt");
				InputStream reviews = new FileInputStream("resources/reviews.txt");
				OutputStream reviewsOutput = new FileOutputStream("resources/reviews.txt", true)) {
			MovieReviewSentimentAnalyzer movie = new MovieReviewSentimentAnalyzer(stopWords, reviews, reviewsOutput);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
