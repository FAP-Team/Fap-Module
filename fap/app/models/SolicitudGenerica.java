
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
import controllers.fap.SecureController;
import enumerado.fap.gen.TiposParticipacionEnum;
import play.db.jpa.JPABase;
import play.libs.F.Promise;
import play.mvc.Http.Request;
import reports.Report;

// === IMPORT REGION END ===
	


@Entity
@Table(name="solicitud")
public class SolicitudGenerica extends Model {
	// Código de los atributos
	
	
	
	public String estado;
	
	
	
	@ValueFromTable("estadosSolicitud")
	@Transient
	public String estadoValue;
	
	
	
	@ValueFromTable("estadosSolicitud")
	@Transient
	public String estadoUsuario;
	
	
	
	@Transient
	public String borradorRequerimiento;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	
	public Solicitante solicitante;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	
	public Documentacion documentacion;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	
	public Documentacion documentacionProceso;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	
	public Documentacion documentacionAportada;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	
	public Registro registro;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	
	public ExpedientePlatino expedientePlatino;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	
	public ExpedienteAed expedienteAed;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	
	public Aportaciones aportaciones;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	
	public Verificacion verificacion;
	
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="solicitudgenerica_verificaciones")
	
	public List<Verificacion> verificaciones;
	
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	
	public Exclusion exclusion;
	
	
	public SolicitudGenerica (){
		init();
	}
	

	public void init(){
		
		
							if (solicitante == null)
								solicitante = new Solicitante();
							else
								solicitante.init();
						
							if (documentacion == null)
								documentacion = new Documentacion();
							else
								documentacion.init();
						
							if (documentacionProceso == null)
								documentacionProceso = new Documentacion();
							else
								documentacionProceso.init();
						
							if (documentacionAportada == null)
								documentacionAportada = new Documentacion();
							else
								documentacionAportada.init();
						
							if (registro == null)
								registro = new Registro();
							else
								registro.init();
						
							if (expedientePlatino == null)
								expedientePlatino = new ExpedientePlatino();
							else
								expedientePlatino.init();
						
							if (expedienteAed == null)
								expedienteAed = new ExpedienteAed();
							else
								expedienteAed.init();
						
							if (aportaciones == null)
								aportaciones = new Aportaciones();
							else
								aportaciones.init();
						
							if (verificacion == null)
								verificacion = new Verificacion();
							else
								verificacion.init();
						
						if (verificaciones == null)
							verificaciones = new ArrayList<Verificacion>();
						
							if (exclusion == null)
								exclusion = new Exclusion();
							else
								exclusion.init();
						
	}
		
	

// === MANUAL REGION START ===

	public static String getEstadoById(Long idSolicitud) {
		Query query = JPA.em().createQuery("select estado from Solicitud s where s.id=" + idSolicitud);
		return (String) query.getSingleResult();
	}

	public String getEstadoUsuario() {
		VisibilidadEstadoUsuario visEstado = VisibilidadEstadoUsuario.find("select estado from VisibilidadEstadoUsuario estado where estado.estadoInterno=?", estado).first();
		if (visEstado == null) {
			utils.DataBaseUtils.updateEstadosSolicitudUsuario();
			visEstado = VisibilidadEstadoUsuario.find("select estado from VisibilidadEstadoUsuario estado where estado.estadoInterno=?", estado).first();
		}
		return visEstado.estadoUsuario;
	}
	
	public String getEstadoValue() {
		return estado;
	}

	@Override
	public <T extends JPABase> T save() {
		//merge();
		_save();
		participacionesSolicitud();
		return (T) this;
	}

	public void participacionSolicitud() {
		if ((solicitante != null) && (solicitante.tipo != null)) {
			if (solicitante.isPersonaFisica()) {
				compruebaUsuarioParticipacionSolicitante(solicitante.fisica.nip.valor, solicitante.getNombreCompleto(), solicitante.email);
				if (solicitante.representado) {
					compruebaUsuarioParticipacionRepresentante(solicitante.representante.getNumeroId(), solicitante.representante.getNombreCompleto(), solicitante.representante.email);
				}
			}
			else {
				compruebaUsuarioParticipacionSolicitante(solicitante.juridica.cif, solicitante.getNombreCompleto(), solicitante.email);
				for (RepresentantePersonaJuridica representante: solicitante.representantes){
					compruebaUsuarioParticipacionRepresentante(representante.getNumeroId(), representante.getNombreCompleto(), representante.email);
				}
				
			}
		}
	}
	
	public void participacionesSolicitud () {
		if ((solicitante != null) && (solicitante.tipo != null)) {
			// Participación del Solicitante
			Participacion p = Participacion.find("select participacion from Participacion participacion where participacion.solicitud.id=? and participacion.tipo='solicitante'", this.id).first();
			if ((p != null) && (solicitante != null) && solicitante.getNumeroId() != null && solicitante.getNumeroId().equals(p.agente.username)) {
				play.Logger.info("El Solicitante <"+solicitante.getNumeroId()+"> ya tiene participación en la solicitud "+this.id);
			} else {
				if (p != null) {
					play.Logger.info("Eliminamos la participación "+p.agente.username+" de la solicitud "+p.solicitud.id);
					p.delete();
				}
				compruebaUsuarioParticipacionSolicitante(solicitante.getNumeroId(), solicitante.getNombreCompleto(), solicitante.email);
			}
			// Participaciones de los representantes
			List<Participacion> listP = Participacion.find("select participacion from Participacion participacion where participacion.solicitud.id=? and participacion.tipo='representante'", this.id).fetch();
			boolean _yaExiste = false;
			if (solicitante.isPersonaFisica()) {
				for (Participacion part: listP) {
					if (solicitante.representado && (part != null) && solicitante.representante.getNumeroId().equals(part.agente.username)) {
						play.Logger.info("El representante <"+solicitante.representante.getNumeroId()+"> ya tiene participación en la solicitud "+this.id);
						_yaExiste = true;
					} else {
						if (part != null) {
							play.Logger.info("Eliminamos la participación "+part.agente.username+" de la solicitud "+part.solicitud.id);
							part.delete();
						}
					}
					if (!_yaExiste && solicitante.representado) {
						compruebaUsuarioParticipacionRepresentante(solicitante.representante.getNumeroId(), solicitante.representante.getNombreCompleto(), solicitante.representante.email);
					}
				}
			}
			
			if (solicitante.isPersonaJuridica()) {
				// Eliminamos las participaciones que sobran
				for (Participacion part: listP) {
					boolean deletePart = true;
					for (RepresentantePersonaJuridica rep : solicitante.representantes) {
						if (rep.getNumeroId().equals(part.agente.username)) {
							play.Logger.info("El representante <"+rep.getNumeroId()+"> ya tiene participación en la solicitud "+this.id);
							deletePart = false;
						}
					}
					if (deletePart) {
						play.Logger.info("Eliminamos la participación "+part.agente.username+" de la solicitud "+part.solicitud.id);
						part.delete();
					}
				}
				// Añadimos las participaciones de los representantes que faltan
				for (RepresentantePersonaJuridica rep : solicitante.representantes) {
					boolean createPart = true;
					for (Participacion part: listP) {
						if (rep.numeroId.equals(part.agente.username)) {
							createPart = false;
						}
					}
					if (createPart) {
						compruebaUsuarioParticipacionRepresentante(rep.getNumeroId(), rep.getNombreCompleto(), rep.email);
					}
				}
			}
			
		}
	}
	
	private void compruebaUsuarioParticipacionSolicitante (String user, String name, String email) {
		compruebaUsuarioParticipacion(user, name, email, "solicitante");
	}
	
	private void compruebaUsuarioParticipacionRepresentante (String user, String name, String email) {
		compruebaUsuarioParticipacion(user, name, email, "representante");
	}
	
	private void compruebaUsuarioParticipacion(String user, String name, String email, String tipo){
		if (user == null) {
			play.Logger.info("No se comprueba la participación, porque el usuario es: "+user);
			return;
		}
		Participacion p = Participacion.find("select participacion from Participacion participacion where participacion.agente.username=? and participacion.solicitud.id=?", user, this.id).first();
		if (p == null) {
			Agente agente = Agente.find("select agente from Agente agente where agente.username=?", user).first();

			if (agente == null) {
				agente = new Agente();
				agente.username = user;
				agente.name = name;
				agente.email = email;
				agente.roles = new HashSet<String>();
				agente.roles.add("usuario");
				agente.rolActivo = "usuario";
				agente.save();
				play.Logger.info("Creado el agente %s", user);
			}

			p = new Participacion();
			p.agente = agente;
			p.solicitud = this;
			p.tipo = tipo;
			p.save();
			play.Logger.info("Asignada la participación del agente %s en la solicitud %s", agente.username, this.id);
		}	
	}
	
	private void clearUsuarioParticipacion(String user, String name, String email){
		if (user == null) {
			play.Logger.info("No se comprueba la participación, porque el usuario es: "+user);
			return;
		}
		Participacion p = Participacion.find("select participacion from Participacion participacion where participacion.agente.username=? and participacion.solicitud.id=?", user, this.id).first();
		if (p != null) {
			p.delete();
			play.Logger.info("Eliminada la participación del agente %s en la solicitud %s", user, this.id);
		}	
	}
	
	public boolean documentoEsObligatorio(String uri) {
		return false;
	}

	// === MANUAL REGION END ===
	
	
	}
		