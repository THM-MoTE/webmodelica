import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import * as serviceWorker from './serviceWorker';
import * as monaco from 'monaco-editor';
import { languageName, tokenizer } from './models/modelica-syntax';

monaco.languages.register({ id: languageName() });
monaco.languages.setMonarchTokensProvider(languageName(), tokenizer())

ReactDOM.render(<App />, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: http://bit.ly/CRA-PWA
serviceWorker.unregister();
