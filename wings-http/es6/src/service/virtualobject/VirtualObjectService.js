import axios from 'axios';

import DomainConfig from "./../DomainConfig";

export default class VirtualObjectService {

  static getAllVirtualObjects() {
    return axios.get(DomainConfig.url.vos());
  }

  static getVirtualObject() {

  }



}
