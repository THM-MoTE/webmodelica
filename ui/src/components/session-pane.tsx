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
import { File, FileNode, AppState, CompilerError, Session, Shortcut, cmdShiftAnd } from '../models/index'
import { Action, setCompilerErrors, setSessionFiles, notifyInfo } from '../redux/actions'
import * as monaco from 'monaco-editor';
import * as R from 'ramda'
import { renderErrors } from '../partials/errors';
import { LoadingSpinner } from '../partials/loading-spinner';

interface State {
  editingFiles: File[]
  deltaMarkers: any[]
  compiling:boolean
}
interface Props {
  api: ApiClient
  session: Session
  compilerErrors: CompilerError[]
  setSessionFiles(f: FileNode): Action
  setCompilerErrors(ers: CompilerError[]): void
  notifyInfo(msg:string):void
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
    this.state = { editingFiles: [], deltaMarkers: [], compiling: false }
  }

  private setupShortcuts(): Shortcut[] {
    return [
      cmdShiftAnd(monaco.KeyCode.KEY_S, this.handleSaveClicked.bind(this)),
      cmdShiftAnd(monaco.KeyCode.KEY_B, this.handleCompileClicked.bind(this))
    ]
  }

  private handleFileClicked(filePath: File): void {
    this.saveCurrentFiles()
      .then(() => this.api.getFile(filePath.relativePath))
      .then(file => this.setState({ editingFiles: [file] }))
  }

  private currentFile(): File {
    return this.state.editingFiles[0]
  }

  private saveCurrentFiles(): Promise<File[]> {
    let content = EditorsPane.monacoEditor!.getValue()
    let files: File[] = this.currentFile() ? [{ ...this.currentFile(), content: content }] : []
    const updatePromises = files.map((f: File) => this.api.updateFile(f))
    return Promise.all(updatePromises)
      .then((files) => {
        // this.api.projectFileTree(this.props.session.project.id)
        //   .then(this.props.setSessionFiles)
      return files
    })
  }

  handleSaveClicked(): Promise<File[]> {
    return this.saveCurrentFiles()
      .then((files) => {
        this.setState({ editingFiles: files })
        this.props.notifyInfo("all files saved!")
        return files
      })
  }
  handleCompileClicked() {
    console.log("compiling .. ")
    this.setState({compiling: true})
    this.handleSaveClicked()
      .then(files => this.api.compile(this.currentFile()))
      .then(errors => {
        console.log("errors:", errors)
        if(R.isEmpty(errors)) {
          this.props.notifyInfo("compilation success!")
        }
        const newMarkers = this.markErrors(this.state.deltaMarkers, errors)
        this.props.setCompilerErrors(errors)
        this.setState({ deltaMarkers: newMarkers, compiling: false })
      })
  }

  markErrors(oldMarkers: string[], errors: CompilerError[]): string[] {
    console.log("old markers ", oldMarkers)
    const decos = deltaDecorations(this.currentFile(), errors)
    return EditorsPane.monacoEditor!.deltaDecorations(oldMarkers, decos)
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
        <Row className="editor-row">
          <Col sm="2">
            <FileView
              onSaveClicked={this.handleSaveClicked.bind(this)}
              onCompileClicked={this.handleCompileClicked.bind(this)}
              onFileClicked={(f: File) => this.handleFileClicked(f)}
              api={this.api}
              activeFile={this.state.editingFiles[0] } />
          </Col>
          <Col sm={10}>
          <EditorsPane
            file={(this.state.editingFiles.length>0) ? this.state.editingFiles[0] : undefined}
            api={this.props.api}
            interactive
            shortcuts={this.setupShortcuts()}/>
          </Col>
        </Row>
        <Row>
          <Col lg="2"></Col>
          <Col>
            {this.props.compilerErrors.map(errorLine)}
          </Col>
        </Row>
        <LoadingSpinner msg={"compiling be patient.."} display={this.state.compiling} />
      </WmContainer>
    )
  }
}

function mapProps(state: AppState) {
  return { session: state.session, compilerErrors: state.session!.compilerErrors }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ setCompilerErrors, setSessionFiles, notifyInfo }, dispatch)
}

const SessionPane = connect(mapProps, dispatchToProps)(SessionPaneCon)
export default SessionPane
