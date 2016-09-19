var configuration = {
    domain: "127.0.0.1:9000"
};

var heatPoints = [];
var heatmap = null;

function setObjectsInMap(object, map) {

}

function initializeVirtualObjectMap() {
    $.get("http://" + configuration.domain + "/api/v1/vos", function (data) {
        reloadVirtualObjectMap(data);
        initWebSocket(data);
    });
}

function reloadVirtualObjectMap(data) {
    var map;
    var mapData = [];

    if (data.length == 0) {
        return;
    }

    var mapDataCount = 0;
    var dataCount = 0;
    for (dataCount = 0; dataCount < data.length; dataCount++) {
        var coordinates = _.get(data[dataCount], "metadata.geometry.coordinates", "undefined");
        if (coordinates !== "undefined") {
            var voId = data[dataCount].voId;
            var location = new google.maps.LatLng(parseFloat(coordinates[0]), parseFloat(coordinates[1]));
            heatPoints[voId] = {
                voId: voId,
                loc: {location: location, weight: 0}
            };
        }
    }

    var barcelona = new google.maps.LatLng(41.412574, 2.1367906);

    map = new google.maps.Map(document.getElementById('virtualobject-map'), {
        center: barcelona,
        zoom: 13,
        mapTypeId: 'satellite'
    });

    heatmap = new google.maps.visualization.HeatmapLayer({});

    heatmap.setMap(map);

    for (var key in heatPoints) {
        if (heatPoints.hasOwnProperty(key)) {

            var addMarker = function () {
                var current = heatPoints[key];
                var loc = heatPoints[key].loc;
                var infowindow = new google.maps.InfoWindow({
                    content: ("[" + loc.location.lat() + "," + loc.location.lng() + "]" + "<a href='http://" + configuration.domain + "/vos/" + current.voId + "'>See</a>")
                });

                var marker = new google.maps.Marker({
                    position: loc.location,
                    map: map,
                    title: 'Hello World!',
                    icon: "https://storage.googleapis.com/support-kms-prod/SNP_2752125_en_v0"
                });

                marker.addListener('click', function () {
                    infowindow.open(map, marker);
                });
            };

            addMarker();

        }
    }

}

function initWebSocket(data) {

    var webSocket = new WebSocket("ws://localhost:9000/api/v1/admin/ws/socket");

    webSocket.onopen = function () {

        console.log("WebSocket opened");

        webSocket.send(JSON.stringify({
            "op": "vo/register/name/request",
            "voId": "73f86a2e-1004-4011-8a8f-3f78cdd6113c"
        }));

        webSocket.send(JSON.stringify({
            "voId": "73f86a2e-1004-4011-8a8f-3f78cdd6113c",
            "path": "73f86a2e-1004-4011-8a8f-3f78cdd6113c",
            "scap": {
                "name": "status",
                "unit": "state"
            },
            "acap": {
                "name": "running/stopped",
                "states": [{
                    "stateId": "on"
                }]
            }
        }));


        _(data).each(function (virtualObject) {
            webSocket.send(JSON.stringify({
                "op": "vo/watch",
                "path": virtualObject.voId
            }));
        });


    };

    webSocket.onerror = function (error) {
        console.log("WebSocket error", error);
    };

    webSocket.onclose = function () {
        console.log("WebSocket closed");

    };

    webSocket.onmessage = function (event) {

        var virtualObject = JSON.parse(event.data);
        console.log(virtualObject.voId + " = " + virtualObject.value);

        $.extend(true, heatPoints[virtualObject.voId], {
            loc: {
                weight: virtualObject.value
            }
        });

        heat();
    };

}

function heat() {
    var array = Object.keys(heatPoints).map(function (x) {
        return heatPoints[x].loc;
    });
    heatmap.setData(array);
}

// ShowVirtualObject

function showVirtualObjectInitializer(virtualObjectId) {
    var ctx = document.getElementById("canvas").getContext("2d");
    window.myLine = new Chart(ctx, config);
    initWebSocket2(virtualObjectId);
    var request = requestVirtualObjectSensedHistory(virtualObjectId);
    request
        .done(function (data) {
            console.log(data);
            addDataBulk(data)
        })
        .fail(function (e) {
            console.log(e);
        }).always(function () {
    });

}

function requestVirtualObjectSensedHistory(virtualObjectId) {
    var uri = "http://" + configuration.domain + "/api/v1/vos/" + virtualObjectId + "/sensed";
    return $.get("http://localhost:9000/api/v1/vos/" + virtualObjectId + "/sensed");
}


function initWebSocket2(virtualObjectId) {

    var currentUrl = document.URL;

    var webSocket = new WebSocket("ws://localhost:9000/api/v1/admin/ws/socket");

    webSocket.onopen = function () {

        console.log("WebSocket opened");

        webSocket.send(JSON.stringify({
            "op": "vo/register/name/request",
            "voId": "73f86a2e-1004-4011-8a8f-3f78cdd6113c"
        }));

        webSocket.send(JSON.stringify({
            "voId": "73f86a2e-1004-4011-8a8f-3f78cdd6113c",
            "path": "73f86a2e-1004-4011-8a8f-3f78cdd6113c",
            "scap": {
                "name": "status",
                "unit": "state"
            },
            "acap": {
                "name": "running/stopped",
                "states": [{
                    "stateId": "on"
                }]
            }
        }));


        webSocket.send(JSON.stringify({
            "op": "vo/watch",
            "path": virtualObjectId
        }));


    };

    webSocket.onerror = function (error) {
        console.log("WebSocket error", error);
    };

    webSocket.onclose = function () {
        console.log("WebSocket closed");

    };

    webSocket.onmessage = function (event) {
        console.log(event.data);
        var virtualObjectSensed = JSON.parse(event.data);
        addData(virtualObjectSensed);
    };

}

var config = {
    type: 'line',
    data: {
        datasets: [{
            label: "Dataset with string point data",
            data: [],
            fill: false,
            borderColor: "rgba(131,101,129,0.4)",
            backGroundColor: "rgba(247,23,90,0.5)",
            pointBorderColor: "rgba(69,112,87,0.7)",
            pointBackgroundColor: "rgba(16,63,185,0.5)",
            pointBorderWidth: 1
        }]
    },
    options: {
        responsive: true,
        title: {
            display: true,
            text: "Chart.js Time Point Data"
        },
        scales: {
            xAxes: [{
                type: "time",
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: 'Date'
                }
            }],
            yAxes: [{
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: 'value'
                }
            }]
        }
    }
};

function addDataBulk(values) {

    for (var key in values) {
        var value = values[key];
        if (config.data.datasets.length > 0) {
            var newTime = moment(parseFloat(value.c))
                .format('MM/DD/YYYY HH:mm:ss');
            for (var index = 0; index < config.data.datasets.length; ++index) {
                config.data.datasets[index].data.push({
                    x: newTime,
                    y: parseFloat(value.value)
                });
            }
        }
    }
    window.myLine.update();
}

function addData(value) {
    if (config.data.datasets.length > 0) {
        var lastTime = myLine.scales['x-axis-0'].labelMoments[0].length ? myLine.scales['x-axis-0'].labelMoments[0][myLine.scales['x-axis-0'].labelMoments[0].length - 1] : moment();
        var newTime = moment(parseFloat(value.c))
            .format('MM/DD/YYYY HH:mm:ss');
        for (var index = 0; index < config.data.datasets.length; ++index) {
            config.data.datasets[index].data.push({
                x: newTime,
                y: parseFloat(value)
            });
        }
        window.myLine.update();
    }
}
