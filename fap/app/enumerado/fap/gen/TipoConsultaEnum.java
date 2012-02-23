
            package enumerado.fap.gen;
            
            public enum TipoConsultaEnum{
                tipoSQL("SQL"),tipoJPQL("JPSQL");
            
                private String value;
            
                private TipoConsultaEnum(String value){
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
            