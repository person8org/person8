"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;

var _react = _interopRequireWildcard(require("react"));

var _reactRouterDom = require("react-router-dom");

var _blockstack = require("blockstack");

var _NavBar = _interopRequireDefault(require("./NavBar"));

var _constants = require("./constants");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) { var desc = Object.defineProperty && Object.getOwnPropertyDescriptor ? Object.getOwnPropertyDescriptor(obj, key) : {}; if (desc.get || desc.set) { Object.defineProperty(newObj, key, desc); } else { newObj[key] = obj[key]; } } } } newObj.default = obj; return newObj; } }

function _typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

function _extends() { _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; }; return _extends.apply(this, arguments); }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

function _possibleConstructorReturn(self, call) { if (call && (_typeof(call) === "object" || typeof call === "function")) { return call; } return _assertThisInitialized(self); }

function _getPrototypeOf(o) { _getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return _getPrototypeOf(o); }

function _assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); if (superClass) _setPrototypeOf(subClass, superClass); }

function _setPrototypeOf(o, p) { _setPrototypeOf = Object.setPrototypeOf || function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return _setPrototypeOf(o, p); }

var styles = {};

require('load-styles')("/*  imported from SignedIn.css  */");

var SignedIn =
/*#__PURE__*/
function (_Component) {
  _inherits(SignedIn, _Component);

  function SignedIn(props) {
    var _this;

    _classCallCheck(this, SignedIn);

    _this = _possibleConstructorReturn(this, _getPrototypeOf(SignedIn).call(this, props));
    _this.userSession = new _blockstack.UserSession({
      appConfig: _constants.appConfig
    });
    _this.state = {
      me: {},
      savingMe: false,
      savingKingdown: false,
      redirectToMe: false
    };
    _this.loadMe = _this.loadMe.bind(_assertThisInitialized(_this));
    _this.saveMe = _this.saveMe.bind(_assertThisInitialized(_this));
    _this.signOut = _this.signOut.bind(_assertThisInitialized(_this));
    return _this;
  }

  _createClass(SignedIn, [{
    key: "componentWillMount",
    value: function componentWillMount() {
      this.loadMe();
    }
  }, {
    key: "loadMe",
    value: function loadMe() {
      var _this2 = this;

      var options = {
        decrypt: false
      };
      this.userSession.getFile(_constants.ME_FILENAME, options).then(function (content) {
        if (content) {
          var me = JSON.parse(content);

          _this2.setState({
            me: me,
            redirectToMe: false
          });
        } else {
          var _me = null;

          _this2.setState({
            me: _me,
            redirectToMe: true
          });
        }
      });
    }
  }, {
    key: "saveMe",
    value: function saveMe(me) {
      var _this3 = this;

      this.setState({
        me: me,
        savingMe: true
      });
      var options = {
        encrypt: false
      };
      this.userSession.putFile(_constants.ME_FILENAME, JSON.stringify(me), options).finally(function () {
        _this3.setState({
          savingMe: false
        });
      });
    }
  }, {
    key: "signOut",
    value: function signOut(e) {
      e.preventDefault();
      this.userSession.signUserOut();
      window.location = '/';
    }
  }, {
    key: "render",
    value: function render() {
      var _this4 = this;

      var username = this.userSession.loadUserData().username;
      var me = this.state.me;
      var redirectToMe = this.state.redirectToMe; // Terje's intercept:

      return _react.default.createElement("div", {
        style: {
          display: 'none'
        }
      }, "Signed in!");

      if (redirectToMe) {
        // User hasn't configured her animal
        if (window.location.pathname !== '/me') {
          return _react.default.createElement(_reactRouterDom.Redirect, {
            to: "/me"
          });
        }
      }

      if (window.location.pathname === '/') {
        return _react.default.createElement(_reactRouterDom.Redirect, {
          to: "/kingdom/".concat(username)
        });
      }

      return _react.default.createElement("div", {
        className: "SignedIn"
      }, _react.default.createElement(_NavBar.default, {
        username: username,
        signOut: this.signOut
      }), _react.default.createElement(_reactRouterDom.Switch, null, _react.default.createElement(_reactRouterDom.Route, {
        path: "/animals",
        render: function render(routeProps) {
          return _react.default.createElement(OptionsList, _extends({
            type: "animals"
          }, routeProps));
        }
      }), _react.default.createElement(_reactRouterDom.Route, {
        path: "/territories",
        render: function render(routeProps) {
          return _react.default.createElement(OptionsList, _extends({
            type: "territories"
          }, routeProps));
        }
      }), _react.default.createElement(_reactRouterDom.Route, {
        path: "/others",
        render: function render(routeProps) {
          return _react.default.createElement(OtherKingdoms, _extends({
            type: "territories"
          }, routeProps));
        }
      }), _react.default.createElement(_reactRouterDom.Route, {
        path: "/me",
        render: function render(routeProps) {
          return _react.default.createElement(EditMe, _extends({
            me: me,
            saveMe: _this4.saveMe,
            username: username
          }, routeProps));
        }
      }), _react.default.createElement(_reactRouterDom.Route, {
        path: "/kingdom/".concat(username),
        render: function render(routeProps) {
          return _react.default.createElement(Kingdom, _extends({
            myKingdom: true,
            protocol: window.location.protocol,
            ruler: username,
            currentUsername: username,
            realm: window.location.origin.split('//')[1]
          }, routeProps));
        }
      }), _react.default.createElement(_reactRouterDom.Route, {
        path: "/kingdom/:protocol/:realm/:ruler",
        render: function render(routeProps) {
          return _react.default.createElement(Kingdom, _extends({
            myKingdom: false,
            protocol: routeProps.match.params.protocol,
            realm: routeProps.match.params.realm,
            ruler: routeProps.match.params.ruler,
            currentUsername: username
          }, routeProps));
        }
      })));
    }
  }]);

  return SignedIn;
}(_react.Component);

var _default = SignedIn;
exports.default = _default;