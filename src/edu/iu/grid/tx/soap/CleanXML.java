package edu.iu.grid.tx.soap;

public class CleanXML {
	public static String removeBadXMLChars( final String inString ) {
		StringBuilder buf = new StringBuilder(inString.length());
		for(int i = 0; i < inString.length(); ++i) {
			char c = inString.charAt(i);
		    if ((c == 0x9) ||
			        (c == 0xA) ||
			        (c == 0xD) ||
			        ((c >= 0x20) && (c <= 0xD7FF)) ||
			        ((c >= 0xE000) && (c <= 0xFFFD)) ||
			        ((c >= 0x10000) && (c <= 0x10FFFF)))
			    {
			        buf.append(c);
			    } else {
			    	buf.append('?');
			    }
			    
		}
		return buf.toString();
	}
}
