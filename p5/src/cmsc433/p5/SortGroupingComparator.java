package cmsc433.p5;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class SortGroupingComparator extends WritableComparator{

	public SortGroupingComparator() {
		super(SortKey.class, true);
	}
	
	public int compare(WritableComparable wc1, WritableComparable wc2) {
		SortKey keyOne = (SortKey)wc1;
		SortKey keyTwo = (SortKey)wc2;
		return keyOne.getText().compareTo(keyTwo.getText());
	}
}
