package cmsc433.p5;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Map reduce which takes in a CSV file with tweets as input and output
 * key/value pairs.</br>
 * </br>
 * The key for the map reduce depends on the specified {@link TrendingParameter}
 * , <code>trendingOn</code> passed to
 * {@link #score(Job, String, String, TrendingParameter)}).
 */
public class TweetPopularityMR {
	
	// For your convenience...
	public static final int          TWEET_SCORE   = 1;
	public static final int          RETWEET_SCORE = 2;
	public static final int          MENTION_SCORE = 1;
	public static final int			 PAIR_SCORE = 1;
	
	// Is either USER, TWEET, HASHTAG, or HASHTAG_PAIR. Set for you before call to map()
	private static TrendingParameter trendingOn;
	
	public static class TweetMapper extends Mapper<LongWritable, Text, Object, IntWritable> {
		
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			// Converts the CSV line into a tweet object
			Tweet tweet = Tweet.createTweet(value.toString());
			
			if (trendingOn == TrendingParameter.HASHTAG) {
				
				List<String> hashTags = tweet.getHashtags();
				for (String hashTag : hashTags) {
					IntWritable count = new IntWritable(TWEET_SCORE);
					Text text = new Text(hashTag);
					context.write(text, count);
				}
				
			} else if (trendingOn == TrendingParameter.HASHTAG_PAIR) {
				
				List<String> hashTags = tweet.getHashtags();
				for (int i = 0; i < hashTags.size(); i++) {
					for (int j = i+1; j < hashTags.size(); j++) {
						String small = hashTags.get(i);
						String large = hashTags.get(j);
						
						if (small.compareTo(large) > 0) {
							String temp = small;
							small = large;
							large = temp;
						}
						
						String thePair = "("+small+","+large+")";
						Text text = new Text(thePair);
						context.write(text, new IntWritable(PAIR_SCORE));
					}
				}
				
			} else if (trendingOn == TrendingParameter.TWEET) {
				if (tweet.wasRetweetOfTweet()) {
					Long idRetweet = tweet.getRetweetedTweet();
					LongWritable longWrite = new LongWritable(idRetweet);
					context.write(longWrite, new IntWritable(RETWEET_SCORE));
				} 
				
				Long idTweet = tweet.getId();
				LongWritable original = new LongWritable(idTweet);
				context.write(original, new IntWritable(TWEET_SCORE));
				
			} else if (trendingOn == TrendingParameter.USER) {
				
				List<String> mentions = tweet.getMentionedUsers();
				for (String names : mentions) {
					Text text = new Text(names);
					context.write(text, new IntWritable(MENTION_SCORE));
				}
				
				if (tweet.wasRetweetOfUser()) {
					String retweetName = tweet.getRetweetedUser();
					Text text = new Text(retweetName);
					context.write(text, new IntWritable(RETWEET_SCORE));
				}
				
				String user = tweet.getUserScreenName();
				Text lastOne = new Text(user);
				context.write(lastOne, new IntWritable(TWEET_SCORE));
			}
		}
	}
	
	public static class PopularityReducer extends Reducer<Object, IntWritable, Text, IntWritable> {
		
		@Override
		public void reduce(Object key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			int value = 0;
			for (IntWritable weight : values) {
				value += weight.get();
			}
			
			String string = key.toString();
			context.write(new Text(string), new IntWritable(value));
		}
	}
	
	/**
	 * Method which performs a map reduce on a specified input CSV file and
	 * outputs the scored tweets, users, or hashtags.</br>
	 * </br>
	 * 
	 * @param job
	 * @param input
	 *          The CSV file containing tweets
	 * @param output
	 *          The output file with the scores
	 * @param trendingOn
	 *          The parameter on which to score
	 * @return true if the map reduce was successful, false otherwise.
	 * @throws Exception
	 */
	public static boolean score(Job job, String input, String output, TrendingParameter trendingOn) throws Exception {
		
		TweetPopularityMR.trendingOn = trendingOn;

		job.setJarByClass(TweetPopularityMR.class);

		// TODO: Set up map-reduce...
		if (TweetPopularityMR.trendingOn == TrendingParameter.TWEET) {
			job.setMapOutputKeyClass(LongWritable.class);
		} else {
			job.setMapOutputKeyClass(Text.class);
		}
		
		job.setMapOutputValueClass(IntWritable.class);
		job.setMapperClass(TweetMapper.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setReducerClass(PopularityReducer.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		// End

		FileInputFormat.addInputPath(job, new Path(input));
		FileOutputFormat.setOutputPath(job, new Path(output));

		return job.waitForCompletion(true);
	}
}
