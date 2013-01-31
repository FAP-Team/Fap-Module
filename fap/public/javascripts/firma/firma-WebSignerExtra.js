// Mientras el websigner no sea actualizado a 6.3 se utilizará este fichero js
function getArrayCAs () {
	
	return new Array('OU=FNMT Clase 2 CA, O=FNMT, C=ES',
			'CN=AC DNIE 003, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES',
			'CN=AC Camerfirma Certificados Camerales, O=AC Camerfirma SA, SERIALNUMBER=A82743287, L=Madrid (see current address at www.camerfirma.com/address), EMAILADDRESS=ac_camerfirma_cc@camerfirma.com, C=ES',
			'CN=AC RAIZ DNIE, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES',
			'CN=AC Firmaprofesional - CA1, O=Firmaprofesional S.A. NIF A-62634068, OU=Jerarquia de Certificacion Firmaprofesional, OU=Consulte http://www.firmaprofesional.com, L=C/ Muntaner 244 Barcelona, EMAILADDRESS=ca1@firmaprofesional.com, C=ES',
			'CN=AC DNIE 001, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES',
			'CN=ANF Server CA, SERIALNUMBER=G-63287510, OU=ANF Clase 1 CA, O=ANF Autoridad de Certificacion, L=Barcelona (see current address at https://www.anf.es/address/ ), ST=Barcelona, C=ES',
			'CN=AC DNIE 002, OU=DNIE, O=DIRECCION GENERAL DE LA POLICIA, C=ES');
	
}


function getArrayRestrictions () {
	return 			
	new Array(new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'),
			new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'),
			new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'),
			new Array('',''), new Array('Extension|2.5.29.15',',?(nonRepudiation)'));
}