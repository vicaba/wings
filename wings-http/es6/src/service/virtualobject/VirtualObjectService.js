import axios from 'axios';

import DomainConfig from "./../DomainConfig";

export default class VirtualObjectService {

  static getAllVirtualObjects() {
    console.log(DomainConfig);
    return axios.get(DomainConfig.url.vos());
  }

  static getVirtualObject() {

  }



}
