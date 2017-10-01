package application.utilities;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


/*
 * _______________________________________________________________________________________________________________________________
 * |            Data item               |                     Explanation (where appropriate)                                     |     
 * |____________________________________|_________________________________________________________________________________________|
 * |Transaction Unique Identifier       | A reference number which is generated automatically recording each published sale.      |
 * |                                    | The number is unique and will change each time a sale is recorded.                      |
 * |____________________________________|_________________________________________________________________________________________|
 * |Price                               | Sale price stated on the transfer deed.                                                 |
 * |____________________________________|_________________________________________________________________________________________|
 * |Date of Transfer                    | Date when the sale was completed, as stated on the transfer deed. ()                    |
 * |____________________________________|_________________________________________________________________________________________|
 * |Postcode                            | This is the postcode used at the time of the original transaction.                      | 
 * |                                    | Note that postcodes can be reallocated and these changes are not reflected in the       | 
 * |                                    | Price Paid Dataset.                                                                     |
 * |____________________________________|_________________________________________________________________________________________|
 * |Property Type                       | D = Detached, S = Semi-Detached, T = Terraced, F = Flats/Maisonettes, O = Other         |
 * |                                    | Note that:                                                                              |
 * |                                    | - we only record the above categories to describe property type, we do not separately   |
 * |                                    |   identify bungalows.                                                                   |
 * |                                    | - end-of-terrace properties are included in the Terraced category above.                |
 * |                                    | - ‘Other’ is only valid where the transaction relates to a property type that is not    |
 * |                                    |   covered by existing values.                                                           |
 * |____________________________________|_________________________________________________________________________________________| 
 * |Old/New                             | Indicates the age of the property and applies to all price paid transactions,           |
 * |                                    | residential and non-residential.                                                        |
 * |                                    | Y = a newly built property, N = an established residential building                     |
 * |____________________________________|_________________________________________________________________________________________|
 * |Duration                            | Relates to the tenure: F = Freehold, L= Leasehold etc.                                  |
 * |                                    | Note that Land Registry does not record leases of 7 years or less in the                |
 * |                                    | Price Paid Dataset.                                                                     |
 * |____________________________________|_________________________________________________________________________________________|
 * | PAON                               | Primary Addressable Object Name.                                                        |
 * |                                    | If there is a sub-building for example the building is divided into flats,              |
 * |                                    | see Secondary Addressable Object Name (SAON).                                           |
 * |____________________________________|_________________________________________________________________________________________|
 * | SAON                               | Secondary Addressable Object Name.                                                      |
 * |                                    | If there is a sub-building, for example the building is divided into flats,             |
 * |                                    | there will be a SAON.                                                                   |
 * |____________________________________|_________________________________________________________________________________________|
 * | Street                             |                                                                                         |
 * |____________________________________|_________________________________________________________________________________________|
 * | Locality                           |                                                                                         |
 * |____________________________________|_________________________________________________________________________________________|
 * | Town/City                          |                                                                                         |
 * |____________________________________|_________________________________________________________________________________________|
 * | District                           |                                                                                         |
 * |____________________________________|_________________________________________________________________________________________|
 * | County                             |                                                                                         |
 * |____________________________________|_________________________________________________________________________________________|
 * | PPD Category Type                  | Indicates the type of Price Paid transaction.                                           |
 * |                                    | A = Standard Price Paid entry, includes single residential property sold for            |
 * |                                    | full market value.                                                                      |
 * |                                    | B = Additional Price Paid entry including transfers under a power of sale/repossessions,|  
 * |                                    | buy-to-lets (where they can be identified by a Mortgage) and transfers to               |  
 * |                                    | non-private individuals.                                                                |
 * |                                    | Note that category B does not separately identify the transaction types stated.         |
 * |                                    | Land Registry has been collecting information on Category A transactions from           |
 * |                                    | January 1995. Category B transactions were identified from October 2013.                |
 * |____________________________________|_________________________________________________________________________________________|
 * | Record Status - monthly file only  | Indicates additions, changes and deletions to the records.(see guide below).            |
 * |                                    | A = Addition                                                                            |
 * |                                    | C = Change                                                                              |
 * |                                    | D = Delete.                                                                             |
 * |                                    | Note that where a transaction changes category type due to misallocation (as above) it  |
 * |                                    | will be deleted from the original category type and added to the correct category with a| 
 * |                                    | new transaction unique identifier.                                                      |
 * |____________________________________|_________________________________________________________________________________________|
 * 
 */

/*
 * CSV Headers Field Names
 * 1. Transaction Unique Identifier
 * 2. Price
 * 3. Date of Transfer
 * 4. Postcode
 * 5. Property Type
 * 6. Old/New
 * 7. Duration
 * 8. PAON
 * 9. SAON
 * 10. Street
 * 11. Locality
 * 12. Town/City 
 * 13. District
 * 14. County
 * 15. PPD Category Type
 * 16. Record Status - monthly file only
 */

public class PostCodeMapper extends Mapper<LongWritable, Text, Text, IntWritable>{

	private final String CSV_HEADER_FIELD_PROPERTY$TYPE_VALUE_DETACHED = "D";
	private final String CSV_HEADER_FIELD_PRICE$PAID$TRANSACTION$CATEGORY$TYPE_VALUE_STANDARD$PRICE$PAID = "A";
	private final String CSV_HEADER_FIELD_RECORD$STATUS_VALUE_ADDITION = "A";
	private final int CSV_HEADER_FIELD_PRICE_VALUE_MINIMUM = 4000000;
	private final int CSV_HEADER_FIELD_PRICE_VALUE_MAXIMUM = 5000000;
	
	@Override
	protected void map(LongWritable key, Text value,Mapper<LongWritable, Text, Text, IntWritable>.Context context)
			throws IOException, InterruptedException {
		if(key.get()>0){
			CSVParser csvParser = new CSVParser();
			String[] csvFileLine = csvParser.parseLine(value.toString());
			String transactionUniqueIdentifier = csvFileLine[0];
			String price = csvFileLine[1];
			String dateOfTransfer = csvFileLine[2];
			String postCode = csvFileLine[3];
			String propertyType = csvFileLine[4];
			String oldOrNew = csvFileLine[5];
			String duration = csvFileLine[6];
			String paon = csvFileLine[7];
			String saon = csvFileLine[8];
			String street = csvFileLine[9];
			String locality = csvFileLine[10];
			String townOrCity = csvFileLine[11];
			String district = csvFileLine[12];
			String county = csvFileLine[13];
			String pricePaidCategoryType = csvFileLine[14];
			String recordStatus = csvFileLine[15];
			
			if(propertyType.equalsIgnoreCase(CSV_HEADER_FIELD_PROPERTY$TYPE_VALUE_DETACHED)){
				if(pricePaidCategoryType.equalsIgnoreCase(CSV_HEADER_FIELD_PRICE$PAID$TRANSACTION$CATEGORY$TYPE_VALUE_STANDARD$PRICE$PAID)){
					if(recordStatus.equalsIgnoreCase(CSV_HEADER_FIELD_RECORD$STATUS_VALUE_ADDITION)){
						int priceInteger = Integer.parseInt(price); 
						if((priceInteger>=CSV_HEADER_FIELD_PRICE_VALUE_MINIMUM)&&(priceInteger<=CSV_HEADER_FIELD_PRICE_VALUE_MAXIMUM)){
							System.out.println("Found Eligible Record Entry ID:>" + transactionUniqueIdentifier);
							System.out.println("Postal Code:>" + postCode);
							System.out.println("Price:>" + priceInteger);
							context.write(new Text(postCode), new IntWritable(priceInteger));
						}
					}
				}
			}
		}
	}

	
	
}
