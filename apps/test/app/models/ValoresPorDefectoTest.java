
package models;

import java.util.*;
import javax.persistence.*;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.data.validation.*;
import org.joda.time.DateTime;
import models.*;
import messages.Messages;
import validation.*;
import audit.Auditable;

// === IMPORT REGION START ===
			
// === IMPORT REGION END ===
	

@Auditable
@Entity
public class ValoresPorDefectoTest extends Model {
	// Código de los atributos
	
	
	public String mString;
	
	
	
	public Long mLong;
	
	
	
	public Integer mInteger;
	
	
	
	public Boolean mBoolean;
	
	
	
	public Double mDouble;
	
	
	@Column(columnDefinition="LONGTEXT")
	public String mLongText;
	
	
	
	public String mTelefono;
	
	
	@Email
	public String mEmail;
	
	
	@Email
	public String mEmail2;
	
	
	@CheckWith(CifCheck.class)
	public String mCif;
	
	
	@Moneda
	public Double mMoneda;
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="mDateTime"),@Column(name="mDateTimeTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime mDateTime;
	
	
	@ValueFromTable("ComboTestList")
	public String mLista;
	
	
	@Embedded
	public Embebida mEmbebida;
	
	
	public ValoresPorDefectoTest (){
		init();
	}
	

	public void init(){
		
		mString = "string";
mLong = 2L;
mInteger = 4;
mBoolean = true;
mDouble = 2.345;
mLongText = "texto largooooo largiiisimo";
mTelefono = "900 120 120";
mEmail = "asas@pepe.com";
mCif = "1234567A";
mMoneda = 2.1;
mDateTime = new DateTime ("12/12/2010");
if (TableKeyValue.contains("ComboTestList", "c"))	mLista = "c";
			if (mEmbebida == null)
				mEmbebida = new Embebida();
			
	}
		
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		