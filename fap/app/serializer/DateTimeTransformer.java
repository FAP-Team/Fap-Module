package serializer;

import java.text.SimpleDateFormat;

import org.joda.time.DateTime;

import flexjson.transformer.AbstractTransformer;

public class DateTimeTransformer  extends AbstractTransformer {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); 
	
	public void transform(Object object) {
		DateTime dateTime = (DateTime) object;
		getContext().writeQuoted((String) dateFormat.format(dateTime.toDate()));
	}

}
