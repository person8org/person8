"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.OTHER_KINGDOMS = exports.EXPLORER_URL = exports.SUBJECTS_FILENAME = exports.ME_FILENAME = exports.TERRITORIES = exports.ANIMALS = exports.appConfig = void 0;

var _blockstack = require("blockstack");

var appConfig = new _blockstack.AppConfig(['store_write', 'publish_data']);
exports.appConfig = appConfig;
var ANIMALS = [{
  id: 'cat',
  name: 'Cat',
  superpower: 'Has 9 lives.'
}, {
  id: 'dog',
  name: 'Dog',
  superpower: 'Catching frisbees.'
}, {
  id: 'lion',
  name: 'Lion',
  superpower: 'Roaring loudly.'
}];
exports.ANIMALS = ANIMALS;
var TERRITORIES = [{
  id: 'forest',
  name: 'Forest',
  superpower: 'Trees!'
}, {
  id: 'tundra',
  name: 'Tundra',
  superpower: 'Let it snow!'
}];
exports.TERRITORIES = TERRITORIES;
var ME_FILENAME = 'me.json';
exports.ME_FILENAME = ME_FILENAME;
var SUBJECTS_FILENAME = 'subjects.json';
exports.SUBJECTS_FILENAME = SUBJECTS_FILENAME;
var EXPLORER_URL = 'https://explorer.blockstack.org';
exports.EXPLORER_URL = EXPLORER_URL;
var OTHER_KINGDOMS = [{
  app: 'https://animal-kingdom-1.firebaseapp.com',
  ruler: 'larry.id'
}, {
  app: 'http://localhost:3001',
  ruler: 'larz.id'
}, {
  app: 'https://decentralised-islands.netlify.com',
  ruler: 'yannael_leborgne.id'
}, {
  app: 'https://thirsty-jang-0c0a17.netlify.com',
  ruler: 'ma1222042.id.blockstack'
}];
exports.OTHER_KINGDOMS = OTHER_KINGDOMS;