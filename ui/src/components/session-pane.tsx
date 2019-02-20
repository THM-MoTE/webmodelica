import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { Container } from '../layouts'
import { FileView } from './index'
import { EditorsPane } from './editors-pane'
import { ApiClient } from '../services/api-client'
import { Row, Col, Button, ButtonGroup, Container as RContainer, Card } from 'react-bootstrap'
import { File, AppState, CompilerError } from '../models/index'
import { Action, updateSessionFiles } from '../redux/actions'
import * as monaco from 'monaco-editor';

interface State {
  editingFiles: File[]
  compilerErrors: CompilerError[]
}
interface Props {
  api: ApiClient
  updateSessionFiles(f: File[]): void
}

function lineLength(f: File, lNo: number): number {
  const line = f.content.split("\n")[lNo - 1]
  return line.length
}

class SessionPaneCon extends React.Component<Props, State> {
  private readonly api: ApiClient

  constructor(props: any) {
    super(props)
    this.api = this.props.api
    this.state = { editingFiles: [], compilerErrors: [] }
  }

  public componentDidMount() {
  }

  private handleFileClicked(f: File): void {
    this.setState({ editingFiles: [f], compilerErrors: [] })
  }

  private currentFile(): File {
    return this.state.editingFiles[0]
  }
  handleSaveClicked() {
    let content = EditorsPane.monacoEditor.getValue()
    let files: File[] = [{ ...this.currentFile(), content: content }]
    const updatePromises = files.map((f: File) => this.api.updateFile(f))
    Promise.all(updatePromises).then(fs => this.props.updateSessionFiles(fs))
  }
  handleCompileClicked() {
    this.api.compile(this.currentFile())
      .then(errors => {
        console.log("errors:", errors)
        let currentFileErrors = errors.filter(e => e.file == this.currentFile().relativePath)
        let decos: monaco.editor.IModelDeltaDecoration[] = currentFileErrors.map(e => ({
          //TODO: possible wrong index for lineNo .. does MoPE return 0-based or 1-baed lineNumbers???
          range: new monaco.Range(2, 1, 2, lineLength(this.currentFile(), e.start.line)),
          options: {
            isWholeLine: true,
            className: 'myContentClass',
            glyphMarginClassName: 'myGlyphMarginClass',
            hoverMessage: { value: e.type.toUpperCase() + ": " + e.message }
          }
        }))
        EditorsPane.monacoEditor.deltaDecorations([], decos)
        this.setState({ ...this.state, compilerErrors: errors })
      })
  }

  render() {
    const errorLine = (e: CompilerError) => (
      <div key={e.file + "-" + e.start}>
        <p><span>{e.type.toUpperCase()} </span>
          <span>L:{e.start.line} </span>
          <span>{e.message} </span>
          <span>({e.file})</span>
        </p>
      </div>)
    return (
      <Container>
        <Row>
          <Col sm="2">
            <FileView
              onSaveClicked={this.handleSaveClicked.bind(this)}
              onCompileClicked={this.handleCompileClicked.bind(this)}
              onFileClicked={(f: File) => this.handleFileClicked(f)}
              api={this.api} />
          </Col>
          <EditorsPane
            files={this.state.editingFiles} />
        </Row>
        <Row>
          <Col lg="2"></Col>
          <Col>
            {this.state.compilerErrors.map(errorLine)}
          </Col>
        </Row>
      </Container>
    )
  }
}

function mapProps(state: AppState) {
  return { session: state.session }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ updateSessionFiles }, dispatch)
}

const SessionPane = connect(mapProps, dispatchToProps)(SessionPaneCon)
export default SessionPane
