"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;

var _react = _interopRequireWildcard(require("react"));

var _blockstack = require("blockstack");

var _Landing = _interopRequireDefault(require("./Landing"));

var _SignedIn = _interopRequireDefault(require("./SignedIn"));

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

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
  "badge-primary": "App__badge-primary___34P2N",
  "btn-primary": "App__btn-primary___299G5",
  disabled: "App__disabled___3NO_E",
  card: "App__card___2c15L",
  "card-img-top": "App__card-img-top___AvyAV",
  "list-group": "App__list-group___2yL98"
};

require('load-styles')("/*  imported from App.css  */\n\nbody {\n}\n\n.App__badge-primary___34P2N {\n  background-color: rgb(33, 31, 109);\n  padding-top: 8px;\n}\n\n.App__btn-primary___299G5 {\n  color: #FFFFFF;\n  background-color: rgb(33, 31, 109);\n  border: 0;\n}\n\n.App__btn-primary___299G5:hover {\n  opacity: 0.5;\n  border: 0;\n}\n\n.App__btn-primary___299G5.App__disabled___3NO_E, .App__btn-primary___299G5:disabled {\n  opacity: 0.5;\n  background-color: rgb(135, 172, 196);\n}\n\n.App__card___2c15L {\n  max-width: 320px;\n  border-top-width: 1px;\n  border-right-width: 1px;\n  border-bottom-width: 1px;\n  border-left-width: 1px;\n  border-top-style: solid;\n  border-right-style: solid;\n  border-bottom-style: solid;\n  border-left-style: solid;\n  border-image-source: initial;\n  border-image-slice: initial;\n  border-image-width: initial;\n  border-image-outset: initial;\n  border-image-repeat: initial;\n  border-top-color: rgb(196, 216, 229);\n  border-right-color: rgb(196, 216, 229);\n  border-bottom-color: rgb(196, 216, 229);\n  border-left-color: rgb(196, 216, 229);\n  border-top-left-radius: 8px;\n  border-top-right-radius: 8px;\n  border-bottom-right-radius: 8px;\n  border-bottom-left-radius: 8px;\n  box-shadow: rgba(0, 0, 0, 0.0470588) 0px 4px 4px;\n}\n\n.App__card-img-top___AvyAV {\n  border-top-left-radius: 0;\n  border-top-right-radius: 0;\n}\n\n.App__list-group___2yL98 {\n  border-top-width: 1px;\n  border-right-width: 1px;\n  border-bottom-width: 1px;\n  border-left-width: 1px;\n  border-top-style: solid;\n  border-right-style: solid;\n  border-bottom-style: solid;\n  border-left-style: solid;\n  border-image-source: initial;\n  border-image-slice: initial;\n  border-image-width: initial;\n  border-image-outset: initial;\n  border-image-repeat: initial;\n  border-top-color: rgb(196, 216, 229);\n  border-right-color: rgb(196, 216, 229);\n  border-bottom-color: rgb(196, 216, 229);\n  border-left-color: rgb(196, 216, 229);\n  border-top-left-radius: 8px;\n  border-top-right-radius: 8px;\n  border-bottom-right-radius: 8px;\n  border-bottom-left-radius: 8px;\n  box-shadow: rgba(0, 0, 0, 0.0470588) 0px 4px 4px;\n}\n");

var App =
/*#__PURE__*/
function (_Component) {
  _inherits(App, _Component);

  function App(props) {
    var _this;

    _classCallCheck(this, App);

    _this = _possibleConstructorReturn(this, _getPrototypeOf(App).call(this, props));
    _this.userSession = new _blockstack.UserSession();
    console.log("Props=", _this.props);
    return _this;
  }

  _createClass(App, [{
    key: "componentWillMount",
    value: function componentWillMount() {
      var _this2 = this;

      var session = this.userSession;

      if (this.props.dispatch) {
        this.props.dispatch("UserSession", session);
      }

      if (!session.isUserSignedIn() && session.isSignInPending()) {
        session.handlePendingSignIn().then(function (userData) {
          if (!userData.username) {
            throw new Error('This app requires a username.');
          }

          if (_this2.props.dispatch) {
            _this2.props.dispatch("UserData", userData);
          }
        });
      }
    }
  }, {
    key: "render",
    value: function render() {
      return _react.default.createElement("main", {
        role: "main"
      }, this.userSession.isUserSignedIn() ? _react.default.createElement("div", null, this.props.children, _react.default.createElement(_SignedIn.default, null)) : _react.default.createElement(_Landing.default, null));
    }
  }]);

  return App;
}(_react.Component);

var _default = App;
exports.default = _default;