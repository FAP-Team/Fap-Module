
package templates;

import es.fap.simpleled.led.*
import es.fap.simpleled.led.impl.*
import generator.utils.CampoUtils
import generator.utils.Entidad;
import generator.utils.StringUtils
import generator.utils.TagParameters;

public class GPersona extends GElement{
	
	Persona persona;
	CampoUtils campo;
		
	public GPersona(Persona persona, GElement container){
		super(persona, container);
		this.persona = persona;
		campo = CampoUtils.create(persona.campo);
	}
	
	public void generate(){
		Combo combo = LedFactory.eINSTANCE.createCombo();
		combo.name = "${persona.name}Combo";
		combo.titulo = "Tipo";
		combo.campo = campo.addMore("tipo").campo;
		combo.requerido = persona.requerido;
			
		Grupo fisica = crearPersonaFisica(combo);
		Grupo juridica = crearPersonaJuridica(combo);
		
		if (persona.titulo){
			Grupo grupo = LedFactory.eINSTANCE.createGrupo();
			grupo.titulo = persona.titulo;
			grupo.elementos.add(combo);
			grupo.elementos.add(fisica);
			grupo.elementos.add(juridica);
			getGroupContainer().replaceElement(grupo, persona);
		}
		else{
			getGroupContainer().replaceElement(combo, persona);
			getGroupContainer().addElementAfter(fisica, combo);
			getGroupContainer().addElementAfter(juridica, fisica);
		}
	}
	
	public Grupo crearPersonaFisica(Combo combo){
		PersonaFisica fisica = LedFactory.eINSTANCE.createPersonaFisica();
		fisica.name = "${persona.name}Fisica";
		fisica.campo = CampoUtils.addMore(persona.campo, "fisica").campo;
		fisica.requerido = persona.requerido;
		fisica.noSexo = persona.noSexo;
		
		Grupo grupo = LedFactory.eINSTANCE.createGrupo();
		grupo.borde = "false";
		grupo.siCombo = combo;
		grupo.siComboValues = LedFactory.eINSTANCE.createValues();
		grupo.siComboValues.values.add("fisica");
		grupo.elementos.add(fisica);
		return grupo;
	}
	
	public Grupo crearPersonaJuridica(Combo combo){
		PersonaJuridica juridica = LedFactory.eINSTANCE.createPersonaJuridica();
		juridica.name = "${persona.name}Juridica";
		juridica.campo = CampoUtils.addMore(persona.campo, "juridica").campo;
		juridica.requerido = persona.requerido;
		juridica.permiso = persona.permiso;
		
		Grupo grupo = LedFactory.eINSTANCE.createGrupo();
		grupo.borde = "false";
		grupo.siCombo = combo;
		grupo.siComboValues = LedFactory.eINSTANCE.createValues();
		grupo.siComboValues.values.add("juridica");
		grupo.elementos.add(juridica);
		return grupo;
	}

}
