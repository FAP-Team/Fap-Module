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
import enumerado.fap.gen.EstadosModificacionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.TiposParticipacionEnum;
import play.db.jpa.JPABase;
import play.mvc.Http.Request;
import utils.PeticionModificacion;
import com.google.gson.Gson;

// === IMPORT REGION END ===

@Auditable
@Entity
@Table(name = "solicitud")
public class SolicitudGenerica extends FapModel {
	// Código de los atributos

	public String estado;

	@ValueFromTable("estadosSolicitud")
	@Transient
	public String estadoValue;

	@ValueFromTable("estadosSolicitud")
	@Transient
	public String estadoUsuario;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Solicitante solicitante;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documentacion documentacion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documentacion documentacionProceso;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documentacion documentacionAportada;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documentacion documentacionJustificacion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documentacion documentacionCesion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Registro registro;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ExpedientePlatino expedientePlatino;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ExpedienteAed expedienteAed;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Aportaciones aportaciones;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Justificaciones justificaciones;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Verificacion verificacion;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitudgenerica_verificaciones")
	public List<Verificacion> verificaciones;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Exclusion exclusion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Desistimiento desistimiento;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitudgenerica_ceconomicos")
	public List<CEconomico> ceconomicos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public AceptarRenunciar aceptarRenunciar;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Alegaciones alegaciones;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitudgenerica_notificaciones")
	public List<Notificacion> notificaciones;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public DatosAnotaciones datosAnotaciones;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitudgenerica_autorizacion")
	public List<AutorizacionesFAP> autorizacion;

	public Boolean activoFH;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitudgenerica_certificados")
	public List<CertificadoSolicitado> certificados;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Cesion cesion;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitudgenerica_comunicacionesinternas")
	public List<ComunicacionInterna> comunicacionesInternas;

	@Transient
	public Boolean activoModificacion;

	public String estadoAntesModificacion;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitudgenerica_registromodificacion")
	public List<RegistroModificacion> registroModificacion;

	@Transient
	public String fechaARestaurarStr;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaFinDeAceptacion"), @Column(name = "fechaFinDeAceptacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaFinDeAceptacion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaFinDeAlegacion"), @Column(name = "fechaFinDeAlegacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaFinDeAlegacion;

	public SolicitudGenerica() {
		init();
	}

	public void init() {

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

		if (documentacionJustificacion == null)
			documentacionJustificacion = new Documentacion();
		else
			documentacionJustificacion.init();

		if (documentacionCesion == null)
			documentacionCesion = new Documentacion();
		else
			documentacionCesion.init();

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

		if (justificaciones == null)
			justificaciones = new Justificaciones();
		else
			justificaciones.init();

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

		if (desistimiento == null)
			desistimiento = new Desistimiento();
		else
			desistimiento.init();

		if (ceconomicos == null)
			ceconomicos = new ArrayList<CEconomico>();

		if (aceptarRenunciar == null)
			aceptarRenunciar = new AceptarRenunciar();
		else
			aceptarRenunciar.init();

		if (alegaciones == null)
			alegaciones = new Alegaciones();
		else
			alegaciones.init();

		if (notificaciones == null)
			notificaciones = new ArrayList<Notificacion>();

		if (datosAnotaciones == null)
			datosAnotaciones = new DatosAnotaciones();
		else
			datosAnotaciones.init();

		if (autorizacion == null)
			autorizacion = new ArrayList<AutorizacionesFAP>();

		if (activoFH == null)
			activoFH = false;

		if (certificados == null)
			certificados = new ArrayList<CertificadoSolicitado>();

		if (cesion == null)
			cesion = new Cesion();
		else
			cesion.init();

		if (comunicacionesInternas == null)
			comunicacionesInternas = new ArrayList<ComunicacionInterna>();

		if (registroModificacion == null)
			registroModificacion = new ArrayList<RegistroModificacion>();

		postInit();
	}

	// === MANUAL REGION START ===

	public static String getEstadoById(Long idSolicitud) {
		Query query = JPA.em().createQuery("select estado from Solicitud s where s.id=" + idSolicitud);
		return (String) query.getSingleResult();
	}

	public String getEstadoUsuario() {
		if (estado == null || estado.isEmpty())
			return "";
		VisibilidadEstadoUsuario visibilidadEstado = VisibilidadEstadoUsuario.find("select visibilidad from VisibilidadEstadoUsuario visibilidad where visibilidad.estadoInterno = ?", estado).first();
		if (visibilidadEstado == null) {
			utils.DataBaseUtils.updateEstadosSolicitudUsuario();
			visibilidadEstado = VisibilidadEstadoUsuario.find("select visibilidad from VisibilidadEstadoUsuario visibilidad where visibilidad.estadoInterno = ?", estado).first();
		}
		return visibilidadEstado.estadoUsuario;
	}

	public String getEstadoValue() {
		return estado;
	}

	@Override
	public <T extends JPABase> T save() {
		//merge();
		_save();
		participacionSolicitud();
		return (T) this;
	}

	public void participacionSolicitud() {
		if ((solicitante != null) && (solicitante.tipo != null)) {
			if (solicitante.isPersonaFisica()) {
				compruebaUsuarioParticipacion(solicitante.fisica.nip.valor, solicitante.getNombreCompleto(), solicitante.email);
				if (solicitante.representado) {
					compruebaUsuarioParticipacionRepresentante(solicitante.representante.getNumeroId(), solicitante.representante.getNombreCompleto(), solicitante.representante.email);
				}
			} else {
				compruebaUsuarioParticipacion(solicitante.juridica.cif, solicitante.getNombreCompleto(), solicitante.email);
				for (RepresentantePersonaJuridica representante : solicitante.representantes) {
					compruebaUsuarioParticipacionRepresentante(representante.getNumeroId(), representante.getNombreCompleto(), representante.email);
				}

			}
		}
	}

	private void compruebaUsuarioParticipacion(String user, String name, String email) {
		if (user == null) {
			play.Logger.info("No se comprueba la participación, porque el usuario es: " + user);
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
			p.tipo = TiposParticipacionEnum.solicitante.name();
			p.save();
			play.Logger.info("Asignada la participación del agente %s en la solicitud %s", agente.username, this.id);
		} //end if p == null
	}// end método

	private void compruebaUsuarioParticipacionRepresentante(String user, String name, String email) {
		if (user == null) {
			play.Logger.info("No se comprueba la participación, porque el usuario es: " + user);
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
			if ((this.estado.equals(EstadosSolicitudEnum.modificacion.name())) && (this.registroModificacion.get(this.registroModificacion.size() - 1).enRecuperacion)) {
				Participacion par = Participacion.find("select participacion from Participacion participacion where participacion.tipo=? and participacion.solicitud.id=?", TiposParticipacionEnum.representante.name(), this.id).first();
				if (par != null) {
					par.delete(); //si ya hubiera un representante antes de modificar creo que no serviría
				}
			}
			p = new Participacion();
			p.agente = agente;
			p.solicitud = this;
			p.tipo = TiposParticipacionEnum.representante.name();
			p.save();
			play.Logger.info("Asignada la participación del agente %s en la solicitud %s", agente.username, this.id);

			SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(p.solicitud.id);

			dbSolicitud.save();

			if (p.solicitud.estado.equals(EstadosSolicitudEnum.modificacion.name())) {
				if (EstadosModificacionEnum.enCurso.name().equals(dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size() - 1).estado)) {
					PeticionModificacion peticionModificacionRepresentante = new PeticionModificacion();
					peticionModificacionRepresentante.campoPagina = "participacion";
					Participacion participacion = null;
					try {
						participacion = Participacion.find("select participacion from Participacion participacion where participacion.agente.username=? and participacion.solicitud.id=?", p.agente.username, p.solicitud.id).first();
					} catch (Exception e) {
						play.Logger.info("La execpción es: " + e);
					}

					List<String> valoresAntiguosRepresentante = new ArrayList<String>();
					List<String> valoresNuevosRepresentante = new ArrayList<String>();

					if (participacion.id != null) {
						peticionModificacionRepresentante.idSimples.put("idParticipacion", participacion.id);
						valoresNuevosRepresentante.add(participacion.id.toString());
						peticionModificacionRepresentante.setValorCreado("participacion.id", new ArrayList<String>(), valoresNuevosRepresentante);
					}
					if (!dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size() - 1).enRecuperacion) {
						if (!peticionModificacionRepresentante.isEmpty()) {
							if ((!Messages.hasErrors())) {
								Gson gson = new Gson();
								String jsonPM = gson.toJson(peticionModificacionRepresentante);
								JsonPeticionModificacion jsonPeticionModificacion = new JsonPeticionModificacion();
								jsonPeticionModificacion.jsonPeticion = jsonPM;
								dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size() - 1).jsonPeticionesModificacion.add(jsonPeticionModificacion);
							}
							dbSolicitud.save();
						}
					}
				} //end if
			}// end if
		} //end if p == null
	}

	public boolean documentoEsObligatorio(String uri) {
		return false;
	}

	public List<RepresentantePersonaFisica> getRepresentantes() {
		List<RepresentantePersonaFisica> listaRepresentantes = new ArrayList<RepresentantePersonaFisica>();
		listaRepresentantes.addAll(this.solicitante.representantes);
		listaRepresentantes.add(this.solicitante.representante);
		return listaRepresentantes;
	}

	public List<Interesado> getInteresados() {
		List<Interesado> interesados = new ArrayList<Interesado>();
		interesados.addAll(this.solicitante.getAllInteresados());

		List<Participacion> participaciones = Participacion.find("select participacion from Participacion participacion where participacion.solicitud.id=?", this.id).fetch();

		boolean encontrado = false, tienePersona = false;
		List<Persona> personas = Persona.findAll();
		List<Interesado> interesadosSoloParticipacion = new ArrayList<Interesado>();
		for (Participacion p : participaciones) {
			encontrado = false;
			for (Interesado i : interesados) {
				if (p.agente.name.equals(i.persona.getNumeroId())) {
					encontrado = true;
					break;
				}
			}
			if (!encontrado) {
				tienePersona = false;
				for (Persona persona : personas) {
					if (p.agente.name.equals(persona.getNumeroId())) {
						Interesado nuevoInteresado = new Interesado();
						nuevoInteresado.persona = persona;
						nuevoInteresado.notificar = false;
						nuevoInteresado.email = p.agente.email;
						interesadosSoloParticipacion.add(nuevoInteresado);
						tienePersona = true;
						break;
					}
				}
				if (!tienePersona) {
					play.Logger.error("El solicitante: " + p.agente.name + " tiene un posible Interesado con participacion que no ha sido añadido a la lista de interesados porque no tener asociada una Persona");
				}
			}
		}
		interesados.addAll(interesadosSoloParticipacion);
		return interesados;
	}

	public Boolean getActivoModificacion() {
		if (registroModificacion == null)
			return false;
		if (registroModificacion.isEmpty())
			return false;
		if (("enCurso").equals(registroModificacion.get(registroModificacion.size() - 1).estado))
			return true;
		return false;
	}

	// === MANUAL REGION END ===

}
