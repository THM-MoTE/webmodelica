import React from 'react'
import { Col } from 'react-bootstrap'
import * as monaco from 'monaco-editor';

export class EditorsPane extends React.Component<any, any> {

  static editorName: string = "monaco-editor"
  static monacoEditor: any = undefined

  constructor(props: any) {
    super(props)
    console.log("EditorsPane: opened files", this.props.files)
  }

  componentDidMount() {
    console.log("files", this.props.files)

    if(!EditorsPane.monacoEditor) {
      EditorsPane.monacoEditor = monaco.editor.create(document.getElementById(EditorsPane.editorName) as HTMLElement, {
        value: "your editor for modelica code!",
        language: "modelica"
      })
    } else {
    }
  }

  render() {
    const file = this.props.files.length>0 && this.props.files[0]
    if(EditorsPane.monacoEditor && file) {
      const model = monaco.editor.createModel(file.content, "modelica")
      EditorsPane.monacoEditor.setModel(model)
    }
    return (
      <Col>
        <div id={EditorsPane.editorName} className="editor"></div>
      </Col>
    )
  }
}
