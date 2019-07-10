import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import { BrowserRouter as Router, Switch, Route, Redirect } from 'react-router-dom'
import { ProjectView, Landing, SessionPane, SimulationPane, ProjectPreview, NotFound } from './components/index'
import { ApiClient } from './services/api-client'
import { rootReducer } from './redux/reducers'
//@ts-ignore
import { cookies } from 'brownies';
import { createStore } from "redux";
import { Provider } from "react-redux";
import { withApi } from './partials/api-wrapper';
import { AppState, userIsAuthenticated } from './models/index';

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
  const state = (item) ? JSON.parse(item) : undefined
  return (state && userIsAuthenticated(state.authentication)) ? state : undefined
}
/** Performs logout by clearing localStorage and reloading base path.*/
export function destroySession() {
  localStorage.clear()
  delete cookies.token
  window.location.replace("/")
  return (<span>logging out ...</span>)
}

function AuthenticatedRoute(obj: any) {
  return (
    <Route
      {...obj.rest}
      render={props =>
        (userIsAuthenticated(store.getState().authentication)) ?
          <obj.component {...props} /> :
          <Redirect to="/" />
      }
      />
  )
}

/** Main application wiring that setups ReactRouter and the redux store. */
class App extends Component {
  render() {
    return (
      <Provider store={store}>
        <Router>
          <Switch>
            <Route exact path="/" component={withApi(client, Landing)} />
            <Route exact path="/logout" render={() => destroySession()} />
            <AuthenticatedRoute exact path="/projects" component={withApi(client, ProjectView)} />
            <AuthenticatedRoute exact path="/projects/:projectId/preview" component={withApi(client, ProjectPreview)} />
            <AuthenticatedRoute exact path="/session/:sessionId/simulate" component={withApi(client, SimulationPane)} />
            <AuthenticatedRoute exact path="/session/:sessionId" component={withApi(client, SessionPane)} />
            <Route component={NotFound} />
          </Switch>
        </Router>
      </Provider>
    )
  }
}

export default App;
