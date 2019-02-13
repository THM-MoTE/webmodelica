import React, { Component } from 'react';
import * as monaco from 'monaco-editor';
import { Container } from '../layouts'
import { File } from '../models/file'

export class CodeEditor extends Component<any, any> {

  private monacoEditor: any = undefined
  private containerName: string
  private file: File

  constructor(props: any) {
    super(props)
    this.file = this.props.file
    this.containerName = `editor-${this.file.relativePath}`
  }

  componentDidMount() {
    if (!this.monacoEditor) {
      this.monacoEditor = monaco.editor.create(document.getElementById(this.containerName) as HTMLElement, {
        value: this.file.content,
        language: "modelica"
      })
    }
  }

  render() {
    return (
      <div id={this.containerName} className="editor"></div>
    )
  }
}
