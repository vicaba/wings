import axios from 'axios';

import DomainConfig from "./../DomainConfig";

export default class VirtualObjectSensedService {

  static getSensedDataFromVirtualObject(virtualObjectId) {
    return axios.get(DomainConfig.url.voSensed(virtualObjectId));
  }

}