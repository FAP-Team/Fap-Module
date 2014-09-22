package properties;

public class FapPropertiesKeys {

    public static final String RESOLUCIONES_USUARIO="fap.resoluciones.usuario";
    public static final String RESOLUCIONES_ID_AREAFUNCIONAL="fap.resoluciones.idAreaFuncional";
    public static final String RESOLUCIONES_URL="fap.resoluciones.url";
    public static final String RESOLUCIONES_GENERAR_DOCUMENTO_RESOLUCION="fap.resoluciones.generarDocumentoResolucion";
    public static final String RESOLUCIONES_PUBLICAR_TABLON_ANUNCIOS="fap.resoluciones.publicarTablonAnuncios";
    public static final String RESOLUCIONES_NOTIFICAR="fap.resoluciones.notificar";
    public static final String RESOLUCIONES_DESCRIPCION_NOTIFICACION="fap.resoluciones.descripcionNotificacion";
    public static final String RESOLUCIONES_APORTACION_TRAMITE="fap.resoluciones.aportacion.tramite";
    public static final String AED_TIPOSDOCUMENTOS_RESOLUCION_PROVISIONAL="fap.aed.tiposdocumentos.resolucion.provisional";
    public static final String AED_TIPOSDOCUMENTOS_RESOLUCION_DEFINITIVA="fap.aed.tiposdocumentos.resolucion.definitiva";
    public static final String AED_TIPOSDOCUMENTOS_EVALUACION="fap.aed.tiposdocumentos.evaluacion";
    public static final String AED_TIPOSDOCUMENTOS_EVALUACION_COMPLETA="fap.aed.tiposdocumentos.evaluacion.completa";
    public static final String AED_TIPOSDOCUMENTOS_EVALUACION_COMPLETA_CONCOMENTARIOS="fap.aed.tiposdocumentos.evaluacion.completa.concomentarios";
    public static final String AED_TIPOSDOCUMENTOS_EVALUACION_OFICIONREMISION = "fap.aed.tiposdocumentos.evaluacion.oficioRemision";


    public static final String RESOLUCION_BAREMACION_PERMITIDA="fap.resolucion.baremacion.permitida";

    public static final String GESTORDOCUMENTAL_TIPOSFACTURAS_URL="fap.gestordocumental.tiposfacturas.url";

    public static final String DOCCONSULTA_PORTAFIRMA_INTERESADO_NOMBRE="fap.docConsulta.portaFirma.interesado.nombre";
    public static final String DOCCONSULTA_PORTAFIRMA_INTERESADO_CIF="fap.docConsulta.portaFirma.interesado.cif";


    //Configuración de borrado de los PDF temporales
    //Se activa el borrado temporal cada cierto tiempo automáticamente
    public static final String DELETE_TEMPORALS="fap.delete.temporals";
    // Valor que indica el tiempo de antiguedad de un pdf temporal para borrarse
    // Xd (borrar pdf temporales creados hace X días o más), siendo X válido de 1 a 2 digitos
    // Xh (borrar pdf temporales creados hace X horas o más), siendo X válido de 1 a 2 digitos
    // Xm (borrar pdf temporales creados hace X minutos o más), siendo X válido de 1 a 2 digitos
    public static final String DELETE_TEMPORALS_OLD="fap.delete.temporals.old";

    // En horas
    public static final String DELETE_TEMPORALS_EVERY="fap.delete.temporals.every";
    // En horas
    public static final String LOG_COMPRESS_EVERY="fap.log.compress.every";
    // Copia extra de los logs
    public static final String LOG_COPY_EXTRA="fap.log.copy.extra";

    //Nombre de la aplicacion
    public static final String APP_NAME="fap.app.name";

    // Número de decimales de los Doubles y Monedas
    // public static final String format_double="fap.format.double";
    public static final String FORMAT_DOUBLE_MAX="fap.format.double.max";
    public static final String FORMAT_DOUBLE_MIN="fap.format.double.min";
    public static final String FORMAT_MONEDA_MAX="fap.format.moneda.max";
    public static final String FORMAT_MONEDA_MIN="fap.format.moneda.min";

    // Tamaño maximo permitido de los ficheros a subir
    public static final String FILE_MAXSIZE="fap.file.maxsize";

    // Tipo de login
    public static final String LOGIN_TYPE_CERT="fap.login.type.cert";
    public static final String LOGIN_TYPE_USER="fap.login.type.user";
    
    // Login mediante ticketing
    public static final String LOGIN_TYPE_TICKETING="fap.login.type.ticketing";
    
    // Asunto acordado entre la aplicación y la sede para recibir el ticket
    public static final String LOGIN_TICKETING_SEDE_ASUNTO="fap.login.ticketing.sede.asunto";
    
    // URL del sistema de ticketing
    public static final String LOGIN_TICKETING_URL="fap.login.ticketing.url";
    public static final String LOGIN_MOCK_TICKETING_URL="fap.login.mock.ticketing.url";


    // Tamaño máximo y mínimo de las contraseñas
    public static final String PASSWORD_MIN="fap.password.min";
    public static final String PASSWORD_MAX="fap.password.max";

    //TableKeyValue - Table de Tablas
    public static final String TABLEKEYVALUE_CACHE="fap.tablekeyvalue.cache";
    public static final String PROD_TABLEKEYVALUE_CACHE="%prod.fap.tablekeyvalue.cache";
    
    // Start
    public static final String START_INITSOLICITUD="fap.start.initSolicitud";
    public static final String PROD_START_INITSOLICITUD="%prod.fap.start.initSolicitud";

    // Permite realizar el auto-test sin errores, ya que aparecen errores con el HtmlUnit y javascript
    // Automatic autotest browser
    public static final String TEST_AUTOMATIC_AUTOTEST="%test.fap.automatic.autotest";

    // Deshabilita chosen para los tests
    public static final String UNABLE_CHOSEN="fap.unable.chosen";

    public static final String TEST_UNABLE_CHOSEN="%test.fap.unable.chosen";
    
    // Según esté configurado el Apache
    public static final String PROXY_PRESERVE_HOST="fap.proxy.preserve.host";
    
    // Aparece un formulario de búsqueda en la pantalla inicial para todos los roles (excepto Usuario)
    public static final String INDEX_SEARCH="fap.index.search";

    public static final String CACHE="fap.cache";

    public static final String CACHE_TIME="fap.cache.time";

    // Para habilitar la tabla de documentacion externa
    public static final String DOCUMENTACION_DOCUMENTOSEXTERNOS="fap.documentacion.documentosExternos";


    // Indica si se envían los campos ocultos en las páginas, los que son ocultos por grupos
    public static final String FORM_SEND_HIDDEN_FIELDS="fap.form.sendHiddenFields";


    // Elimina los logs antiguos de texto plano (siguen almacenándose los .zip)
    public static final String DELETELOGS_TEXTOPLANO="fap.deleteLogs.textoPlano";


    // Documentos dependiendo del trámite
    public static final String GESTORDOCUMENTAL_DOCUMENTACION_TRAMITE="fap.gestordocumental.documentacion.tramite";


    // -------------------------------------------------------------------------------------------------------------
    // AED2
    // -------------------------------------------------------------------------------------------------------------

    // Aed por defecto que se va a utilizar, así permitimos configurar más AEDs (fap.aed1.url, fap.aed2.url)
    public static final String DEFAULT_AED="fap.defaultAED";

    //End-point del archivo electrónico
    public static final String AED_URL="fap.aed.url";

    //Procedimiento
    public static final String AED_PROCEDIMIENTO="fap.aed.procedimiento";

    //Ruta donde se van a almacenar los archivo temporales, esta ruta debe estar creada en el aed
    public static final String AED_TEMPORALES="fap.aed.temporales";

    //Convocatoria
    public static final String AED_CONVOCATORIA="fap.aed.convocatoria";

    //Prefijo que va a llevar el nombre del expediente en el AED
    public static final String AED_EXPEDIENTE_PREFIJO="fap.aed.expediente.prefijo";
    public static final String AED_EXPEDIENTE_PREFIJO_REINICIAR_ANUALMENTE="fap.aed.expediente.prefijo.reiniciarAnualmente";

    //Nombre del expediente en el AED de la convocatoria
    public static final String AED_EXPEDIENTE_CONVOCATORIA="fap.aed.expediente.convocatoria";
    public static final String AED_EXPEDIENTE_MODALIDAD="fap.aed.expediente.modalidad";
    public static final String AED_EXPEDIENTE_CONVOCATORIA_INTERESADO_NOMBRE="fap.aed.expediente.convocatoria.interesado.nombre";
    public static final String AED_EXPEDIENTE_CONVOCATORIA_INTERESADO_NIP="fap.aed.expediente.convocatoria.interesado.nip";

    //End-point del servicio de tipos de documentos
    public static final String AED_TIPOSDOCUMENTOS_URL="fap.aed.tiposdocumentos.url";

    //End-point del servicio de tipos de documentos
    public static final String AED_PROCEDIMIENTOS_URL="fap.aed.procedimientos.url";

    //Uri del tipo de documento base
    public static final String AED_TIPOSDOCUMENTOS_BASE="fap.aed.tiposdocumentos.base";

    //Uri del tramite del que se obtendran los tipos de documentos
    public static final String AED_PROCEDIMIENTOS_TRAMITE_URI="fap.aed.procedimientos.tramite.uri";

    //Uri del tramite del que se obtendran los tipos de documentos
    public static final String AED_PROCEDIMIENTOS_PROCEDIMIENTO_URI="fap.aed.procedimientos.procedimiento.uri";

    //Uri del tipo de solicitud
    public static final String AED_TIPOSDOCUMENTOS_SOLICITUD="fap.aed.tiposdocumentos.solicitud";
    //Uri del tipo de solicitud de modificación
    public static final String AED_TIPOSDOCUMENTOS_SOLICITUD_MODIFICACION="fap.aed.tiposdocumentos.solicitud.modificacion";
    //Uri del tipo de justificante de registro de modificación
    public static final String AED_TIPOSDOCUMENTOS_JUSTIFICANTE_REGISTROSOLICITUD_MODIFICACION="fap.aed.tiposdocumentos.justificanteRegistroSolicitudModificacion";

    //Prefijo PDF Justificante
    public static final String TRAMITACION_PREFIJO_JUSTIFICANTEPDF_SOLICITUD="fap.tramitacion.prefijojustificantepdf.solicitud";
    public static final String TRAMITACION_PREFIJO_JUSTIFICANTEPDF_SOLICITUD_MODIFICACION="fap.tramitacion.prefijojustificantepdf.solicitudModificacion";

    // Cuando se sube un documento, puede que todavía el usuario no haya rellenado el solicitante
    // se le pone un interesado por defecto, cuando se registre la solicitud y se clasifique el documento
    // se estable bien el interesado
    public static final String AED_DOCUMENTONOCLASIFICADO_INTERESADO_NOMBRE="fap.aed.documentonoclasificado.interesado.nombre";
    public static final String AED_DOCUMENTONOCLASIFICADO_INTERESADO_NIF="fap.aed.documentonoclasificado.interesado.nif";

    //Documentos para la aportacion
    public static final String AED_TIPOSDOCUMENTOS_APORTACION_SOLICITUD="fap.aed.tiposdocumentos.aportacion.solicitud";
    public static final String AED_TIPOSDOCUMENTOS_APORTACION_REGISTRO="fap.aed.tiposdocumentos.aportacion.registro";

    //Documentos para la justificacion
    public static final String AED_TIPOSDOCUMENTOS_JUSTIFICACION_SOLICITUD="fap.aed.tiposdocumentos.justificacion.solicitud";
    public static final String AED_TIPOSDOCUMENTOS_JUSTIFICACION_REGISTRO="fap.aed.tiposdocumentos.justificacion.registro";

    //Documentos para el Desistimiento
    public static final String AED_TIPOSDOCUMENTOS_TIPO_TRAMITE_DESISTIMIENTO="fap.aed.tiposdocumentos.tipo.tramite.desistimiento";
    public static final String AED_TIPOSDOCUMENTOS_JUSTIFICANTE_REGISTROSOLICITUD="fap.aed.tiposdocumentos.justificanteRegistroSolicitud";
    public static final String TRAMITACION_DESISTIMIENTO_FECHAFIN="fap.tramitacion.desistimiento.fechafin";

    // -------------------------------------------------------------------------------------------------------------
    // PLATINO
    // -------------------------------------------------------------------------------------------------------------
    // Seguridad
    public static final String PLATINO_SECURITY_BACKOFFICE_URI="fap.platino.security.backoffice.uri";
    public static final String PLATINO_SECURITY_CERTIFICADO_ALIAS="fap.platino.security.certificado.alias";


    // Proxy
    public static final String PROXY_ENABLE="fap.proxy.enable";
    public static final String PROXY_SERVER="fap.proxy.server";
    public static final String PROXY_PORT="fap.proxy.port";

    // Firma
    //------------------------------------------------------------------------------
    // Dirección de los javascripts que se van a utilizar.Valores: [pre, pro]
    // por defecto se utiliza pro
    public static final String PLATINO_FIRMA_JS="fap.platino.firma.js";
    public static final String PLATINO_FIRMA_URL="fap.platino.firma.url";
    public static final String PLATINO_FIRMA_INVOKINGAPP="fap.platino.firma.invokingApp";
    public static final String PLATINO_FIRMA_ALIAS="fap.platino.firma.alias";
    public static final String PLATINO_WEBSIGNER63="fap.platino.websigner63";

    // Portafirmas
    public static final String PLATINO_PORTAFIRMA_URL="fap.platino.portafirma.url";
    // Indica si se usa el portafirma de Platino
    public static final String PLATINO_PORTAFIRMA="fap.platino.portafirma";
    // Especifica los usuarios destinatarios. Añada parejas id usuario destinatario y nombre usuario destinatario. Separado por comas (no dejar espacios antes y después de una coma).
    public static final String PLATINO_PORTAFIRMA_DESTINATARIOS="fap.platino.portafirma.destinatarios";

    // GestorDocumental
    public static final String PLATINO_GESTORDOCUMENTAL_URL="fap.platino.gestordocumental.url";
    public static final String PLATINO_GESTORDOCUMENTAL_EXPEDIENTE_DESCRIPCION="fap.platino.gestordocumental.expediente.descripcion";
    public static final String PLATINO_GESTORDOCUMENTAL_PROCEDIMIENTO="fap.platino.gestordocumental.procedimiento";

    // Registro de entrada y salida
    public static final String PLATINO_REGISTRO_URL="fap.platino.registro.url";
    public static final String PLATINO_REGISTRO_USERNAME="fap.platino.registro.username";
    public static final String PLATINO_REGISTRO_PASSWORD="fap.platino.registro.password";
    public static final String PLATINO_REGISTRO_ASUNTO="fap.platino.registro.asunto";
    public static final String PLATINO_REGISTRO_UNIDADORGANICA="fap.platino.registro.unidadOrganica";
    public static final String PLATINO_REGISTRO_ALIASSERVIDOR="fap.platino.registro.aliasServidor";

    // Mensajes
    public static final String PLATINO_MENSAJES_URL="fap.platino.mensajes.url";


    // Verificación de Datos (SVD)


    public static final String PLATINO_SVD_URL="fap.platino.svd.url";


    // BDD De Procedimientos
    public static final String PLATINO_PROCEDIMIENTOS_URL="fap.platino.procedimientos.url";

    // BDD De Organizacion
    public static final String PLATINO_ORGANIZACION_URL="fap.platino.organizacion.url";

    //Comunicaciones Internas
    public static final String SERVICES_COMUNICACIONES_INTERNAS_URL="fap.services.comunicaciones.internas.url";
    public static final String SERVICES_GENERICOS_COMUNICACIONES_INTERNAS_URL="fap.services.genericos.comunicaciones.internas.url";
    public static final String SERVICES_ENTRADA_COMUNICACIONES_INTERNAS_URL="fap.services.entrada.comunicaciones.internas.url";
    public static final String SERVICES_SALIDA_COMUNICACIONES_INTERNAS_URL="fap.services.salida.comunicaciones.internas.url";

    // BDD De Terceros
    //---------------------------------------------------------------------------------
    public static final String PLATINO_TERCEROS_URL="fap.platino.terceros.url";
    // Para activar o no el servicio de BDD de Terceros de Platino
    public static final String PLATINO_TERCERO_ACTIVA="fap.platino.tercero.activa";
    // Localizaciones
    public static final String PLATINO_LOCALIZACIONES_URL="fap.platino.localizaciones.url";

    // Agencia firmante (firma en el servidor)
    public static final String PLATINO_FIRMANTE_NOMBRE="fap.platino.firmante.nombre";
    public static final String PLATINO_FIRMANTE_DOCUMENTO="fap.platino.firmante.documento";


    public static final String SERVICIOS_HTTPTIMEOUT="fap.servicios.httpTimeout";

    //---------------------------------------------------------
    // Verificación
    //---------------------------------------------------------
    public static final String VERIFICACION_LOCATIONURI="fap.verificacion.locationURI";
    public static final String VERIFICACION_PROCEDIMIENTOURI="fap.verificacion.procedimientoURI";
    public static final String VERIFICACION_TRAMITEURI="fap.verificacion.tramiteURI";


    //---------------------------------------------------------
    // FileSystem Gestor Documental
    //---------------------------------------------------------
    public static final String FS_GESTORDOCUMENTAL_PATH="fap.fs.gestorDocumental.path";

    // -------------------------------------------------------------------------------------------------------------
    // Configuración de los trámites Aceptación/Renuncia/Alegaciones/Reformulación/Desistimiento
    // -------------------------------------------------------------------------------------------------------------
    // Uri del tipo de solicitud
    public static final String AED_TIPOSDOCUMENTOS_RENUNCIA="fap.aed.tiposdocumentos.renuncia";
    public static final String AED_TIPOSDOCUMENTOS_ACEPTACIONRENUNCIA_ACEPTACION="fap.aed.tiposdocumentos.aceptacionrenuncia.aceptacion";
    public static final String AED_TIPOSDOCUMENTOS_ACEPTACIONRENUNCIA_RENUNCIA="fap.aed.tiposdocumentos.aceptacionrenuncia.renuncia";
    public static final String AED_TIPOSDOCUMENTOS_ALEGACION="fap.aed.tiposdocumentos.alegacion";
    public static final String AED_TIPOSDOCUMENTOS_REFORMULACION="fap.aed.tiposdocumentos.reformulacion";
    public static final String AED_TIPOSDOCUMENTOS_DESISTIMIENTO="fap.aed.tiposdocumentos.desistimiento";

    // Uri del tipo de justificante de registro
    public static final String AED_TIPOSDOCUMENTOS_JUSTIFICANTE_REGISTROACEPTACIONRENUNCIA_ACEPTACION="fap.aed.tiposdocumentos.justificanteRegistroAceptacionRenuncia.aceptacion";
    public static final String AED_TIPOSDOCUMENTOS_JUSTIFICANTE_REGISTROACEPTACIONRENUNCIA_RENUNCIA="fap.aed.tiposdocumentos.justificanteRegistroAceptacionRenuncia.renuncia";
    public static final String AED_TIPOSDOCUMENTOS_JUSTIFICANTE_REGISTRORENUNCIA="fap.aed.tiposdocumentos.justificanteRegistroRenuncia";
    public static final String AED_TIPOSDOCUMENTOS_JUSTIFICANTE_REGISTROALEGACION="fap.aed.tiposdocumentos.justificanteRegistroAlegacion";
    public static final String AED_TIPOSDOCUMENTOS_JUSTIFICANTE_REGISTROREFORMULACION="fap.aed.tiposdocumentos.justificanteRegistroReformulacion";
    public static final String AED_TIPOSDOCUMENTOS_JUSTIFICANTE_REGISTRODESISTIMIENTO="fap.aed.tiposdocumentos.justificanteRegistroDesistimiento";

    // Plantilla para los nombre los ficheros de justificantes
    public static final String TRAMITACION_ACEPTACION_PREFIJO_JUSTIFICANTEPDF="fap.tramitacion.aceptacion.prefijojustificantepdf";
    public static final String TRAMITACION_RENUNCIA_PREFIJO_JUSTIFICANTEPDF="fap.tramitacion.renuncia.prefijojustificantepdf";
    public static final String TRAMITACION_REFORMULACION_PREFIJO_JUSTIFICANTEPDF="fap.tramitacion.reformulacion.prefijojustificantepdf";
    public static final String TRAMITACION_ALEGACION_PREFIJO_JUSTIFICANTEPDF="fap.tramitacion.alegacion.prefijojustificantepdf";
    public static final String TRAMITACION_DESISTIMIENTO_PREFIJO_JUSTIFICANTEPDF="fap.tramitacion.desistimiento.prefijojustificantepdf";

    // Identificadores para los correos
    public static final String TRAMITACION_ACEPTACION_IDENTIFICADOREMAIL="fap.tramitacion.aceptacion.identificadoremail";
    public static final String TRAMITACION_RENUNCIA_IDENTIFICADOREMAIL="fap.tramitacion.renuncia.identificadoremail";
    public static final String TRAMITACION_REFORMULACION_IDENTIFICADOREMAIL="fap.tramitacion.reformulacion.identificadoremail";
    public static final String TRAMITACION_ALEGACION_IDENTIFICADOREMAIL="fap.tramitacion.alegacion.identificadoremail";
    public static final String TRAMITACION_DESISTIMIENTO_IDENTIFICADOREMAIL="fap.tramitacion.desistimiento.identificadoremail";
    public static final String TRAMITACION_APORTACION_IDENTIFICADOREMAIL="fap.tramitacion.aportacion.identificadoremail";
    public static final String TRAMITACION_JUSTIFICACION_IDENTIFICADOREMAIL="fap.tramitacion.justificacion.identificadoremail";
    public static final String TRAMITACION_INICIADA_IDENTIFICADOREMAIL="fap.tramitacion.iniciada.identificadoremail";
    public static final String TRAMITACION_MODIFICADA_IDENTIFICADOREMAIL="fap.tramitacion.modificada.identificadoremail";
    // Mails Seguimiento / Fin de Presentacion
    public static final String SEGUIMIENTO_ALERTA_IDENTIFICADOREMAIL="fap.seguimiento.alerta.identificadoremail";

    public static final String CONVOCATORIA_FINPRESENTACIONSOLICITUD_IDENTIFICADOREMAIL="fap.convocatoria.finPresentacionSolicitud.identificadoremail";

    // requerimientos
    public static final String AED_TIPOSDOCUMENTOS_REQUERIMIENTO="fap.aed.tiposdocumentos.requerimiento";
    public static final String AED_TIPOSDOCUMENTOS_JUSTIFICANTEREGISTROSALIDA="fap.aed.tiposdocumentos.justificanteRegistroSalida";


    // Documentos de Cesion y respuesta de datos.
    public static final String AED_TIPOSDOCUMENTOS_PETICIONBASE="fap.aed.tiposdocumentos.peticionBase";
    public static final String AED_TIPOSDOCUMENTOS_RESPUESTABASE="fap.aed.tiposdocumentos.respuestaBase";

    public static final String AED_TIPOSDOCUMENTOS_PETICIONINSSR001="fap.aed.tiposdocumentos.peticionINSSR001";
    public static final String AED_TIPOSDOCUMENTOS_RESPUESTAINSSR001="fap.aed.tiposdocumentos.respuestaINSSR001";

    public static final String AED_TIPOSDOCUMENTOS_PETICIONINSSA008="fap.aed.tiposdocumentos.peticionINSSA008";
    public static final String AED_TIPOSDOCUMENTOS_RESPUESTAINSSA008="fap.aed.tiposdocumentos.respuestaINSSA008";

    public static final String AED_TIPOSDOCUMENTOS_PETICIONATC="fap.aed.tiposdocumentos.peticionATC";
    public static final String AED_TIPOSDOCUMENTOS_RESPUESTAATC="fap.aed.tiposdocumentos.respuestaATC";

    public static final String AED_TIPOSDOCUMENTOS_PETICIONAEAT="fap.aed.tiposdocumentos.peticionAEAT";
    public static final String AED_TIPOSDOCUMENTOS_RESPUESTAAEAT="fap.aed.tiposdocumentos.respuestaAEAT";

    //Prefijo que se empleará en el nombre del fichero de la peticion de cesion de datos (provincia)
    public static final String PREFIJO_PETICION_PROVINCIA="fap.prefijo.peticion.provincia";
    //Aceptacion/Renuncia
    public static final String AED_PROCEDIMIENTOS_TRAMITEACEPTACIONRENUNCIA_ACEPTACION_NOMBRE="fap.aed.procedimientos.tramiteaceptacionrenuncia.aceptacion.nombre";
    public static final String AED_PROCEDIMIENTOS_TRAMITEACEPTACIONRENUNCIA_RENUNCIA_NOMBRE="fap.aed.procedimientos.tramiteaceptacionrenuncia.renuncia.nombre";


    // -----------------------------------------------------------------------------------------
    // Configuración de Notificaciones
    // -----------------------------------------------------------------------------------------

    public static final String NOTIFICACION_ACTIVA="fap.notificacion.activa";

    public static final String NOTIFICACION_PROCEDIMIENTO="fap.notificacion.procedimiento";
    public static final String NOTIFICACION_BACKOFFICE="fap.notificacion.backoffice";
    public static final String NOTIFICACIONES_URL="fap.notificaciones.url";
    public static final String AED_NOTIFICACION_TIPODOCUMENTO_ANULACION="fap.aed.notificacion.tipodocumento.anulacion";
    public static final String AED_NOTIFICACION_TIPODOCUMENTO_PUESTAADISPOSICION="fap.aed.notificacion.tipodocumento.puestaadisposicion";
    public static final String AED_NOTIFICACION_TIPODOCUMENTO_MARCARARESPONDIDA="fap.aed.notificacion.tipodocumento.marcararespondida";
    public static final String AED_NOTIFICACION_TIPODOCUMENTO_ACUSERECIBO="fap.aed.notificacion.tipodocumento.acuserecibo";
    public static final String AED_NOTIFICACION_TIPODOCUMENTO_NOACCESO="fap.aed.notificacion.tipodocumento.noacceso";
    public static final String NOTIFICACION_PLAZOACCESO="fap.notificacion.plazoacceso";
    public static final String NOTIFICACION_PLAZORESPUESTA="fap.notificacion.plazorespuesta";
    public static final String NOTIFICACION_FRECUENCIA_RECORDATORIO_ACCESO="fap.notificacion.frecuenciarecordatorioacceso";
    public static final String NOTIFICACION_FRECUENCIA_RECORDATORIO_RESPUESTA="fap.notificacion.frecuenciarecordatoriorespuesta";
    public static final String NOTIFICACION_ENLACESEDE="fap.notificacion.enlaceSede";
    // En minutos, si no se pone nada, por defecto es cada minuto
    public static final String NOTIFICACION_REFRESCOBASEDEDATOSFROMWS="fap.notificacion.refrescoBaseDeDatosFromWS";

    public static final String NOTIFICACION_SCHEDULER_ESTADO_DELAYED="fap.notificacion.schedulerestado.delayed";
    public static final String NOTIFICACION_ACTIVARMODIFICACION="fap.notificacion.activarModificacion";

    public static final String APP_NAME_REQUERIMIENTO_JUSTIFICANTE_DESCRIPCION="fap.app.name.requerimiento.justificante.descripcion";

    public static final String DIRECCIONES_TIPO="fap.direcciones.tipo";

    public static final String TERCEROS_RELOAD="fap.terceros.reload";

    public static final String TRAMITACION_TRAMITE_TIPO="fap.tramitacion.tramite.tipo";
    public static final String TRAMITACION_TRAMITE_MODIFICACION_TIPO="fap.tramitacion.tramite.modificacion.tipo";

    // Seguimiento en minutos
    public static final String SEGUIMIENTO_NOTIFICAR_ALERTAR_ANOTACIONES="fap.seguimiento.notificarAlertar.anotaciones";

    public static final String DIRECCION_ANTERIOR_VERSION2_1="fap.direccion.anterior.version2.1";

    // Para que se validen y guarden los datos de la pagina que contiene una tabla que redirige a otra pagina antes de ir a esa otra pagina.
    public static final String TABLAS_VALIDACION_ANTES_REDIRIGIR_PAGINAS="fap.tablas.validacion.antesRedirigirPaginas";

    // Para permitir que el solicitante pueda habilitar a que un Funcionario Habilitado firme y registre por él.
    public static final String FIRMAYREGISTRO_FUNCIONARIOHABILITADO="fap.firmaYRegistro.funcionarioHabilitado";

    // Tipo de documento para la autorizacion de firma de un Funcionario Habilitado
    public static final String FIRMAYREGISTRO_FUNCIONARIOHABILITADO_TIPODOCUMENTO="fap.firmaYRegistro.funcionarioHabilitado.tipoDocumento";

    // Tipo de documento de las solicitud en evaluacion, que pueden ver los evaluadores
    public static final String BAREMACION_EVALUACION_DOCUMENTO_SOLICITUD="fap.baremacion.evaluacion.documento.solicitud";

    // Para configurar que las entidades se guarden nada mas invocarlas con el Nuevo de las tablas, y no esperar a que le den a Guardar.
    public static final String ENTIDADES_GUARDAR_ANTES="fap.entidades.guardar.antes";

    //---------------------------------------------------------
    // Personalizacion de mensajes de Error
    //---------------------------------------------------------
    public static final String MENSAJE_ERROR_MONEDA="fap.mensaje.error.moneda";

    public static final String ICONOS_MOSTRAR="fap.iconos.mostrar";

    // Para habilitar la opcion de listar los documentos subidos
    public static final String DOCUMENTACION_LISTARDOCUMENTOSSUBIDOS="fap.documentacion.listarDocumentosSubidos";

    // Properties de los modulos de FAP (true para usarlos en la aplicación)
    public static final String MODULO_VERIFICACION="fap.modulo.Verificacion";
    public static final String MODULO_ADMINISTRACION="fap.modulo.Administracion";
    public static final String MODULO_PRINCIPAL="fap.modulo.Principal";
    public static final String MODULO_SEGUIMIENTO="fap.modulo.Seguimiento";
    public static final String MODULO_DOCUMENTACION="fap.modulo.Documentacion";
    public static final String MODULO_APORTACION="fap.modulo.Aportacion";
    public static final String MODULO_JUSTIFICACION="fap.modulo.Justificacion";
    public static final String MODULO_PRESENTACION="fap.modulo.Presentacion";
    public static final String MODULO_EXCLUSION="fap.modulo.Exclusion";
    public static final String MODULO_BAREMACION="fap.modulo.Baremacion";
    public static final String MODULO_DESISTIMIENTO="fap.modulo.Desistimiento";
    public static final String MODULO_ACEPTARRENUNCIAR="fap.modulo.AceptarRenunciar";
    public static final String MODULO_ALEGACION="fap.modulo.Alegacion";

    // Para inhabilitar el botón de finalizar la evaluación de una solicitud
    public static final String BAREMACION_FINALIZAR_EVALUACION="fap.baremacion.finalizar.evaluacion";

    // Anotaciones Administrativas Autorizadas
    public static final String ANOTACIONES_ADMINISTRATIVAS_AUTORIZADAS_TIPOS_DOCUMENTOS="fap.anotaciones.administrativas.autorizadas.tipos.documentos";

    // En la tabla de evaluaciones permitir que se recarguen los conceptos económicos
    public static final String BAREMACION_EVALUACION_PERMITIR_RECARGAR_CONCEPTOS="fap.baremacion.evaluacion.permitirRecargarConceptos";

    // Para habilitar la alerta cuando vas a otra pagina y has modificado campos que no has guardado
    public static final String JAVASCRIPT_DETECTAR_CAMBIOS_NO_GUARDADOS="fap.javascript.detectarcambios.noguardados";

    public static final String PORTAFIRMA_SECRET_KEY="fap.portafirma.secret.key";
}
