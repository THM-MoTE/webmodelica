import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import { BrowserRouter as Router, Route } from 'react-router-dom'
import { ProjectView, Landing, SessionPane } from './components/index'
import { ApiClient } from './services/api-client'
import { rootReducer } from './redux/reducers'
import { createStore } from "redux";
import { Provider } from "react-redux";
import { login } from './redux/actions'
import { withApi } from './partials/api-wrapper';
import { AppState } from './models';


const stateKey = "wm-redux-State"
const store = createStore(rootReducer, persistedStore())
const client = new ApiClient(store)
/** on every state-update, update the localStorage.
 * TODO: this is really inefficient.
 */
store.subscribe(() => {
  const state = store.getState()
  console.log("state changed:", state)
  localStorage.setItem(stateKey, JSON.stringify(state))
})

/** read state from localStorage.  */
function persistedStore() {
  const item = localStorage.getItem(stateKey)
  return (item) ? JSON.parse(item) : undefined
}
/** Performs logout by clearing localStorage and reloading base path.*/
function destroySession() {
  localStorage.clear()
  window.location.href = "/"
  return (<span>logout..</span>)
}

class App extends Component {
  render() {
    return (
      <Provider store={store}>
        <Router>
          <div>
            <Route exact path="/projects" component={withApi(client, ProjectView)} />
            <Route exact path="/" component={withApi(client, Landing)} />
            <Route exact path="/logout" render={() => destroySession()} />
            <Route path="/session/:sessionId" component={withApi(client, SessionPane)} />
          </div>
        </Router>
      </Provider>
    )
  }
}

export default App;
