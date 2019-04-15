import React from 'react'
import { Col, Nav } from 'react-bootstrap'
import * as monaco from 'monaco-editor';

const language = "modelica"

export class EditorsPane extends React.Component<any, any> {

  static editorName: string = "monaco-editor"
  static monacoEditor: any = undefined

  constructor(props: any) {
    super(props)
  }

  componentDidMount() {
    if (!EditorsPane.monacoEditor) {
      EditorsPane.monacoEditor = monaco.editor.create(document.getElementById(EditorsPane.editorName) as HTMLElement, {
        value: "your editor for modelica code!",
        language: language,
        glyphMargin: true
      })
      this.setupAutoComplete()
    } else {
    }
  }

  private setupAutoComplete() {
    const suggestions:monaco.languages.CompletionItem[] = [
      {label: 'model', kind: monaco.languages.CompletionItemKind.Keyword, insertText: 'model'},
      {label: 'class', kind: monaco.languages.CompletionItemKind.Keyword, insertText: 'class'},
      {label: 'annotation', kind: monaco.languages.CompletionItemKind.Keyword, insertText: 'annotation'},
      {label: 'Modelica', kind: monaco.languages.CompletionItemKind.Folder, insertText: 'Modelica'},
      { label: 'BouncingBall', kind: monaco.languages.CompletionItemKind.Class, insertText: 'BouncingBall'},
    ]
    monaco.languages.registerCompletionItemProvider(language, {
      provideCompletionItems: (model: monaco.editor.ITextModel, position: monaco.Position) => {
        return {
          suggestions: suggestions
        }
      }
    })
  }

  render() {
    const tabSelected = (key: string) => console.log("editor tab selected:", key)
    const file = this.props.files.length > 0 && this.props.files[0]
    //only overwrite editor-content if it really changed; otherwise decorations would be removed
    if (EditorsPane.monacoEditor && file &&
      EditorsPane.monacoEditor.getValue() !== file.content) {
      const model = monaco.editor.createModel(file.content, "modelica")
      EditorsPane.monacoEditor.setModel(model)
    }

    let tabTitle = file.relativePath || "welcome"

    return (<>
        <Nav className="justify-content-center" activeKey={tabTitle} onSelect={tabSelected}>
          <Nav.Link href={tabTitle}>{tabTitle}</Nav.Link>
        </Nav>
        <div id={EditorsPane.editorName} className="editor"></div>
      </>)
  }
}
