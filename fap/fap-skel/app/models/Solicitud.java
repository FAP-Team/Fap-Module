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
// === MANUAL REGION END ===