import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { WmContainer } from '../partials/container'
import { FileView } from './index'
import { EditorsPane } from './editors-pane'
import { ApiClient } from '../services/api-client'
import { Row, Col, Button, ButtonGroup, Container as RContainer, Card } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { File, AppState, CompilerError, Session } from '../models/index'
import { Action, updateSessionFiles, setCompilerErrors } from '../redux/actions'
import * as monaco from 'monaco-editor';
import { renderErrors } from '../partials/errors';
import * as R from 'ramda';

interface State {
  editingFiles: File[]
  deltaMarkers: any[]
}
interface Props {
  api: ApiClient
  session: Session
  compilerErrors: CompilerError[]
  updateSessionFiles(f: File[]): void
  setCompilerErrors(ers: CompilerError[]): void
  history: History
}

function lineLength(f: File, lNo: number): number {
  const line = f.content.split("\n")[lNo - 1]
  return line.length
}

function deltaDecorations(openedFile: File, errors: CompilerError[]): monaco.editor.IModelDeltaDecoration[] {
  return errors
    .filter(e => e.file == openedFile.relativePath)
    .map(e => ({
      //TODO: possible wrong index for lineNo .. does MoPE return 0-based or 1-baed lineNumbers???
      range: new monaco.Range(e.start.line, 1, e.end.line, e.end.column),
      options: {
        isWholeLine: true,
        className: 'contentClass',
        glyphMarginClassName: 'errorGlyph',
        hoverMessage: { value: e.type.toUpperCase() + ": " + e.message }
      }
    }))
}

class SessionPaneCon extends React.Component<Props, State> {
  private readonly api: ApiClient

  constructor(props: any) {
    super(props)
    this.api = this.props.api
    this.state = { editingFiles: [], deltaMarkers: [] }
  }

  public componentDidMount() {
  }

  private handleFileClicked(f: File): void {
    this.saveCurrentFiles().then(() => this.setState({ editingFiles: [f] }))
  }

  private currentFile(): File {
    return this.state.editingFiles[0]
  }

  private saveCurrentFiles(): Promise<File[]> {
    let content = EditorsPane.monacoEditor.getValue()
    let files: File[] = this.currentFile() ? [{ ...this.currentFile(), content: content }] : []
    const updatePromises = files.map((f: File) => this.api.updateFile(f))
    return Promise.all(updatePromises).then((files) => {
      this.props.updateSessionFiles(files)
      return files
    })
  }

  handleSaveClicked(): Promise<File[]> {
    return this.saveCurrentFiles()
      .then((files) => {
        this.setState({ editingFiles: files })
        return files
      })
  }
  handleCompileClicked() {
    console.log("compiling .. ")
    this.handleSaveClicked()
      .then(files => this.api.compile(this.currentFile()))
      .then(errors => {
        console.log("errors:", errors)
        const newMarkers = this.markErrors(this.state.deltaMarkers, errors)
        this.props.setCompilerErrors(errors)
        this.setState({ deltaMarkers: newMarkers })
      })
  }

  markErrors(oldMarkers: string[], errors: CompilerError[]): string[] {
    console.log("old markers ", oldMarkers)
    const decos = deltaDecorations(this.currentFile(), errors)
    return EditorsPane.monacoEditor.deltaDecorations(oldMarkers, decos)
  }

  render() {
    const errorLine = (e: CompilerError) => (
      <div key={e.file + "-" + e.start}>
        <p><span className="errorGlyph"><Octicon name="alert" /> {e.type.toUpperCase()} </span>
          <span>L:{e.start.line} </span>
          <span>{e.message} </span>
          <span>({e.file})</span>
        </p>
      </div>)
    return (
      <WmContainer title={"Session: " + this.props.session.project.name} active="session" sessionId={this.props.session.id}>
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
            {this.props.compilerErrors.map(errorLine)}
          </Col>
        </Row>
      </WmContainer>
    )
  }
}

function mapProps(state: AppState) {
  return { session: state.session, compilerErrors: state.session!.compilerErrors }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ updateSessionFiles, setCompilerErrors }, dispatch)
}

const SessionPane = connect(mapProps, dispatchToProps)(SessionPaneCon)
export default SessionPane
