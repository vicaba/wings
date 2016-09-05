var map;
function initMap() {
    /* Data points defined as an array of LatLng objects */
    var heatmapData = [
        new google.maps.LatLng(41.4115156,2.1315243),
        new google.maps.LatLng(41.412574, 2.1367894),
        new google.maps.LatLng(41.412574, 2.1367895),
        new google.maps.LatLng(41.412574, 2.1366896),
        new google.maps.LatLng(41.412574, 2.1367097),
        new google.maps.LatLng(41.412574, 2.1367198),
        new google.maps.LatLng(41.412574, 2.1367299),
        new google.maps.LatLng(41.412574, 2.1367300),
        new google.maps.LatLng(41.412574, 2.1367401),
        new google.maps.LatLng(41.412574, 2.1367502),
        new google.maps.LatLng(41.412574, 2.1367603),
        new google.maps.LatLng(41.412574, 2.1367704),
        new google.maps.LatLng(41.412574, 2.1367805),
        new google.maps.LatLng(41.412574, 2.1367906)
    ];

    var sanFrancisco = new google.maps.LatLng(41.412574, 2.1367906);

    map = new google.maps.Map(document.getElementById('heatmap'), {
        center: sanFrancisco,
        zoom: 13,
        mapTypeId: 'satellite'
    });

    var heatmap = new google.maps.visualization.HeatmapLayer({
        data: heatmapData
    });
    heatmap.setMap(map);
    var marker = new google.maps.Marker({
        position: new google.maps.LatLng(41.4115156,2.1315243),
        map: map,
        title: 'Hello World!',
        icon: "https://storage.googleapis.com/support-kms-prod/SNP_2752125_en_v0"
    });
}