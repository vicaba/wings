import moment from 'moment';

import SensedChartJs from './SensedChartJs';
import ChartJsSensedDefaultConfig from "./ChartJsSensedDefaultConfig";


window.showVirtualObjectInitializer = () => {
  let chart = document.getElementById('single-virtualobject-canvas');
  let showVirtualObject = new ShowVirtualObject(chart);

};

const timeFormat = 'MM/DD/YYYY HH:mm:ss';

let momentTime = (time) => {
  moment(time).format(timeFormat);
};

class ShowVirtualObject {
  constructor(chartElement) {
    this.chartElement = chartElement;
    this.sensedChart = null;
  }

  start() {
    this.sensedChart = new SensedChartJs(this.chartElement, ChartJsSensedDefaultConfig);
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

}
