package cmsc433.p5;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

public class SortKey implements Writable, WritableComparable<SortKey>{
	
	private Text dataField;
	private IntWritable weight;
	
	public SortKey(Text dataField, IntWritable weight) {
		this.dataField = dataField;
		this.weight = weight;
	}
	
	public SortKey() {
		dataField = new Text();
		weight = new IntWritable();
	}
	
	public Text getText() {
		return dataField;
	}
	
	public IntWritable getWeight() {
		return weight;
	}
	
	//Compare/sort on the score associated with the original key.
	@Override
	public int compareTo(SortKey o) {
		int result = this.getWeight().compareTo(o.getWeight());
		return (-1*result);
	}
	
	@Override
	public void readFields(DataInput arg0) throws IOException {
		dataField.readFields(arg0);
		weight.readFields(arg0);
	}
	
	@Override
	public void write(DataOutput arg0) throws IOException {
		dataField.write(arg0);
		weight.write(arg0);
	}

}
