var configuration = {
    domain: "127.0.0.1:9000"
};

function setObjectsInMap(object, map) {

}

function initializeVirtualObjectMap() {
    $.get("http://" + configuration.domain + "/api/v1/vos", function (data) {
        initWebSocket(data);
        reloadVirtualObjectMap(data);
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
            mapData[mapDataCount] = new google.maps.LatLng(coordinates[0], coordinates[1]);
            mapDataCount = mapDataCount + 1;
        }
    }

    console.log(mapData);

    var barcelona = new google.maps.LatLng(41.412574, 2.1367906);

    map = new google.maps.Map(document.getElementById('virtualobject-map'), {
        center: barcelona,
        zoom: 13,
        mapTypeId: 'satellite'
    });

    var heatmap = new google.maps.visualization.HeatmapLayer({
        data: mapData
    });

    heatmap.setMap(map);

    _(mapData).each(function (coords) {
        var infowindow = new google.maps.InfoWindow({
            content: ("[" + coords[0] + "," + coords[1] + "]")
        });

        var marker = new google.maps.Marker({
            position: coords,
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
        console.log(event.data);
    };

}
