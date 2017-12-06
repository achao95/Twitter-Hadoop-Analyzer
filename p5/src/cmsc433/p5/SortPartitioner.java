package cmsc433.p5;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Partitioner;

public class SortPartitioner extends Partitioner<SortKey, IntWritable>{

	//Divide off the score's hash code.
	@Override
	public int getPartition(SortKey arg0, IntWritable arg1, int numPartitions) {
		if (numPartitions == 0) {
			return 0;
		}
		return Math.abs(arg0.getWeight().hashCode() % numPartitions);
	}

}
