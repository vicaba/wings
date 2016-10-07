import Chart from 'chart.js';

export default class SensedChartJs {
  constructor(context, config) {
    this.chart = new Chart(context, config);
  }

  addData(dataset, value) {
    this.chart.config.data.datasets[dataset].data.push({
      x: value.x,
      y: value.y
    });
    this.update();
  }

  addDataBulk(dataset, values) {
    values.forEach(
      (value) => {
        this.chart.config.data.datasets[dataset].data.push({
          x: value.x,
          y: value.y
        });
      }
    );

    this.update();

  }

  update() {
    this.chart.update();
  }

}
