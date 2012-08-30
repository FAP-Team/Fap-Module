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
import java.text.ParseException;
import java.text.SimpleDateFormat;

// === IMPORT REGION START ===

// === IMPORT REGION END ===

@Entity
public class ValoresPorDefectoTest extends FapModel {
	// Código de los atributos

	public String mString;

	public Long mLong;

	public Integer mInteger;

	public Boolean otroBoolean;

	public Boolean mBoolean;

	public boolean mbooleanNuevo;

	public boolean otrobooleanNuevo;

	public Double mDouble;

	@Column(columnDefinition = "LONGTEXT")
	public String mLongText;

	public String mTelefono;

	@Email
	public String mEmail;

	@CheckWith(CifCheck.class)
	public String mCif;

	@Moneda
	public Double mMoneda;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "mDateTime"), @Column(name = "mDateTimeTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime mDateTime;

	@ValueFromTable("ComboTestList")
	public String mLista;

	/*default = "c"*/

	@Embedded
	public Embebida mEmbebida;

	@Transient
	public String mMoneda_formatFapTabla;

	// Getter del atributo del tipo moneda
	public String getMMoneda_formatFapTabla() {
		return format.FapFormat.format(mMoneda);
	}

	public ValoresPorDefectoTest() {
		init();
	}

	public void init() {

		if (mString == null)
			mString = "string";

		if (mLong == null)
			mLong = 2L;

		if (mInteger == null)
			mInteger = 4;

		if (mBoolean == null)
			mBoolean = true;
		otrobooleanNuevo = true;

		if (mDouble == null)
			mDouble = 2.345;

		if (mLongText == null)
			mLongText = "texto largooooo largiiisimo";

		if (mTelefono == null)
			mTelefono = "900 120 120";

		if (mEmail == null)
			mEmail = "asas@pepe.com";

		if (mCif == null)
			mCif = "A58818501";

		if (mMoneda == null)
			mMoneda = 2.1;

		if (mDateTime == null)
			try {
				mDateTime = new DateTime((new SimpleDateFormat("dd/MM/yyyy")).parse("12/12/2010"));
			} catch (ParseException e) {
				e.printStackTrace();
			}

		if (mEmbebida == null)
			mEmbebida = new Embebida();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
