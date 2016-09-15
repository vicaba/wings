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
    })
        .done(function () {
            alert("second success");
        })
        .fail(function () {
            alert("error");
        })
        .always(function () {
            alert("finished");
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
            var location = new google.maps.LatLng(parseFloat(coordinates[0]), parseFloat(coordinates[1]));
            mapData[mapDataCount] = { location: location, weight: Math.random() };
            mapDataCount = mapDataCount + 1;
            heatPoints[data[dataCount].voId] = { location: location, weight: 0 };
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

    _(mapData).each(function (coords) {
        var infowindow = new google.maps.InfoWindow({
            content: ("[" + coords.location.lat() + "," + coords.location.lng() + "]")
        });

        var marker = new google.maps.Marker({
            position: coords.location,
            map: map,
            title: 'Hello World!',
            icon: "https://storage.googleapis.com/support-kms-prod/SNP_2752125_en_v0"
        });

        marker.addListener('click', function () {
            infowindow.open(map, marker);
        });

    });

}

function initWebSocket(data) {

    var webSocket = new WebSocket("ws://localhost:9000/api/v1/admin/ws/socket");

    webSocket.onopen = function () {

        console.log("WebSocket opened");

        webSocket.send(JSON.stringify({
            "op" : "vo/register/name/request",
            "voId" : "73f86a2e-1004-4011-8a8f-3f78cdd6113c"
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
            console.log(virtualObject);
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

        heatPoints[virtualObject.voId] = {location: heatPoints[virtualObject.voId].location, weight: virtualObject.value};
        heat();
    };

}

function heat() {
    var array = Object.keys(heatPoints).map(function(x) { return heatPoints[x]; });
    heatmap.setData(array);
}