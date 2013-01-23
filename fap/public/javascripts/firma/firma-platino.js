Firma._getCertificados = function(){
		try {			
			initPlatinoWebSigner();

			/*arrCAs = new Array('OU=FNMT Clase 2 CA, O=FNMT, C=ES',
					'CN=AC DNIE 003, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES',
					'CN=AC Camerfirma Certificados Camerales, O=AC Camerfirma SA, SERIALNUMBER=A82743287, L=Madrid (see current address at www.camerfirma.com/address), EMAILADDRESS=ac_camerfirma_cc@camerfirma.com, C=ES',
					'CN=AC RAIZ DNIE, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES',
					'CN=AC Firmaprofesional - CA1, O=Firmaprofesional S.A. NIF A-62634068, OU=Jerarquia de Certificacion Firmaprofesional, OU=Consulte http://www.firmaprofesional.com, L=C/ Muntaner 244 Barcelona, EMAILADDRESS=ca1@firmaprofesional.com, C=ES',
					'CN=AC DNIE 001, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES',
					'CN=ANF Server CA, SERIALNUMBER=G-63287510, OU=ANF Clase 1 CA, O=ANF Autoridad de Certificacion, L=Barcelona (see current address at https://www.anf.es/address/ ), ST=Barcelona, C=ES',
					'CN=AC DNIE 002, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES');

			
			arrRestrictions = new Array(new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'),
					new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'),
					new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'),
					new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'));*/
			
			arrCAs = getArrayCAs();
			arrRestrictions = getArrayRestrictions();
			
			var arrValidCertificates = getTrustedCertificates('PLATINO', 'firma', 'PLATINO');

			var certificados = [];
			for ( var i = 0; i < arrValidCertificates.length; i++) {
				var cert = arrValidCertificates[i];
				certificados.push(new Certificado(cert[0], cert[1]));
			}
			return certificados;
		} catch (err) {
			return null;
		}			
}

Firma._firmarTexto = function(texto, certificado){
	return signPKCS7(certificado.clave, texto);
}

Firma._firmarUrl = function(url, certificado){
	return signFile(certificado.clave, url);
}

Firma._firmarDocumento = function(url, certificado){
	return signFile(certificado.clave, url);
}

var Platino = {
	
	listarCertificados : function(combo, opciones){
		if(opciones == null){
			opciones = {};
		}
		
		var mensajes;
		if(opciones.mensajes != null){
			mensajes = opciones.mensajes;
		}else{
			mensajes = new Mensajes();
		}

		try {			
			initPlatinoWebSigner();
			arrCAs = new Array('OU=FNMT Clase 2 CA, O=FNMT, C=ES',
					'CN=AC DNIE 003, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES',
					'CN=AC Camerfirma Certificados Camerales, O=AC Camerfirma SA, SERIALNUMBER=A82743287, L=Madrid (see current address at www.camerfirma.com/address), EMAILADDRESS=ac_camerfirma_cc@camerfirma.com, C=ES',
					'CN=AC RAIZ DNIE, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES',
					'CN=AC Firmaprofesional - CA1, O=Firmaprofesional S.A. NIF A-62634068, OU=Jerarquia de Certificacion Firmaprofesional, OU=Consulte http://www.firmaprofesional.com, L=C/ Muntaner 244 Barcelona, EMAILADDRESS=ca1@firmaprofesional.com, C=ES',
					'CN=AC DNIE 001, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES',
					'CN=ANF Server CA, SERIALNUMBER=G-63287510, OU=ANF Clase 1 CA, O=ANF Autoridad de Certificacion, L=Barcelona (see current address at https://www.anf.es/address/ ), ST=Barcelona, C=ES',
					'CN=AC DNIE 002, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES');

			
			arrRestrictions = new Array(new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'),
					new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'),
					new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'),
					new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'));
			
			var arrValidCertificates = getTrustedCertificates('PLATINO', 'firma', 'PLATINO');

			var options = '';
			for ( var i = 0; i < arrValidCertificates.length; i++) {
				var cert = arrValidCertificates[i];
				if (cert != null && cert != ""){
					options += '<option value="'+cert[0]+'">'+cert[1]+'</option>';
				}
			}
			$(combo).html(options);
		} catch (err) {
			//mensajes.debug(err);
			mensajes.error("No se ha podido listar los certificados. Error al acceder al servicio electrónico de Platino.");
				
		}		
	},

	/**
	 * certificado: ID del combo de la lista de certificados
	 * texto: ID del campo donde está el texto a firmar
	 * firma: ID del campo donde se almacenará la firma
	 */
	firmarTexto : function(certificado, texto, firma, opciones){		
		if(opciones == null){
			opciones = {};
		}
		var mensajes;
		if(opciones.mensajes != null){
			mensajes = opciones.mensajes;
		}else{
			mensajes = new Mensajes();
		}
		
		var certificadoSeleccionado = $(certificado).val();
		var textoAFirmar = $(texto).val();
		var $firma = $(firma);
		
		var firmapcks7 = signPKCS7(certificadoSeleccionado, textoAFirmar);
		$firma.val(firmapcks7);
		return firmapcks7;
	}

}

/**
* Fichero que trabajo lista los certificados y firma con platino
 * Requisitos:
 * 		error.js - Para mostrar los mensajes de error
 */
function actualizarCertificados() {
	for ( var i = 0; i < arrCertificates.length; i++) {
		if (arrCertificates[i] != null)
			if (arrCertificates[i] != "") {
				document.formData.selCertificate.options[i] = new Option(
						arrCertificates[i][1], arrCertificates[i][0]);
			}
	}
	document.formData.selCertificate.options.lenght = arrCertificates.length;
}


/**
 * 
 * @param elementId ID del combo donde se van a listar los certificados
 * @param options - Hash con opctiones
 * 			idError : Div donde se van a mostrar los mensajes de error
 */
function actualizarCertificadosValidos(elementId, options) {
	if(options == null){
		options = {};
	}
	
	try {
		var selectElement = document.getElementById(elementId);
		if (selectElement == null)
			return;
		
		initPlatinoWebSigner();
		arrCAs = new Array('OU=FNMT Clase 2 CA, O=FNMT, C=ES',
				'CN=AC DNIE 003, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES',
				'CN=AC Camerfirma Certificados Camerales, O=AC Camerfirma SA, SERIALNUMBER=A82743287, L=Madrid (see current address at www.camerfirma.com/address), EMAILADDRESS=ac_camerfirma_cc@camerfirma.com, C=ES',
				'CN=AC RAIZ DNIE, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES',
				'CN=AC Firmaprofesional - CA1, O=Firmaprofesional S.A. NIF A-62634068, OU=Jerarquia de Certificacion Firmaprofesional, OU=Consulte http://www.firmaprofesional.com, L=C/ Muntaner 244 Barcelona, EMAILADDRESS=ca1@firmaprofesional.com, C=ES',
				'CN=AC DNIE 001, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES',
				'CN=ANF Server CA, SERIALNUMBER=G-63287510, OU=ANF Clase 1 CA, O=ANF Autoridad de Certificacion, L=Barcelona (see current address at https://www.anf.es/address/ ), ST=Barcelona, C=ES',
				'CN=AC DNIE 002, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES');

		
		arrRestrictions = new Array(new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'),
				new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'),
				new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'),
				new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'));
		
		var arrValidCertificates = getTrustedCertificates('PLATINO', 'firma', 'PLATINO');
		
		for ( var i = 0; i < arrValidCertificates.length; i++) {
			if (arrValidCertificates[i] != null)
				if (arrValidCertificates[i] != "") {
					selectElement.options[i] = new Option(
							arrValidCertificates[i][1],
							arrValidCertificates[i][0]);
				}
		}
		selectElement.options.lenght = arrValidCertificates.length;
	} catch (err) {
		console.log(err);
		MSGError(options.idError, "No se ha podido listar los certificados. Error al acceder al servicio electrónico de Platino.");
		
	}
}

function limpiarDatos() {
	document.formData.infoCertificate.value = "";
	document.formData.datosfirmados.value = "";
}

function firmarTexto(formulario) {
	var certificado = document.getElementById("validCertificates");
	var texto = document.getElementById(formulario + ":idSession");
	var firma = document.getElementById(formulario + ":firma");
	firma.value = signPKCS7(certificado.value, texto.value);

}

function addEvent(obj, evType, fn) {
	if (obj.addEventListener) {
		obj.addEventListener(evType, fn, false);
		return true;
	} else if (obj.attachEvent) {
		var r = obj.attachEvent("on" + evType, fn);
		return r;
	} else {
		return false;
	}
}

/**
 * El campo options es un hash en el que se puede especificar
 * {
 * 	  idFirma : Id del elemento donde se va a guardar la firma
 * 	  idError : Id del elemento donde se van a escribir los errores, si no se especifica muestra un alert
 * }
 */
function firmarUrl(url, idCampoCerts, options) {

	
	
	if (url == "" || url == null) {
		MSGError(options.idError, "El fichero a firmar no está disponible");
		return null;
	}
	
	try {
		var $certificados = jQuery('#' + idCampoCerts);
		var certificadoSeleccionado = $certificados.val();
		var codigoFirma = signFile(certificadoSeleccionado, url);
				
		if(options.idFirma != null){
			//Se especifico idFirma
			$firma = jQuery('#' + options.idFirma);
			//console.log($firma);
			$firma.val(codigoFirma);
		}
		//console.log(codigoFirma);
		return codigoFirma;
	} catch (error) {
		console.log("Error : " +  error);
		MSGError(options.idError, "No se ha podido firmar la solicitud, el servicio correspondiente no está disponible");
		return null;
	}
}

function firmar(formName, url) {
	console.log('firmando');
	var certificados = document.getElementById('validCertificates');
	var firma = document.getElementById(formName + ':firma');
	console.log('firma' + firma);
	firma.value = signFile(certificados.value, url);
	console.log('valor' + firma.value);
}

function copyCredentials() {
	var user = document.getElementById("usuario");
	var password = document.getElementById("password");

	document.formData.j_username.value = user.value;
	document.formData.j_password.value = password.value;
}

function copyInfoCert() {
	var certificado = document.getElementById("validCertificates");
	var token = document.getElementById("token");

	document.formData.j_username.value = token.value;

	document.formData.j_password.value = signPKCS7(certificado.value,
			token.value);

}

