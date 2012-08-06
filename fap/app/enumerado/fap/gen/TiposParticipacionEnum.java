
            package enumerado.fap.gen;
            
            public enum TiposParticipacionEnum{
                creador("Creador"),solicitante("Solicitante"),representante("Representante"),autorizado("Autorizado");
            
                private String value;
            
                private TiposParticipacionEnum(String value){
                    this.value = value;
                }
                
                public String value(){
                    return value;
                }
            
                @Override
                public String toString(){
                    return this.name() + "[" + this.value() + "]";
                }
            }
            