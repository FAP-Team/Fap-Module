
            package enumerado.fap.gen;
            
            public enum TipoFirmaJuridicaEnum{
                cif("Certificado de empresa"),representantes("Certificados de los representantes");
            
                private String value;
            
                private TipoFirmaJuridicaEnum(String value){
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
            