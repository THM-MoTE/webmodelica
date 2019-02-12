import React, { Component } from 'react';
import * as monaco from 'monaco-editor';

export class CodeEditor extends Component {

  private file: string
  private containerName: string
  private monacoEditor: any = undefined

  constructor(props:any) {
    super(props)
    this.file = "example.mo"
    this.containerName = `editor-${this.file}`
  }
  componentDidMount() {
    if(!this.monacoEditor) {
      this.monacoEditor =  monaco.editor.create(document.getElementById(this.containerName) as HTMLElement, {
        value: "// First line\nfunction hello() {\n\talert('Hello world!');\n}\n// Last line",
        language: "typescript"
      })
    }
  }

  render() {
    return (
      <Container>
        <div id={this.containerName} className="editor"></div>
      </Container>
    )
  }
}
