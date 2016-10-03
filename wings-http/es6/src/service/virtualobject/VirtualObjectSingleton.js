import Rx from "rx";
import RxDOM from "rx-dom";
import uuid from "uuid";

import DomainConfig from "./../DomainConfig";

class VirtualObjectSingleton {

  _nameRegisterMessage() {
    return {
      "op": "vo/register/name/request",
      "voId": this.virtualObjectId
    };
  }

  _metadataMessage() {
    return {
      "voId": this.virtualObjectId,
      "path": this.virtualObjectId,
      "scap": {
        "name": "status",
        "unit": "state"
      },
      "acap": {
        "name": "running/stopped",
        "states": [{
          "stateId": "on"
        }]
      }
    };
  }

  constructor() {
    this._isOpen = false;
    this.virtualObjectId = uuid.v4();
  }

  connect(onOpenCallback = () => {}, onCloseCallback = () => {}) {

    if (this.isOpen()) return this.subject;

    console.log(JSON.stringify(
      this._nameRegisterMessage()
    ));

    var openObserver = Rx.Observer.create(
      (e) => {
        console.log("hi");
        this._isOpen = true;
        this.subject.onNext(
          JSON.stringify(
            this._nameRegisterMessage()
          )
        );

        this.subject.onNext(
          JSON.stringify(
            this._metadataMessage()
          )
        );

        this.observable = this.subject.share();

        onOpenCallback(this);

      });

    var closeObserver = Rx.Observer.create(
      (e) => {
        console.log(e);
        this._isOpen = false;
        onCloseCallback(this);
      }
    );

    this.subject = Rx.DOM.fromWebSocket(
      DomainConfig.url.webSocket(),
      null,
      openObserver,
      closeObserver
    );

    this.observable = this.subject.share();

    return this._socketWrapper();
  }

  getSocketWrapper() {
    return this._socketWrapper();
  }

  isOpen() {
    return this._isOpen;
  }

  isClosed() {
    return !this._isOpen;
  }

  getVirtualObjectId() {
    return this.virtualObjectId;
  }

  _socketWrapper() {
    return {
      socket: this.subject,
      messageEmitter: this.observable
    };
  }

}

export default (new VirtualObjectSingleton);
