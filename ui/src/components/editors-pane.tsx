import React from 'react'
import { Col, Nav } from 'react-bootstrap'
import * as monaco from 'monaco-editor';
import { ApiClient } from '../services/api-client';
import { File, toVSCodeComplete } from '../models/index'
import * as R from 'ramda'

const language = "modelica"
interface Props {
  api: ApiClient
  files: File[]
}

function extractWord(model: monaco.editor.ITextModel, position: monaco.Position): string | undefined {
  const value = model.getValueInRange(new monaco.Range(position.lineNumber, 0, position.lineNumber, position.column))
  const matches = /[\w\.]+$/.exec(value)
  return (matches) ? R.last(matches) : undefined
}

export class EditorsPane extends React.Component<Props, any> {

  static editorName: string = "monaco-editor"
  static monacoEditor?: monaco.editor.ICodeEditor = undefined

  componentDidMount() {
    if (!EditorsPane.monacoEditor) {
      EditorsPane.monacoEditor = monaco.editor.create(document.getElementById(EditorsPane.editorName) as HTMLElement, {
        value: "your editor for modelica code!",
        language: language,
        glyphMargin: true
      })
      this.setupAutoComplete()
    }
  }

  private setupAutoComplete() {
    monaco.languages.registerCompletionItemProvider(language, {
      provideCompletionItems: (model: monaco.editor.ITextModel, position: monaco.Position) => {
        const wordAtCursor = extractWord(model, position)
        if(wordAtCursor) {
          const c = {
            file: this.props.files[0].relativePath,
            position: {line: position.lineNumber, column: position.column},
            word: wordAtCursor
          }
          console.log("word below cursor: ", wordAtCursor)
          return this.props.api
            .autocomplete(c)
            .then(sugs => {
              console.log("suggestions from backend are: ", sugs)
              return {
                suggestions: sugs.map(toVSCodeComplete)
              }
            })
        } else {
          return undefined
        }
      }
    })
  }

  render() {
    const tabSelected = (key: string) => console.log("editor tab selected:", key)
    const file = (this.props.files.length > 0) ? this.props.files[0] : undefined
    //only overwrite editor-content if it really changed; otherwise decorations would be removed
    if (EditorsPane.monacoEditor && file &&
      EditorsPane.monacoEditor.getValue() !== file.content) {
      const model = monaco.editor.createModel(file.content, "modelica")
      EditorsPane.monacoEditor.setModel(model)
    }

    let tabTitle = (file) ? file.relativePath : "welcome"

    return (<>
        <Nav className="justify-content-center" activeKey={tabTitle} onSelect={tabSelected}>
          <Nav.Link href={tabTitle}>{tabTitle}</Nav.Link>
        </Nav>
        <div id={EditorsPane.editorName} className="editor"></div>
      </>)
  }
}
