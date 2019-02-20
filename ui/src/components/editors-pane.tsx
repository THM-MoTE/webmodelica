import React from 'react'
import { Col, Nav } from 'react-bootstrap'
import * as monaco from 'monaco-editor';

export class EditorsPane extends React.Component<any, any> {

  static editorName: string = "monaco-editor"
  static monacoEditor: any = undefined

  constructor(props: any) {
    super(props)
  }

  componentDidMount() {
    // console.log("files", this.props.files)

    if (!EditorsPane.monacoEditor) {
      EditorsPane.monacoEditor = monaco.editor.create(document.getElementById(EditorsPane.editorName) as HTMLElement, {
        value: "your editor for modelica code!",
        language: "modelica"
      })
    } else {
    }
  }

  render() {
    const tabSelected = (key: string) => console.log("editor tab selected:", key)
    const file = this.props.files.length > 0 && this.props.files[0]
    if (EditorsPane.monacoEditor && file) {
      const model = monaco.editor.createModel(file.content, "modelica")
      EditorsPane.monacoEditor.setModel(model)
    }

    let tabTitle = file.relativePath || "welcome"

    return (
      <Col>
        <Nav className="justify-content-center" activeKey={tabTitle} onSelect={tabSelected}>
          <Nav.Link href={tabTitle}>{tabTitle}</Nav.Link>
        </Nav>
        <div id={EditorsPane.editorName} className="editor"></div>
      </Col>
    )
  }
}
