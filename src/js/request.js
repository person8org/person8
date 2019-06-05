import { setStatus } from 'modules/status'
import BSON from 'bson'
  /**
   * Action to fetch any documents associated with the current query
   * information associated with this ImageViewerStore.
   */

function fetchImages (props, dispatch, state) {
    console.log("Fetch images", props, state, window.app);
    if (props.status === 'fetching') {
      console.log("Already fetching images")
      return;
    }

    dispatch(setStatus('fetching'));
    props.resetResult();

    var query = state.query
    console.log("Query:", query)

    /*
    const findOptions = {
      sort: query.sort,
      fields: query.project,
      skip: query.skip,
      limit: Math.min(5, query.limit)
    };*/

    const findOptions = state.query

    var ns = query.ns
    var filter = query.filter || null

    var service = state.dataService.dataService
    console.log("DataService:", service, ns, filter, findOptions);
    service.find(ns, filter, findOptions,
                                (findError, documents) => {
      console.log("Finding:", documents, findError);
      if (findError) {
        console.log("Find Error:", findError);
        dispatch({error: findError});
        return;
      }

      const urls = [];
      try {
        for (const doc of documents) {
          console.log("Document:", doc)
          const id = doc._id
          for (const field of doc.fields) {
            console.log("BSON:", BSON)
            console.log("Field:", field)
            if (field.data) {
              const type = field.data.type
              const bin = field.data.content.buffer
              const label = field.data.label
              const blob = new Blob([bin], {type: type})
              const url = URL.createObjectURL(blob)
              props.insertResult(id, [{href: url, type, label}])
            } else {
              console.log("No image for field")
            }
          }
        }
      } catch (error) {
        console.log("error:", error)
        dispatch({type: "ERROR", error: error});
      }
      dispatch(setStatus("done"))
    });
  }

export const requestImages = (props) => (dispatch, getState) => {
        console.log("retrieving images...")
        fetchImages(props, dispatch, getState())
        // return(setStatus("fetching"))
      };
