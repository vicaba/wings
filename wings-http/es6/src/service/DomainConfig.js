var config = {
  domain: "127.0.0.1:9000",
  url: {
    voSensed: (virtualObjectId) => {
      return `http://${config.domain}/api/v1/vos/${virtualObjectId}/sensed`;
    },
    vos: () => {
      return `http://${config.domain}/api/v1/vos`;
    },
    vo: (voId) => {
      return `http://${config.domain}/api/v1/vos/${voId}`;
    },
    webSocket: () => {
      return `ws://${config.domain}/api/v1/admin/ws/socket`;
    }
  },
  urlFront: {
    vo: (voId) => {
      return `http://${config.domain}/vos/${voId}`;
    }
  }
};

export default config;
