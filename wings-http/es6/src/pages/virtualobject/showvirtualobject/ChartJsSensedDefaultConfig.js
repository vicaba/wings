export default {
  type: 'line',
  data: {
    datasets: [{
      label: "",
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
      text: "Sensor Data"
    },
    scales: {
      xAxes: [{
        type: "time",
        display: true,
        scaleLabel: {
          display: true,
          labelString: 'Time'
        },
        ticks: {
          suggestedMin: 0,
          beginAtZero: true
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
