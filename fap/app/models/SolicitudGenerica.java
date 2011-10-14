
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
import controllers.fap.SecureController;
import play.db.jpa.JPABase;
import play.mvc.Http.Request;

// === IMPORT REGION END ===
	

@Auditable
@Entity
@Table(name="solicitud")
public class SolicitudGenerica extends Model {
	// Código de los atributos
	
	
	public String estado;
	
	
	@ValueFromTable("estadosSolicitud")
	@Transient
	public String estadoUsuario;
	
	
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
					
	}
		
	

// === MANUAL REGION START ===

	public static String getEstadoById(Long idSolicitud) {
		Query query = JPA.em().createQuery("select estado from Solicitud s where s.id=" + idSolicitud);
		return (String) query.getSingleResult();
	}

	public String getEstadoUsuario() {
		return estado;
	}

	@Override
	public <T extends JPABase> T save() {
		_save();
		participacionSolicitud();
		return (T) this;
	}

	public void participacionSolicitud() {
		if ((solicitante != null) && (solicitante.tipo != null)) {
			if (solicitante.isPersonaFisica()) {
				compruebaUsuarioParticipacion(solicitante.fisica.nip.valor, solicitante.getNombreCompleto(), solicitante.email);
				if (solicitante.representado) {
					compruebaUsuarioParticipacion(solicitante.representante.getNumeroId(), solicitante.representante.getNombreCompleto(), solicitante.representante.email);
				}	
			}
			else {
				compruebaUsuarioParticipacion(solicitante.juridica.cif, solicitante.getNombreCompleto(), solicitante.email);
				for (RepresentantePersonaJuridica representante: solicitante.representantes){
					compruebaUsuarioParticipacion(representante.getNumeroId(), representante.getNombreCompleto(), representante.email);
				}
				
			}
		}
	}
	
	private void compruebaUsuarioParticipacion(String user, String name, String email){
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
			p.tipo = "solicitante";
			p.save();
			play.Logger.info("Asignada la participación del agente %s en la solicitud %s", agente.username, this.id);
		}	
	}
	
	public boolean documentoEsObligatorio(String uri) {
		return false;
	}

	// === MANUAL REGION END ===
	
	
	}
		