"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;

var _react = _interopRequireWildcard(require("react"));

var _reactRouterDom = require("react-router-dom");

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
  navbar: "NavBar__navbar___3CdYz"
};

require('load-styles')("/*  imported from NavBar.css  */\n\n.NavBar__navbar___3CdYz {\n  background-color:  rgb(33, 31, 109) !important;\n}\n");

var NavBar =
/*#__PURE__*/
function (_Component) {
  _inherits(NavBar, _Component);

  function NavBar() {
    _classCallCheck(this, NavBar);

    return _possibleConstructorReturn(this, _getPrototypeOf(NavBar).apply(this, arguments));
  }

  _createClass(NavBar, [{
    key: "render",
    value: function render() {
      var username = this.props.username;
      return _react.default.createElement("nav", {
        className: "navbar navbar-expand-md navbar-dark bg-dark fixed-top"
      }, _react.default.createElement(_reactRouterDom.Link, {
        className: "navbar-brand",
        to: "/"
      }, "ClipBox"), _react.default.createElement("div", {
        className: "collapse navbar-collapse",
        id: "navbarsExampleDefault"
      }, _react.default.createElement("ul", {
        className: "navbar-nav mr-auto"
      }, _react.default.createElement("li", {
        className: "nav-item"
      }, _react.default.createElement(_reactRouterDom.Link, {
        className: "nav-link",
        to: "/kingdom/".concat(username)
      }, "Your Kingdom")), _react.default.createElement("li", {
        className: "nav-item"
      }, _react.default.createElement(_reactRouterDom.Link, {
        className: "nav-link",
        to: "/animals"
      }, "Animals")), _react.default.createElement("li", {
        className: "nav-item"
      }, _react.default.createElement(_reactRouterDom.Link, {
        className: "nav-link",
        to: "/territories"
      }, "Territories")), _react.default.createElement("li", {
        className: "nav-item"
      }, _react.default.createElement(_reactRouterDom.Link, {
        className: "nav-link",
        to: "/others"
      }, "Other Kingdoms")))), _react.default.createElement("ul", {
        className: "navbar-nav mr-auto"
      }, _react.default.createElement("li", {
        className: "nav-item"
      }, _react.default.createElement(_reactRouterDom.Link, {
        className: "nav-link",
        to: "/me"
      }, username))), _react.default.createElement("button", {
        className: "btn btn-primary",
        onClick: this.props.signOut.bind(this)
      }, "Sign out"));
    }
  }]);

  return NavBar;
}(_react.Component);

var _default = NavBar;
exports.default = _default;