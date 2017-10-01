package application.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateCheckTrial {
	
	public static void main(String[] args) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd HH:mm");
		String simpleDate = "2015-06-01 00:00";
		try {
			Date date = simpleDateFormat.parse(simpleDate);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			int year = calendar.get(Calendar.YEAR);
			System.out.println("I need Year:" + year);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}

}
