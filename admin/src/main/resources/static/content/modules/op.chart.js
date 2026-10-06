var ChartCommon = {};

ChartCommon.init = function () {
	var imported = document.createElement('script');
	imported.src = '/content/modules/chart.min.js';
	//imported.src = 'https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.9.1/chart.min.js';
	document.head.append(imported);
}

ChartCommon.customTitle = {
	id: 'customTitle',
    beforeLayout: function (chart, args, opts) {
        var display = opts.display;
        var font = opts.font;

        if (!display) {
            return;
        }
        var ctx = chart.ctx;
        ctx.font = font || '12px "Helvetica Neue", Helvetica, Arial, sans-serif'
        var width = ctx.measureText(opts.text).width;
        chart.options.layout.padding.left = width * 1.3;
    },
    afterDraw: function (chart, args, opts) {
        var font = opts.font;
        var text = opts.text;
        var color = opts.color;
        var ctx = chart.ctx;
        var top = chart.chartArea.top;
        if (opts.display) {
            ctx.fillStyle = color || Chart.defaults.color
            ctx.font = font || '12px "Helvetica Neue", Helvetica, Arial, sans-serif',
            ctx.fillText(text, 0, top)
        }
    }
}

ChartCommon.customTitle1 = {
	id: 'customTitle1',
    beforeLayout: function (chart, args, opts) {
        var display = opts.display;
        var font = opts.font;

        if (!display) {
            return;
        }
        var ctx = chart.ctx;
        ctx.font = font || '12px "Helvetica Neue", Helvetica, Arial, sans-serif'
        var width = ctx.measureText(opts.text).width;
        chart.options.layout.padding.right = width * 1.3;
    },
    afterDraw: function (chart, args, opts) {
        var font = opts.font;
        var text = opts.text;
        var color = opts.color;
        var ctx = chart.ctx;
        var top = chart.chartArea.top;
        let canvasWidth = ctx.canvas.getAttribute('width');
        if (opts.display) {
            ctx.fillStyle = color || Chart.defaults.color
            ctx.font = font || '12px "Helvetica Neue", Helvetica, Arial, sans-serif',
            ctx.fillText(text, canvasWidth - ctx.measureText(opts.text).width, top)
        }
    }
}

/**
 * id : Chart를 그릴 canvas id
 * type : line, bar
 * options : null인 경우 기본 option 적용
 * xAxisArr : x축 arr
 * ex) ['2022','2023','2024'];
 * values : y축 json arr
 * ex) [{
 *	label : '라벨',
 *  data : ['100','200','300','400'],
 *  backgroundColor : '#004CCE',
 *  borderColor : '#004CCE'
 * }];
 * explain : x축, y축에 대한 설명 json
 * ex) {x : '년', 'y' : '건'};
 */
ChartCommon.drawChart = function (id, type, options, xAxisArr, values, explain, heightFixed) {
	var context = document.getElementById(id).getContext('2d');
    var dataSets = [];

    values.forEach(function (ele) {
        var d = {
            lineTension: 0,
            backgroundColor: "#92278F",
            borderColor: "#92278F",
            fill: false
        }
        dataSets.push(Object.assign(d, ele));
    });

    var defaultData = {
        labels : xAxisArr,
        datasets : dataSets
    };

    let autoSize = true;
    if (heightFixed == "FIXED") {
		autoSize = false;
	}

    var defualtOptions = {
        plugins: {
            legend: {
                display: true,
                position : 'bottom'
            },
            customTitle : {},
            tooltip : {
                callbacks : {}
            }
        },
        scales: {
            x: {
                title : {}
            },
            y: {
                suggestedMin: 0,
                title : {},
                // suggestedMax: 100000,
            },
        }
		, responsive: autoSize
    };

    if (explain) {

        if (explain.x) {
            defualtOptions.scales.x.title = {
                display : true,
                align: 'end',
                text : '[' + explain.x + ']'
            };

            defualtOptions.plugins.tooltip.callbacks.title = function (tootipItem) {
                return tootipItem[0].label + '(' + explain.x + ')';
            };

        }

        if (explain.y) {
            defualtOptions.plugins.customTitle = {
                display : true,
                text : '[' + explain.y + ']'
            };

            defualtOptions.plugins.tooltip.callbacks.label = function (tootipItem) {
                return tootipItem.formattedValue + '(' + explain.y + ')';
            };
        }

    }

    if (options != null) {
        Object.keys(options).forEach(function (key) {

            Object.keys(options[key]).forEach(function (childKey) {
                Object.assign(defualtOptions[key][childKey], options[key][childKey]);
            });
        });
    }

    var c = new Chart(context, {
        type : type,
        data : defaultData,
        options : defualtOptions,
        plugins : [ChartCommon.customTitle]
    });

    return c;
}


/**
 * Y축 2개 그래프 그리기
 * id : Chart를 그릴 canvas id
 * type : line, bar
 * options : null인 경우 기본 option 적용
 * xAxisArr : x축 arr
 * ex) ['2022','2023','2024'];
 * values : y축 json arr
 * ex) [
 *	{
 *		label : '라벨1'
 * 		, type : 'bar'					// 그래프 형식 : bar(막대), line(선)
 * 		, data : [100,200,300,400]
 * 		, backgroundColor:'#09C2C7'
 * 		, borderColor:'#09C2C7'
 * 		, minBarLength: 0
 * 		, yAxisID: 'y'					// 사용할 y축 아이디(y[좌측] 또는 y1[우측])
 * }
 * ,
 *	{
 *		label : '라벨2'
 * 		, type : 'bar'					// 그래프 형식 : bar(막대), line(선)
 * 		, data : [100,200,300,400]
 * 		, backgroundColor:'#004CCE'
 * 		, borderColor:'#004CCE'
 * 		, minBarLength: 0
 * 		, yAxisID: 'y1'					// 사용할 y축 아이디(y[좌측] 또는 y1[우측])
 * }
 * ];
 * explain : x축, y축에 대한 설명 json
 * ex) {x : '월', y : '원', y1 : '포인트'};	// y, y1 키 고정(y[좌측], y1[우측])
 */
ChartCommon.drawChartMultiYaxis = function (id, type, options, xAxisArr, values, explain, heightFixed) {
	let context = document.getElementById(id).getContext('2d');
    let dataSets = [];

    let valuesLength = values.length;

    for(let i = 0 ; i < valuesLength ; i++) {
		let ele = values[i];
        let d = {
            lineTension: 0,
            backgroundColor: "#92278F",
            borderColor: "#92278F",
            fill: false
        }
        dataSets.push(Object.assign(d, ele));
	}

    let defaultData = {
        labels : xAxisArr,
        datasets : dataSets
    };

    let autoSize = true;
    if (heightFixed == "FIXED") {
		autoSize = false;
	}

    let defualtOptions = {
        plugins: {
            legend: {
                display: true,
                position : 'bottom',
                align : {
					usePointStyle : true,
				}
            },
            customTitle : {},
            customTitle1 : {},
            tooltip : {
                callbacks : {}
            }
        },
        scales: {
            x: {
                title : {}
            },
            y: {
                suggestedMin: 0,
                title : {},
                position : 'left',
                // suggestedMax: 100000,
            },
        }
		, responsive: autoSize
		, layout: {
			padding: {
				y : 50
			}
		}
    };

    if (explain) {

        if (explain.x) {
            defualtOptions.scales.x.title = {
                display : true,
                align: 'end',
                text : '[' + explain.x + ']'
            };

            defualtOptions.plugins.tooltip.callbacks.title = function (tootipItem) {
                /*if (tootipItem[0].dataset.yAxisID == 'y') {
					return tootipItem[0].label + '(' + explain.y + ')';
				} else {
					return tootipItem[0].label + '(' + explain.y1 + ')';
				}*/
				return tootipItem[0].label;
            };

        }

        if (explain.y && explain.y1) {
			defualtOptions.scales.y1 = {
                suggestedMin: 0,
                title : {},
                position : 'right',
                stacked : false,
                // suggestedMax: 100000,
		        grid: {
		          drawOnChartArea: false, // y1 축 라인 표시 여부
		        },
            },
/*
            defualtOptions.scales.y.title = {
                display : true,
                text : '[' + explain.y + ']',
            };
            defualtOptions.scales.y1.title = {
                display : true,
                text : '[' + explain.y1 + ']',
            };
*/
            defualtOptions.plugins.customTitle = {
                display : true,
                text : '[' + explain.y + ']'
            };
            defualtOptions.plugins.customTitle1 = {
                display : true,
                text : '[' + explain.y1 + ']'
            };
			/*
            defualtOptions.plugins.tooltip.callbacks.label = function (tootipItem) {
				if (tootipItem.datasetIndex == 0) {
                	return tootipItem.formattedValue + '(' + explain.y + ')';
				} else if (tootipItem.datasetIndex == 1) {
                	return tootipItem.formattedValue + '(' + explain.y1 + ')';
				}
            };
            */
        } else if (explain.y) {
            defualtOptions.plugins.customTitle = {
                display : true,
                text : '[' + explain.y + ']'
            };
			/*
            defualtOptions.plugins.tooltip.callbacks.label = function (tootipItem) {
            	return tootipItem.formattedValue + '(' + explain.y + ')';
            };
            */
		} else if (explain.y1) {
            defualtOptions.plugins.customTitle = {
                display : true,
                text : '[' + explain.y1 + ']'
            };
			/*
            defualtOptions.plugins.tooltip.callbacks.label = function (tootipItem) {
            	return tootipItem.formattedValue + '(' + explain.y1 + ')';
            };
            */
        }

    }

    if (options != null) {
        Object.keys(options).forEach(function (key) {

            Object.keys(options[key]).forEach(function (childKey) {
                Object.assign(defualtOptions[key][childKey], options[key][childKey]);
            });
        });
    }

    var c = new Chart(context, {
        type : type,
        data : defaultData,
        options : defualtOptions,
        plugins : [ChartCommon.customTitle, ChartCommon.customTitle1]
    });

    return c;
}

ChartCommon.init();