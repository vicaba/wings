import * as underscore from "underscore";

import VirtualObjectService from "./../../../service/virtualobject/VirtualObjectService";
import VirtualObjectSingleton from "./../../../service/virtualobject/VirtualObjectSingleton";
import * as VirtualObjectMessages from "./../../../service/virtualobject/VirtualObjectMessages";
import DomainConfig from "./../../../service/DomainConfig";

window.mapVirtualObjectInitializer = () => {
  let mapElement = document.getElementById('virtualobject-map');
  let mapVirtualObject = new MapVirtualObject();
  mapVirtualObject.start(mapElement);
};

class MapVirtualObject {

  constructor() {

    this._addMarker = this._addMarker.bind(this);
    this._transformVirtualObjectToCoordinates = this._transformVirtualObjectToCoordinates.bind(this);

  }

  start(mapElement) {
    this.mapElement = mapElement;
    this.mapCenter = new google.maps.LatLng(41.412574, 2.1367906);
    this.mapData = null;
    this.map = null;
    this.heatMap = null;
    this.initializeMap();
  }

  initializeMap() {

    this.map = new google.maps.Map(document.getElementById('virtualobject-map'), {
      center: this.mapCenter,
      zoom: 13,
      mapTypeId: 'satellite'
    });

    this.heatMap = new google.maps.visualization.HeatmapLayer({});

    this.heatMap.setMap(this.map);

    this.addDataPoints();

  }

  addDataPoints() {

    VirtualObjectService.getAllVirtualObjects()
      .then((response) => {
        let data = response.data
          .map(this._transformVirtualObjectToCoordinates)
          .filter((value) => value != 'undefined');
        this.mapData = this._generateMapData(data);
        _.each(this.mapData, this._addMarker);
        this._startVirtualObject();
      })
      .catch((error) => {
        console.log(error);
        alert('An error occurred while trying to retrieve VirtualObjects');
      });
  }

  _generateMapData(data) {
    var resultMapData = {};
    data.forEach((value) => resultMapData[value.voId] = value);
    return resultMapData;
  }

  _transformVirtualObjectToCoordinates(virtualObject) {

    let coordinates = _.get(virtualObject, "metadata.geometry.coordinates", "undefined");

    if (coordinates != 'undefined') {
      let voId = virtualObject.voId;
      let location = new google.maps.LatLng(parseFloat(coordinates[0]), parseFloat(coordinates[1]));

      return {
        voId: voId,
        loc: {location: location, weight: 0}
      };

    } else {
      return coordinates;
    }
  }

  _addMarker(heatPoint) {

    let location = heatPoint.loc;
    let showVirtualObjectRef = `${DomainConfig.urlFront.vo(heatPoint.voId)}`;
    let heatPointInfoWindow = new google.maps.InfoWindow({
      content: ("[" + location.location.lat() + "," + location.location.lng() + "]" + "<a href='" + showVirtualObjectRef + "'>See</a>")
    });

    let marker = new google.maps.Marker({
      position: location.location,
      map: this.map,
      title: 'Hello World!',
      icon: "https://storage.googleapis.com/support-kms-prod/SNP_2752125_en_v0"
    });

    marker.addListener('click', () => {
      heatPointInfoWindow.open(this.map, marker);
    });

  }

  _startVirtualObject() {

    let socket = VirtualObjectSingleton.connect(
      (_socket) => {
        _.each(this.mapData,
          (value) => {
            this._watchVirtualObjects(_socket.getSocketWrapper(), value);
          }
        );
      }
    );

    let socketObservable = socket.messageEmitter;

    socketObservable.subscribe(
      (message) => {
        let sensedData = JSON.parse(message.data);
        if (_.get(sensedData, "value", "undefined") == "undefined") return;
        this.mapData[sensedData.voId].loc.weight = sensedData.value;
        this.heatMap.setData(_.map(this.mapData, v => v.loc));
      }
      ,
      (error) => {

      },
      () => {

      });
  }

  _watchVirtualObjects(socketWrapper, value) {
    socketWrapper.socket.onNext(
      JSON.stringify(VirtualObjectMessages.WatchVirtualObject(value.voId))
    );
  }

}
