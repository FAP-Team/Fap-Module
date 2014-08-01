package models;

import java.util.*;
import javax.persistence.*;

// === IMPORT REGION START ===
import play.db.jpa.JPABase;
import play.exceptions.JPAException;
import properties.FapProperties;
import properties.FapPropertiesKeys;

// === IMPORT REGION END ===

@Entity
public class SemillaExpediente extends Singleton {
	// Código de los atributos
    public Long semilla;

	public Integer anyo;

	public void init() {
		super.init();

		postInit();
	}

	// === MANUAL REGION START ===
    private static final Long VALOR_INICIAL_EXPEDIENTE = (long)1;

    @Override
    public synchronized  <T extends JPABase> T save() {
        super.save(); //para obtener inicialmente el id
		this.anyo = this.anyo != null ? this.anyo : getAnyoActual();
        this.semilla = this.semilla != null ? this.semilla : crearNuevaSemilla();
        return super.save();
    }

    private boolean yaExisteSemilla() {
        return SemillaExpediente.find("bySemilla",this.semilla).first() != null;
    }

    private Long crearNuevaSemilla() {
        Long ultimaSemilla = getValorUltimaSemilla();
        return ultimaSemilla + 1;
    }

    private Long getValorUltimaSemilla() {
        Long valorUltimaSemilla;
        SemillaExpediente semillaAnterior = getUltimaSemilla();
        if(Convocatoria.esAnual()
                && hayQueReiniciarNumeracionCadaAnyo()
                && !this.mismoAnyoQueSemilla(semillaAnterior)) {
            valorUltimaSemilla = semillaInicial().getValorSemilla();
        } else {
            valorUltimaSemilla = semillaAnterior.getValorSemilla();
        }
        return valorUltimaSemilla;
    }

    public SemillaExpediente getUltimaSemilla() {
        SemillaExpediente ultimaSemilla;
        try {
            List<SemillaExpediente> semillas = SemillaExpediente.find("order by id desc limit").fetch();
            ultimaSemilla = semillas.get(1);
        } catch (NullPointerException e) {
            ultimaSemilla = semillaInicial();
        } catch (JPAException e) {
            ultimaSemilla = semillaInicial();
        } catch (IndexOutOfBoundsException e) {
            ultimaSemilla = semillaInicial();
        }
        return ultimaSemilla;
    }

    private SemillaExpediente semillaInicial() {
        SemillaExpediente ultimaSemilla;
        ultimaSemilla = new SemillaExpediente();
        ultimaSemilla.semilla = VALOR_INICIAL_EXPEDIENTE - 1;
        ultimaSemilla.anyo = getAnyoActual();
        return ultimaSemilla;
    }

    public Long getValorSemilla() {
        return (this.semilla != null) ? this.semilla : this.id;
    }

    private boolean mismoAnyoQueSemilla(SemillaExpediente semillaExpediente) {
		return semillaExpediente.anyo.intValue() == this.anyo.intValue();
    }

    private boolean hayQueReiniciarNumeracionCadaAnyo() {
        return FapProperties.getBoolean(FapPropertiesKeys.AED_EXPEDIENTE_PREFIJO_REINICIAR_ANUALMENTE);
    }

    private int getAnyoActual() {
        Calendar now = Calendar.getInstance();
        return now.get(Calendar.YEAR);
    }

	// === MANUAL REGION END ===

}
