import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import {BrowserRouter as Router, Route} from 'react-router-dom'
import {CodeEditor, Landing, ProjectView} from './components'

class App extends Component {
  render() {
    return (
      <Router>
        <div>
          <Route exact path="/editor" component={CodeEditor} />
          <Route exact path="/project-view" component={ProjectView} />
          <Route exact path="/" component={Landing} />
        </div>
      </Router>
    )
  }
}

export default App;
