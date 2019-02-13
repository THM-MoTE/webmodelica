import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import { BrowserRouter as Router, Route } from 'react-router-dom'
import { CodeEditor, Landing, ProjectView, SessionPane } from './components'
import { ApiClient } from './services/api-client'

class App extends Component {
  private apiClient = new ApiClient(location.toString())
  render() {
    return (
      <Router>
        <div>
          <Route exact path="/editor" component={CodeEditor} />
          <Route exact path="/projects" render={({ history }) => <ProjectView history={history} api={this.apiClient} />} />
          <Route exact path="/" render={({ history }) => <Landing history={history} api={this.apiClient} />} />
          <Route path="/session/:sessionId" render={({ history, match }) => <SessionPane history={history} sessionId={match.params.sessionId} api={this.apiClient} />} />
        </div>
      </Router>
    )
  }
}

export default App;
