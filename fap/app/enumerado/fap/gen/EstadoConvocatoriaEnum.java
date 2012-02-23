
            package enumerado.fap.gen;
            
            public enum EstadoConvocatoriaEnum{
                presentacion("Presentación"),instruccion("Instrucción");
            
                private String value;
            
                private EstadoConvocatoriaEnum(String value){
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
            