package cmsc433.p5;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Map reduce which sorts the output of {@link TweetPopularityMR}.
 * The input will either be in the form of: </br>
 * 
 * <code></br>
 * &nbsp;(screen_name,  score)</br>
 * &nbsp;(hashtag, score)</br>
 * &nbsp;(tweet_id, score)</br></br>
 * </code>
 * 
 * The output will be in the same form, but with results sorted on the score.
 * 
 */
public class TweetSortMR {

	/**
	 * Minimum <code>int</code> value for a pair to be included in the output.
	 * Pairs with an <code>int</code> less than this value are omitted.
	 */
	private static int CUTOFF = 10;
	
	public static class SwapMapper extends Mapper<LongWritable, Text, SortKey, IntWritable> {
		
		String      id;
		int         score;

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] columns = value.toString().split("\t");
			id = columns[0];
			score = Integer.valueOf(columns[1]);
			
			if (score >= CUTOFF) {
				Text text = new Text(id);
				IntWritable weight = new IntWritable(score);
				context.write(new SortKey(text, weight), weight);
			}
			
		}
	}
	
	public static class SwapReducer extends Reducer<SortKey, IntWritable, Text, IntWritable> {
		
		@Override
		public void reduce(SortKey key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			for (IntWritable integer : values) {
				context.write(key.getText(), integer);
			}
		}
	}
	
	/**
	 * This method performs value-based sorting on the given input by configuring
	 * the job as appropriate and using Hadoop.
	 * 
	 * @param job
	 *          Job created for this function
	 * @param input
	 *          String representing location of input directory
	 * @param output
	 *          String representing location of output directory
	 * @return True if successful, false otherwise
	 * @throws Exception
	 */
	public static boolean sort(Job job, String input, String output, int cutoff) throws Exception {
		
		CUTOFF = cutoff;

		job.setJarByClass(TweetSortMR.class);

		// TODO: Set up map-reduce...
		job.setMapOutputKeyClass(SortKey.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setMapperClass(SwapMapper.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setReducerClass(SwapReducer.class);
		
		job.setPartitionerClass(SortPartitioner.class);
		job.setGroupingComparatorClass(SortGroupingComparator.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		// End

		FileInputFormat.addInputPath(job, new Path(input));
		FileOutputFormat.setOutputPath(job, new Path(output));

		return job.waitForCompletion(true);
	}

}
