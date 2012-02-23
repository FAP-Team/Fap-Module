
            package enumerado.fap.gen;
            
            public enum TipoDePersonaEnum{
                fisica("Persona física"),juridica("Persona jurídica");
            
                private String value;
            
                private TipoDePersonaEnum(String value){
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
            