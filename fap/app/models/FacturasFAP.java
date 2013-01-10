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
import services.GestorDocumentalServiceException;
import utils.BinaryResponse;
import validation.*;
import audit.Auditable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

// === IMPORT REGION START ===
import es.mityc.facturae.FacturaeVersion;
import es.mityc.facturae.utils.UnmarshalException;
import es.mityc.facturae.utils.UnmarshallerUtil;
import es.mityc.facturae32.Facturae;
import es.mityc.facturae32.InvoiceLineType;
import es.mityc.facturae32.InvoiceType;

// === IMPORT REGION END ===

/* -------------------------------------------
 * Factura electrónica
 * -------------------------------------------
 */

@Entity
public class FacturasFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento documento;

	public String identificadorLote;

	@Moneda
	public Double totalPagar;

	@Embedded
	public EmisorReceptor emisor;

	@Embedded
	public EmisorReceptor receptor;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "facturasfap_datosfactura")
	public List<FacturaDatos> datosFactura;

	@Transient
	public String totalPagar_formatFapTabla;

	// Getter del atributo del tipo moneda
	public String getTotalPagar_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalPagar);
	}

	public FacturasFAP() {
		init();
	}

	public void init() {

		if (documento == null)
			documento = new Documento();
		else
			documento.init();

		if (emisor == null)
			emisor = new EmisorReceptor();

		if (receptor == null)
			receptor = new EmisorReceptor();

		if (datosFactura == null)
			datosFactura = new ArrayList<FacturaDatos>();

		postInit();
	}

	// === MANUAL REGION START ===

	public Facturae getFacturaeObject() {
		String uri = documento.uri;
		services.GestorDocumentalService gestorDocumentalService = config.InjectorConfig.getInjector().getInstance(services.GestorDocumentalService.class);
		File factura32 = null;
		
		try {
			factura32 = File.createTempFile("factura", ".xsig");
			BinaryResponse response = gestorDocumentalService.getDocumentoByUri(uri);
			FileOutputStream fichO = new FileOutputStream(factura32);
			fichO.write(response.getBytes());
			fichO.close();
		} catch (GestorDocumentalServiceException e1) {
			Messages.error("No se ha podido obtener el documento.");
			play.Logger.info("No se ha podido obtener el documento.");
			e1.printStackTrace();
		} catch (IOException e) {
			Messages.error("No se ha podido obtener el documento.");
			play.Logger.info("No se ha podido obtener el documento.");
			e.printStackTrace();
		}
		
		Facturae invoice32 = null;
		try {
			UnmarshallerUtil unmarshallerUtil32 = UnmarshallerUtil.getInstance(FacturaeVersion.FACTURAE_32);
			invoice32 = (Facturae) unmarshallerUtil32.unmarshal(factura32);
		} catch (UnmarshalException e) {
			Messages.error("No se ha podido desserializar la factura.");
			play.Logger.info("No se ha podido desserializar la factura.");
			e.printStackTrace();
		}
		
		return invoice32;
	}
	
	public static void getDataFromFacturae(Facturae factura, FacturasFAP dbFacturasFAP) {

		dbFacturasFAP.identificadorLote = factura.getFileHeader().getBatch().getBatchIdentifier();

		if (factura.getFileHeader().getBatch().getTotalOutstandingAmount().getEquivalentInEuros() == null)
			dbFacturasFAP.totalPagar = factura.getFileHeader().getBatch().getTotalOutstandingAmount().getTotalAmount();
		else
			dbFacturasFAP.totalPagar = factura.getFileHeader().getBatch().getTotalOutstandingAmount().getEquivalentInEuros();

		dbFacturasFAP.emisor = EmisorReceptor.getDataFromEmisorReceptor(factura.getParties().getSellerParty().getIndividual(), factura.getParties().getSellerParty().getTaxIdentification());
		// En el caso de que no sea una persona física, poner nombre de la empresa.
		if (dbFacturasFAP.emisor == null) {
			if (factura.getParties().getSellerParty().getLegalEntity() != null) {
				EmisorReceptor emisor = new EmisorReceptor();
				emisor.nombreCompleto = factura.getParties().getSellerParty().getLegalEntity().getCorporateName();
				emisor.identificacionFiscal = factura.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber();
				dbFacturasFAP.emisor = emisor;
			}
		}

		dbFacturasFAP.receptor = EmisorReceptor.getDataFromEmisorReceptor(factura.getParties().getBuyerParty().getIndividual(), factura.getParties().getBuyerParty().getTaxIdentification());
		// En el caso de que no sea una persona física, poner nombre de la empresa.
		if (dbFacturasFAP.receptor == null) {
			if (factura.getParties().getBuyerParty().getLegalEntity() != null) {
				EmisorReceptor receptor = new EmisorReceptor();
				receptor.nombreCompleto = factura.getParties().getBuyerParty().getLegalEntity().getCorporateName();
				receptor.identificacionFiscal = factura.getParties().getBuyerParty().getTaxIdentification().getTaxIdentificationNumber();
				dbFacturasFAP.receptor = receptor;
			}
		}

		List<InvoiceType> inv = factura.getInvoices().getInvoice();
		if (inv != null) {
			List<FacturaDatos> lista = new ArrayList<FacturaDatos>();
			for (int i = 0; i < inv.size(); i++) {
				FacturaDatos f = new FacturaDatos();
				f.numeroFactura = inv.get(i).getInvoiceHeader().getInvoiceNumber();
				f.numeroSerie = inv.get(i).getInvoiceHeader().getInvoiceSeriesCode();
				DateTime aux = new DateTime(inv.get(i).getInvoiceIssueData().getIssueDate().toGregorianCalendar().getTime());
				int dia = aux.getDayOfMonth();
				int mes = aux.getMonthOfYear();
				int agno = aux.getYear();
				f.fechaExpedicion = dia + "/" + mes + "/" + agno;
				f.totalesFactura = TotalesFactura.getDataFromTotalesFactura(inv.get(i).getInvoiceTotals());

				List<InvoiceLineType> lines = inv.get(i).getItems().getInvoiceLine();
				for (int j = 0; j < lines.size(); j++) {
					ItemsFactura item = new ItemsFactura();
					item = ItemsFactura.getDataFromItemFacturas(lines.get(j));
					f.informacionDetallada.add(item);
				}
				lista.add(f);
			}
			dbFacturasFAP.datosFactura = lista;
		}
	}

	// === MANUAL REGION END ===

}
