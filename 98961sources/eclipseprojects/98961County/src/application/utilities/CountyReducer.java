package application.utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class CountyReducer extends Reducer<Text, Text, Text, Text> {

	@Override
	protected void reduce(Text county, Iterable<Text> yearNprices, Reducer<Text, Text, Text, Text>.Context reducerContext)
			throws IOException, InterruptedException {
		
		HashMap<Integer, ArrayList<Integer>> yearNPricesHashMap = new HashMap<>();
		for (Text text : yearNprices) {
//			2000###184000
			String[] yearNPrice = text.toString().split("###");
			if(yearNPricesHashMap.keySet().contains(Integer.parseInt(yearNPrice[0]))){
				yearNPricesHashMap.get(Integer.parseInt(yearNPrice[0])).add(Integer.parseInt(yearNPrice[1]));
			}else{
				ArrayList<Integer> pricesArrayList = new ArrayList<>();
				pricesArrayList.add(Integer.parseInt(yearNPrice[1]));
				yearNPricesHashMap.put(Integer.parseInt(yearNPrice[0]), pricesArrayList);
			}
		}
		
		Iterator<Integer> yearNPricesHashMapIterator = yearNPricesHashMap.keySet().iterator();
		while (yearNPricesHashMapIterator.hasNext()) {
			Integer year = (Integer) yearNPricesHashMapIterator.next();
			int totalPrice=0;
			ListIterator<Integer> pricesArrayListIterator = yearNPricesHashMap.get(year).listIterator();
			while (pricesArrayListIterator.hasNext()) {
				Integer price = (Integer) pricesArrayListIterator.next();
				totalPrice += price;
			}
			int totalPriceRecords = yearNPricesHashMap.get(year).size();
			int averagePrice = totalPrice / totalPriceRecords;
			reducerContext.write(county, new Text(String.valueOf(year + ": " + averagePrice)));
		}
		
	}

	
}
