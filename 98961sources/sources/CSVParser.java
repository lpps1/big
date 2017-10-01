package application.utilities;
 
import java.io.IOException;
import java.util.ArrayList;
 import java.util.List;

import org.apache.commons.lang3.StringUtils;
 
 public class CSVParser
 {
   public static final char DEFAULT_SEPARATOR = ',';
   public static final int INITIAL_READ_SIZE = 1024;
   public static final int READ_BUFFER_SIZE = 128;
   public static final char DEFAULT_QUOTE_CHARACTER = '"';
   public static final char DEFAULT_ESCAPE_CHARACTER = '\\';
   public static final boolean DEFAULT_STRICT_QUOTES = false;
   public static final boolean DEFAULT_IGNORE_LEADING_WHITESPACE = true;
   public static final boolean DEFAULT_IGNORE_QUOTATIONS = false;
   public static final char NULL_CHARACTER = '\000';
/*  81 */   public static final CSVReaderNullFieldIndicator DEFAULT_NULL_FIELD_INDICATOR = CSVReaderNullFieldIndicator.NEITHER;
   
 
 
   private final char separator;
   
 
   private final char quotechar;
   
 
   private final char escape;
   
 
   private final boolean strictQuotes;
   
 
   private final boolean ignoreLeadingWhiteSpace;
   
 
   private final boolean ignoreQuotations;
   
 
   private final CSVReaderNullFieldIndicator nullFieldIndicator;
   
 
   private String pending;
   
 
/* 109 */   private boolean inField = false;
   
 
 
   public CSVParser()
   {
/* 115 */     this(',', '"', '\\');
   }
   
 
 
 
 
   public CSVParser(char separator)
   {
/* 124 */     this(separator, '"', '\\');
   }
   
 
 
 
 
 
 
   public CSVParser(char separator, char quotechar)
   {
/* 135 */     this(separator, quotechar, '\\');
   }
   
 
 
 
 
 
 
   public CSVParser(char separator, char quotechar, char escape)
   {
/* 146 */     this(separator, quotechar, escape, false);
   }
   
 
 
 
 
 
 
 
 
   public CSVParser(char separator, char quotechar, char escape, boolean strictQuotes)
   {
/* 159 */     this(separator, quotechar, escape, strictQuotes, true);
   }
   
 
 
 
 
 
 
 
 
 
   public CSVParser(char separator, char quotechar, char escape, boolean strictQuotes, boolean ignoreLeadingWhiteSpace)
   {
/* 173 */     this(separator, quotechar, escape, strictQuotes, ignoreLeadingWhiteSpace, false);
   }
   
 
 
 
 
 
 
 
 
 
 
 
   public CSVParser(char separator, char quotechar, char escape, boolean strictQuotes, boolean ignoreLeadingWhiteSpace, boolean ignoreQuotations)
   {
/* 189 */     this(separator, quotechar, escape, strictQuotes, ignoreLeadingWhiteSpace, ignoreQuotations, DEFAULT_NULL_FIELD_INDICATOR);
   }
   
 
 
 
 
 
 
 
 
 
 
 
 
 
   CSVParser(char separator, char quotechar, char escape, boolean strictQuotes, boolean ignoreLeadingWhiteSpace, boolean ignoreQuotations, CSVReaderNullFieldIndicator nullFieldIndicator)
   {
/* 207 */     if (anyCharactersAreTheSame(separator, quotechar, escape)) {
/* 208 */       throw new UnsupportedOperationException("The separator, quote, and escape characters must be different!");
     }
/* 210 */     if (separator == 0) {
/* 211 */       throw new UnsupportedOperationException("The separator character must be defined!");
     }
/* 213 */     this.separator = separator;
/* 214 */     this.quotechar = quotechar;
/* 215 */     this.escape = escape;
/* 216 */     this.strictQuotes = strictQuotes;
/* 217 */     this.ignoreLeadingWhiteSpace = ignoreLeadingWhiteSpace;
/* 218 */     this.ignoreQuotations = ignoreQuotations;
/* 219 */     this.nullFieldIndicator = nullFieldIndicator;
   }
   
 
 
 
   public char getSeparator()
   {
/* 227 */     return this.separator;
   }
   
 
 
   public char getQuotechar()
   {
/* 234 */     return this.quotechar;
   }
   
 
 
   public char getEscape()
   {
/* 241 */     return this.escape;
   }
   
 
 
   public boolean isStrictQuotes()
   {
/* 248 */     return this.strictQuotes;
   }
   
 
 
   public boolean isIgnoreLeadingWhiteSpace()
   {
/* 255 */     return this.ignoreLeadingWhiteSpace;
   }
   
 
 
   public boolean isIgnoreQuotations()
   {
/* 262 */     return this.ignoreQuotations;
   }
   
 
 
 
 
 
 
 
 
   private boolean anyCharactersAreTheSame(char separator, char quotechar, char escape)
   {
/* 275 */     return (isSameCharacter(separator, quotechar)) || (isSameCharacter(separator, escape)) || (isSameCharacter(quotechar, escape));
   }
   
 
 
 
 
 
   private boolean isSameCharacter(char c1, char c2)
   {
/* 285 */     return (c1 != 0) && (c1 == c2);
   }
   
 
 
   public boolean isPending()
   {
/* 292 */     return this.pending != null;
   }
   
 
 
 
 
 
 
   public String[] parseLineMulti(String nextLine)
     throws IOException
   {
/* 304 */     return parseLine(nextLine, true);
   }
   
 
 
 
 
 
 
   public String[] parseLine(String nextLine)
     throws IOException
   {
/* 316 */     return parseLine(nextLine, false);
   }
   
 
 
 
 
 
 
 
   private String[] parseLine(String nextLine, boolean multi)
     throws IOException
   {
/* 329 */     if ((!multi) && (this.pending != null)) {
/* 330 */       this.pending = null;
     }
     
/* 333 */     if (nextLine == null) {
/* 334 */       if (this.pending != null) {
/* 335 */         String s = this.pending;
/* 336 */         this.pending = null;
/* 337 */         return new String[] { s };
       }
/* 339 */       return null;
     }
     
 
/* 343 */     List<String> tokensOnThisLine = new ArrayList();
/* 344 */     StringBuilder sb = new StringBuilder(nextLine.length() + 128);
/* 345 */     boolean inQuotes = false;
/* 346 */     boolean fromQuotedField = false;
/* 347 */     if (this.pending != null) {
/* 348 */       sb.append(this.pending);
/* 349 */       this.pending = null;
/* 350 */       inQuotes = !this.ignoreQuotations;
     }
/* 352 */     for (int i = 0; i < nextLine.length(); i++)
     {
/* 354 */       char c = nextLine.charAt(i);
/* 355 */       if (c == this.escape) {
/* 356 */         if (isNextCharacterEscapable(nextLine, inQuotes(inQuotes), i)) {
/* 357 */           i = appendNextCharacterAndAdvanceLoop(nextLine, sb, i);
         }
/* 359 */       } else if (c == this.quotechar) {
/* 360 */         if (isNextCharacterEscapedQuote(nextLine, inQuotes(inQuotes), i)) {
/* 361 */           i = appendNextCharacterAndAdvanceLoop(nextLine, sb, i);
         }
         else {
/* 364 */           inQuotes = !inQuotes;
/* 365 */           if (atStartOfField(sb)) {
/* 366 */             fromQuotedField = true;
           }
           
 
/* 370 */           if ((!this.strictQuotes) && 
/* 371 */             (i > 2) && (nextLine.charAt(i - 1) != this.separator) && (nextLine.length() > i + 1) && (nextLine.charAt(i + 1) != this.separator))
           {
 
 
 
 
/* 377 */             if ((this.ignoreLeadingWhiteSpace) && (sb.length() > 0) && (isAllWhiteSpace(sb))) {
/* 378 */               sb.setLength(0);
             } else {
/* 380 */               sb.append(c);
             }
           }
         }
         
 
/* 386 */         this.inField = (!this.inField);
/* 387 */       } else if ((c == this.separator) && ((!inQuotes) || (this.ignoreQuotations))) {
/* 388 */         tokensOnThisLine.add(convertEmptyToNullIfNeeded(sb.toString(), fromQuotedField));
/* 389 */         fromQuotedField = false;
/* 390 */         sb.setLength(0);
/* 391 */         this.inField = false;
       }
/* 393 */       else if ((!this.strictQuotes) || ((inQuotes) && (!this.ignoreQuotations))) {
/* 394 */         sb.append(c);
/* 395 */         this.inField = true;
/* 396 */         fromQuotedField = true;
       }
     }
     
 
 
     if ((inQuotes) && (!this.ignoreQuotations)) {
       if (multi)
       {
         sb.append('\n');
        this.pending = sb.toString();
         sb = null;
       } else {
         throw new IOException("Un-terminated quoted field at end of CSV line");
       }
       if (this.inField) {
         fromQuotedField = true;
       }
     } else {
       this.inField = false;
     }
     
     if (sb != null) {
       tokensOnThisLine.add(convertEmptyToNullIfNeeded(sb.toString(), fromQuotedField));
       fromQuotedField = false;
     }
     return (String[])tokensOnThisLine.toArray(new String[tokensOnThisLine.size()]);
   }
   
   private boolean atStartOfField(StringBuilder sb)
   {
    return sb.length() == 0;
   }
   
   private String convertEmptyToNullIfNeeded(String s, boolean fromQuotedField) {
     if ((s.isEmpty()) && (shouldConvertEmptyToNull(fromQuotedField))) {
       return null;
     }
     return s;
   }
   
   private boolean shouldConvertEmptyToNull(boolean fromQuotedField) {
     switch (this.nullFieldIndicator) {
     case BOTH: 
       return true;
     case EMPTY_SEPARATORS: 
      return !fromQuotedField;
     case EMPTY_QUOTES: 
       return fromQuotedField;
     }
    return false;
   }
   
 
 
 
 
 
 
 
 
   private int appendNextCharacterAndAdvanceLoop(String line, StringBuilder sb, int i)
   {
     sb.append(line.charAt(i + 1));
     i++;
    return i;
   }
   
 
 
 
 
 
   private boolean inQuotes(boolean inQuotes)
   {
     return ((inQuotes) && (!this.ignoreQuotations)) || (this.inField);
   }
   
 
 
 
 
 
 
 
 
 
   private boolean isNextCharacterEscapedQuote(String nextLine, boolean inQuotes, int i)
   {
    return (inQuotes) && (nextLine.length() > i + 1) && (isCharacterQuoteCharacter(nextLine.charAt(i + 1)));
   }
   
 
 
 
 
 
 
 
   private boolean isCharacterQuoteCharacter(char c)
   {
     return c == this.quotechar;
   }
   
 
 
 
 
 
   private boolean isCharacterEscapeCharacter(char c)
   {
/* 507 */     return c == this.escape;
   }
   
 
 
 
 
 
 
   private boolean isCharacterEscapable(char c)
   {
     return (isCharacterQuoteCharacter(c)) || (isCharacterEscapeCharacter(c));
   }
   
 
 
 
 
 
 
 
 
 
 
 
   protected boolean isNextCharacterEscapable(String nextLine, boolean inQuotes, int i)
   {
     return (inQuotes) && (nextLine.length() > i + 1) && (isCharacterEscapable(nextLine.charAt(i + 1)));
   }
   
 
 
 
 
 
 
 
 
 
   protected boolean isAllWhiteSpace(CharSequence sb)
   {
     return StringUtils.isWhitespace(sb);
   }
   
 
 
   public CSVReaderNullFieldIndicator nullFieldIndicator()
   {
     return this.nullFieldIndicator;
   }
 }
