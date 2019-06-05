"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.requestImages = void 0;

var _status = require("modules/status");

var _bson = _interopRequireDefault(require("bson"));

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

/**
 * Action to fetch any documents associated with the current query
 * information associated with this ImageViewerStore.
 */
function fetchImages(props, dispatch, state) {
  console.log("Fetch images", props, state, window.app);

  if (props.status === 'fetching') {
    console.log("Already fetching images");
    return;
  }

  dispatch((0, _status.setStatus)('fetching'));
  props.resetResult();
  var query = state.query;
  console.log("Query:", query);
  /*
  const findOptions = {
    sort: query.sort,
    fields: query.project,
    skip: query.skip,
    limit: Math.min(5, query.limit)
  };*/

  var findOptions = state.query;
  var ns = query.ns;
  var filter = query.filter || null;
  var service = state.dataService.dataService;
  console.log("DataService:", service, ns, filter, findOptions);
  service.find(ns, filter, findOptions, function (findError, documents) {
    console.log("Finding:", documents, findError);

    if (findError) {
      console.log("Find Error:", findError);
      dispatch({
        error: findError
      });
      return;
    }

    var urls = [];

    try {
      var _iteratorNormalCompletion = true;
      var _didIteratorError = false;
      var _iteratorError = undefined;

      try {
        for (var _iterator = documents[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
          var doc = _step.value;
          console.log("Document:", doc);
          var id = doc._id;
          var _iteratorNormalCompletion2 = true;
          var _didIteratorError2 = false;
          var _iteratorError2 = undefined;

          try {
            for (var _iterator2 = doc.fields[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
              var field = _step2.value;
              console.log("BSON:", _bson.default);
              console.log("Field:", field);

              if (field.data) {
                var type = field.data.type;
                var bin = field.data.content.buffer;
                var label = field.data.label;
                var blob = new Blob([bin], {
                  type: type
                });
                var url = URL.createObjectURL(blob);
                props.insertResult(id, [{
                  href: url,
                  type: type,
                  label: label
                }]);
              } else {
                console.log("No image for field");
              }
            }
          } catch (err) {
            _didIteratorError2 = true;
            _iteratorError2 = err;
          } finally {
            try {
              if (!_iteratorNormalCompletion2 && _iterator2.return != null) {
                _iterator2.return();
              }
            } finally {
              if (_didIteratorError2) {
                throw _iteratorError2;
              }
            }
          }
        }
      } catch (err) {
        _didIteratorError = true;
        _iteratorError = err;
      } finally {
        try {
          if (!_iteratorNormalCompletion && _iterator.return != null) {
            _iterator.return();
          }
        } finally {
          if (_didIteratorError) {
            throw _iteratorError;
          }
        }
      }
    } catch (error) {
      console.log("error:", error);
      dispatch({
        type: "ERROR",
        error: error
      });
    }

    dispatch((0, _status.setStatus)("done"));
  });
}

var requestImages = function requestImages(props) {
  return function (dispatch, getState) {
    console.log("retrieving images...");
    fetchImages(props, dispatch, getState()); // return(setStatus("fetching"))
  };
};

exports.requestImages = requestImages;