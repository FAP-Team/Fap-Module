/*
  Pie simple de Highcharts
  ==================================
  Argumentos
  	titulo - Título
  	nombre - Nombre
  	mapa   - Datos que se van a representar en el "Pie". Array de pares: [Valor - Número de ocurrencias]
  	          Ejemplo: [[ 'Borrador', 4], [ 'Iniciada', 4], [ 'En Verificación', 7]]
*/

function ejecutarPie(mapa, titulo, subtitulo, nombre) {
    var chart;
    var dataMapa = eval(mapa);
    $(document).ready(function() {
        chart = new Highcharts.Chart({
            chart: {
                renderTo: 'grafica',
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false
            },
            title: {
                text: titulo
            },
            subtitle: {
                text: subtitulo
            },
            tooltip: {
                formatter: function() {
                    return '<b>'+ this.point.name +'</b>: '+ this.percentage +' %';
                }
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: true,
                        color: '#000000',
                        connectorColor: '#000000',
                        formatter: function() {
                            return '<b>'+ this.point.name +'</b>: '+ this.y;
                        }
                    }
                }
            },
            series: [{
                type: 'pie',
                name: nombre,
                data: dataMapa
            }]
        });
    });
    
}

/*
  Basic Columns de Highcharts
  ==================================
  Argumentos
  	keyArray - Lista de valores del eje X.
  	valueArray - Lista de valores del eje Y.
  	titulo - Título
  	subtitulo - Subtítulo
  	ejex - Título del eje X
  	ejey - Título del eje Y
  	nombre - Nombre genérico de lo que se está represntando.
  	
  	keyArray - valueArray: Datos que se van a representar en el "Column". 
  	Tiene que recibir dos Array del tipo: [Valores] [Número de ocurrencias]
	Ejemplo: keyArray -> ['borrador','iniciada','En verificación']
	 		 valueArray -> [4, 4, 7]

*/

function ejecutarColumn(keyArray, valueArray, titulo, subtitulo, ejex, ejey, nombre) {
    var chart;
    var dataKey = eval(keyArray);
    var dataValue = eval(valueArray);
    $(document).ready(function() {
        chart = new Highcharts.Chart({
            chart: {
                renderTo: 'grafica',
                type: 'column'
            },
            title: {
                text: titulo
            },
            subtitle: {
                text: subtitulo
            },
            xAxis: {
                categories: dataKey,
                title: {
                    text: ejex
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: ejey
                }
            },
            legend: {
                layout: 'vertical',
                backgroundColor: '#FFFFFF',
                align: 'left',
                verticalAlign: 'top',
                x: 100,
                y: 70,
                floating: true,
                shadow: true
            },
            tooltip: {
                formatter: function() {
                    return '<b>'+ this.x +'</b>: '+ this.y;
                }
            },
            plotOptions: {
                column: {
                    pointPadding: 0.2,
                    borderWidth: 0
                }
            },
               series: [{
                	name: nombre,
                	data: dataValue
            }]
        });
    });
}