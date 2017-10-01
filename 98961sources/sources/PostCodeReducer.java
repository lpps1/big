package application.utilities;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class PostCodeReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

	@Override
	protected void reduce(Text postCode, Iterable<IntWritable> prices, Reducer<Text, IntWritable, Text, IntWritable>.Context reducerContext)
			throws IOException, InterruptedException {
		
		int maximumPrice = 0;
		for (IntWritable intWritable : prices) {
			int price = intWritable.get();
			if(price>maximumPrice){
				maximumPrice = price;
			}
		}
		
		reducerContext.write(postCode, new IntWritable(maximumPrice));
	}

	
}
