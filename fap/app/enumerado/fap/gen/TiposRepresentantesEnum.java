
            package enumerado.fap.gen;
            
            public enum TiposRepresentantesEnum{
                mancomunado("Mancomunado"),solidario("Solidario"),administradorUnico("Administrador Único");
            
                private String value;
            
                private TiposRepresentantesEnum(String value){
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
            