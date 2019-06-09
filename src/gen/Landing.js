"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;

var _react = _interopRequireWildcard(require("react"));

var _blockstack = require("blockstack");

var _constants = require("./constants");

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) { var desc = Object.defineProperty && Object.getOwnPropertyDescriptor ? Object.getOwnPropertyDescriptor(obj, key) : {}; if (desc.get || desc.set) { Object.defineProperty(newObj, key, desc); } else { newObj[key] = obj[key]; } } } } newObj.default = obj; return newObj; } }

function _typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

function _possibleConstructorReturn(self, call) { if (call && (_typeof(call) === "object" || typeof call === "function")) { return call; } return _assertThisInitialized(self); }

function _assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }

function _getPrototypeOf(o) { _getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return _getPrototypeOf(o); }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); if (superClass) _setPrototypeOf(subClass, superClass); }

function _setPrototypeOf(o, p) { _setPrototypeOf = Object.setPrototypeOf || function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return _setPrototypeOf(o, p); }

var styles = {
  Landing: "Landing__Landing___Ks3RI"
};

require('load-styles')("/*  imported from Landing.css  */\n\n.Landing__Landing___Ks3RI {\n  background-color: black;\n  color: white;\n  display: -ms-flexbox;\n  display: flex;\n  -ms-flex-align: center;\n  align-items: center;\n  width: 100%;\n  max-width: 330px;\n  padding: 15px;\n  /* margin: auto; */\n  margin: 2em;\n  padding-top: 40px;\n  padding-bottom: 40px;\n  font-size: larger;\n}\n");

var Landing =
/*#__PURE__*/
function (_Component) {
  _inherits(Landing, _Component);

  function Landing() {
    var _this;

    _classCallCheck(this, Landing);

    _this = _possibleConstructorReturn(this, _getPrototypeOf(Landing).call(this));
    _this.userSession = new _blockstack.UserSession({
      appConfig: _constants.appConfig
    });
    return _this;
  }

  _createClass(Landing, [{
    key: "signIn",
    value: function signIn(e) {
      e.preventDefault();
      this.userSession.redirectToSignIn();
    }
  }, {
    key: "render",
    value: function render() {
      return _react.default.createElement("div", {
        className: "Landing"
      }, _react.default.createElement("h1", {
        className: "h1 mb-3 font-weight-normal"
      }), _react.default.createElement("div", {
        className: "card"
      }, _react.default.createElement("p", null, "A lifeline for the Digital Nomad."), _react.default.createElement("p", null, "Keep your essential documentation from an encrypted vault."), _react.default.createElement("p", null, "Request emergency funding through the Lightning network.")), _react.default.createElement("div", {
        className: "form-signin"
      }, _react.default.createElement("button", {
        className: "btn btn-lg btn-primary",
        onClick: this.signIn.bind(this)
      }, "Sign in with Blockstack!!!")));
    }
  }]);

  return Landing;
}(_react.Component);

var _default = Landing;
exports.default = _default;