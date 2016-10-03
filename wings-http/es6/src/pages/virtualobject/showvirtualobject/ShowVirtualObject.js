import moment from 'moment';

import SensedChartJs from './SensedChartJs';
import ChartJsSensedDefaultConfig from "./ChartJsSensedDefaultConfig";
import VirtualObjectSensedService from "./../../../service/virtualobject/VirtualObjectSensedService";
import VirtualObjectSingleton from "./../../../service/virtualobject/VirtualObjectSingleton";


window.showVirtualObjectInitializer = (virtualObjectId) => {
  let chart = document.getElementById('single-virtualobject-canvas');
  let showVirtualObject = new ShowVirtualObject();
  showVirtualObject.start(chart, virtualObjectId);

};

const TimeFormat = 'MM/DD/YYYY HH:mm:ss';

const DefaultDataSet = 0;

let momentTime = (time) => {
  return moment(time).format(TimeFormat);
};

class ShowVirtualObject {
  constructor(chartElement, virtualObjectId) {
    this.chartElement = null;
    this.virtualObjectId = null;
    this.sensedChart = null;
  }

  start(chartElement, virtualObjectId) {
    this.chartElement = chartElement;
    this.virtualObjectId = virtualObjectId;


    this.sensedChart = new SensedChartJs(this.chartElement, ChartJsSensedDefaultConfig);

    VirtualObjectSensedService.getSensedDataFromVirtualObject(this.virtualObjectId)
      .then((response) => {
        console.log(response);
        let sensedData = response.data;
        this.addDataToSensedChart(DefaultDataSet, sensedData);
      });

  }

  addDataToSensedChart(dataset, values) {

    let transformData = (value) => {
      return {
        y: parseFloat(value.value),
        x: momentTime(value.c)
      };
    };

    if (Array.isArray(values)) {
      let preparedValues = values.map(transformData);
      this.sensedChart.addDataBulk(dataset, preparedValues);
    } else {
      let preparedValue = transformData(values);
      this.sensedChart.addData(dataset, preparedValue);
    }
  }

  _startVirtualObject() {

    let socket = VirtualObjectSingleton.connect(
      (_socket) => {
        this._watchVirtualObject(_socket.getSocketWrapper(), virtualObjectId);
      }
    );

    let socketObservable = socket.messageEmitter;

    socketObservable.subscribe(
      (message) => {
        console.log(message);
      }
      ,
      (error) => {

      },
      () => {

      });

    socketObservable.subscribe(
      function (x) {
        console.log('Value published to observer #2: ' + x);
      },
      function (e) {
        console.log('onError: ' + e.message);
      },
      function () {
        console.log('onCompleted');
      });


  }

  _watchVirtualObject(socketWrapper, virtualObjectid) {
    socketWrapper.socket.onNext(
      JSON.stringify(VirtualObjectMessages.WatchVirtualObject(virtualObjectid))
    );
  }

}
