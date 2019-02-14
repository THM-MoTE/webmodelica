import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import { BrowserRouter as Router, Route } from 'react-router-dom'
import {CodeEditor} from './components/editor'
import ProjectView from './components/project-view'
import {SessionPane} from './components/session-pane'
import Landing from './components/landing'
import { ApiClient } from './services/api-client'
import {rootReducer} from './redux/reducers'
import { createStore } from "redux";
import {Provider} from "react-redux";
import {login} from './redux/actions'

const store = createStore(rootReducer)
store.subscribe(() => console.log("state changed:", store.getState()))

class App extends Component {
//  private apiClient = new ApiClient(location.toString())
  render() {
    return (
      <Provider store={store}>
        <Router>
          <div>
            <Route exact path="/editor" component={CodeEditor} />
            <Route exact path="/projects" component={ProjectView} />
            <Route exact path="/" component={Landing} />
            <Route exact path="/logout" component={Landing} />
            <Route path="/session/:sessionId" component={SessionPane} />
          </div>
        </Router>

      </Provider>

    )
  }
}

export default App;
