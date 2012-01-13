package templates

import javax.management.InstanceOfQueryExp;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource
import es.fap.simpleled.led.*;
import generator.utils.HashStack;

class Expand {

	private static boolean inPath(EObject obj){
		if (obj.eResource() == null){
			return true;
		}
		File fileResource = new File(obj.eResource().getURI().toFileString());
		File filePath = new File(wfcomponent.Start.path);
		return fileResource.getAbsolutePath().startsWith(filePath.getAbsolutePath());
	}
	
	public static expand(Object object){
		generator.utils.LedUtils.setFapResources(object);
		
		if (!inPath(object))
			return;
			
		if(object instanceof Entity)
			return GEntidad.generate(object)

		if(object instanceof Formulario)
			return GFormulario.generate(object)

		if(object instanceof Pagina)
			return GPagina.generate(object)
			
		if(object instanceof Popup)
			return GPopup.generate(object)

		if(object instanceof Texto)
			return GTexto.generate(object)

		if(object instanceof Fecha)
			return GFecha.generate(object)
			
		if(object instanceof Combo)
			return GCombo.generate(object)
		
		if(object instanceof Tabla)
			return GTabla.generate(object)
			
		if(object instanceof Lista)
			return GLista.generate(object)
			
		if(object instanceof Grupo)
			return GGrupo.generate(object)
			
		if(object instanceof Menu)
			return GMenu.generate(object)		

		if(object instanceof SubirArchivo)
			return GSubirArchivo.generate(object)
			
		if(object instanceof Nip)
			return GNip.generate(object)
			
		if(object instanceof PersonaFisica)
			return GPersonaFisica.generate(object)
			
		if(object instanceof PersonaJuridica)
			return GPersonaJuridica.generate(object)
		
		if (object instanceof Solicitante)
			return GSolicitante.generate(object)
			
		if(object instanceof Persona)
			return GPersona.generate(object)

		if(object instanceof Direccion)
			return GDireccion.generate(object)
			
		if(object instanceof Boton)
			return GBoton.generate(object)
		
		if(object instanceof Check)
			return GCheck.generate(object)
		
		if(object instanceof Wiki)
			return GWiki.generate(object)	
			
		if(object instanceof EntidadAutomatica)
			return GEntidadAutomatica.generate(object)
		
		if(object instanceof AreaTexto)
			return GAreaTexto.generate(object)

		if(object instanceof Enlace)
			return GEnlace.generate(object)
			
		if(object instanceof AgruparCampos)
			return GAgruparCampos.generate(object)
		
		if(object instanceof Permiso)
			return GPermiso.generate(object)
			
		if(object instanceof Form)
			return GForm.generate(object)
	
		if(object instanceof SubirArchivoAed)
			return GSubirArchivoAed.generate(object)
			
		if(object instanceof EditarArchivoAed)
			return GEditarArchivoAed.generate(object)
		
		if(object instanceof FirmaPlatinoSimple)
			return GFirmaPlatinoSimple.generate(object)
			
		if(object instanceof AgrupaBotones)
			return GAgrupaBotones.generate(object)
			
		return "";
	}
}
