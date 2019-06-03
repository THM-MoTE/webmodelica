import React from 'react'
import { Col, Nav } from 'react-bootstrap'
import * as monaco from 'monaco-editor';
import { ApiClient } from '../services/api-client';
import { File, toVSCodeComplete, Shortcut, asKeyBinding } from '../models/index'
import * as R from 'ramda'

const language = "modelica"
interface Props {
  api: ApiClient
  file?: File
  interactive?:boolean
  shortcuts?: Shortcut[]
}

function extractWord(model: monaco.editor.ITextModel, position: monaco.Position): string | undefined {
  const value = model.getValueInRange(new monaco.Range(position.lineNumber, 0, position.lineNumber, position.column))
  const matches = /[\w\.]+$/.exec(value)
  return (matches) ? R.last(matches) : undefined
}

/** the monaco editor wrapped inside of a react component. */
export class EditorsPane extends React.Component<Props, any> {

  static editorName: string = "monaco-editor"
  static monacoEditor?: monaco.editor.IStandaloneCodeEditor = undefined

  componentDidMount() {
    //make sure we have only 1 instance of MonacoEditor regardless of how many EditorsPane's are created
    if (!EditorsPane.monacoEditor) {
      EditorsPane.monacoEditor = monaco.editor.create(document.getElementById(EditorsPane.editorName) as HTMLElement, {
        value: "your editor for modelica code!",
        language: language,
        glyphMargin: true
      })
      if(this.props.interactive) {
        this.setupAutoComplete()
        this.setupShortcuts()
      }
    }
  }

  private setupShortcuts() {
    if(EditorsPane.monacoEditor) {
      for(let s of (this.props.shortcuts || [])) {
        EditorsPane.monacoEditor.addCommand(asKeyBinding(s), s.callback, "")
      }
    } else {
      console.warn("can't setup shortcuts because there is not monaco-editor defined!")
    }
  }

  private setupAutoComplete() {
    monaco.languages.registerCompletionItemProvider(language, {
      provideCompletionItems: (model: monaco.editor.ITextModel, position: monaco.Position) => {
        const wordAtCursor = extractWord(model, position)
        if(wordAtCursor) {
          const c = {
            file: this.props.file!.relativePath,
            position: {line: position.lineNumber, column: position.column},
            word: wordAtCursor
          }
          console.log("word below cursor: ", wordAtCursor)
          return this.props.api
            .autocomplete(c)
            .then(sugs => {
              console.log("suggestions from backend are: ", sugs)
              return {
                suggestions: sugs.map(s => toVSCodeComplete(wordAtCursor, s))
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
    const file = this.props.file
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
