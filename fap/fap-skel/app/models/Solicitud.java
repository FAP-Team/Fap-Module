// === MANUAL REGION START ===
		public Solicitud(Agente agente) {
			super.init();
			init();
			this.save();
			
			//Crea la participacion
			Participacion p = new Participacion();
			p.agente = agente;
			p.solicitud = this;
			p.tipo = "creador";
			p.save();
		}		
		
		/**
		 * Método para evaluar la obligatoriedad de los documentos
		 * aportados, este método se llama para todos los documentos 
		 * que tengan obligatoriedad automatica. Modificar para Cambiar
		 * el comportamiento.
		 * 
		 * @param uri: uri del documento a evaluar
		 * @return
		 * 		- true: si el documento es obligatorio
		 */
		@Override
		public boolean documentoEsObligatorio(String uri) {
			return false;
		}

		
// === MANUAL REGION END ===