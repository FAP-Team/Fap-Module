Firma._getCertificados = function(){
	var certificados = [];
	certificados.push(new Certificado('11111111H', 'Luke Skywalker (11111111H)'));
	certificados.push(new Certificado('12345678Z', 'Darth Vader (12345678Z)'));
	certificados.push(new Certificado('A99999997', 'Estrella de la Muerte (A99999997)'));
	return certificados;
}

Firma._firmarTexto = function(texto, certificado){
	if(texto == null)
		return null;
	return Base64.encode(addCertInfo(texto, certificado));
}

var addCertInfo = function(data, certificado){
	return certificado.nombre + "#" + certificado.clave + "#" + data;
}

Firma._firmarDocumento = function(url, certificado){
	//Firma la url en vez del contenido del fichero
	alert("Método: _firmarDocumento (firma-fs.js) | Simulación: correcta | url: "+url+" | certificado: "+certificado.clave);
	return Firma._firmarTexto(url, certificado);
	//alert("Método: _firmarDocumento (firma-fs.js) | Simulación: fallo | url: "+url+" | certificado: "+certificado.clave);
	//return null;
}