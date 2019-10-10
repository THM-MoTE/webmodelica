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
import { ApiError, File, FileNode, AppState, CompilerError, Session, Shortcut, cmdShiftAnd } from '../models/index'
import { Action, setCompilerErrors, setSessionFiles, notifyInfo, notifyWarning, notifyError,setOpenFile } from '../redux/actions'
import * as monaco from 'monaco-editor';
import * as R from 'ramda'
import { renderErrors } from '../partials/errors';
import { LoadingSpinner } from '../partials/loading-spinner';

interface State {
  deltaMarkers: any[]
  compiling:boolean
}
interface Props {
  api: ApiClient
  session: Session
  compilerErrors: CompilerError[]
  openedFile?: File
  setOpenFile(f:File): Action
  setSessionFiles(f: FileNode): Action
  setCompilerErrors(ers: CompilerError[]): void
  notifyInfo(msg:string):void
  notifyWarning(msg:string):void
  notifyError(msg:string):void
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


/** The session pane, displayed at `session/:sessionId` */
class SessionPaneCon extends React.Component<Props, State> {
  private readonly api: ApiClient

  constructor(props: any) {
    super(props)
    this.api = this.props.api
    this.state = { deltaMarkers: [], compiling: false }
    EditorsPane.killEditorInstance() //to prevent old monaco editor instances
  }

  private setupShortcuts(): Shortcut[] {
    return [
      cmdShiftAnd(monaco.KeyCode.KEY_S, this.handleSaveClicked.bind(this)),
      cmdShiftAnd(monaco.KeyCode.KEY_B, this.handleCompileClicked.bind(this))
    ]
  }

  private handleFileClicked(filePath: File): void {
    this.saveCurrentFiles()
        //ignore errors and fetch current file
      .then(() => this.api.getFile(this.props.session.project, filePath.relativePath),
        () => this.api.getFile(this.props.session.project, filePath.relativePath))
      .then(file => {
        this.props.setOpenFile(file)
        //when opening a new file; display error markers for the new file
        this.markErrors(this.state.deltaMarkers, this.props.compilerErrors)
      })
      .catch((er: ApiError) => this.props.notifyError(`Couldn't open file: ${er.statusText}`))
  }

  private currentFile(): File|undefined {
    return this.props.openedFile
  }

  private saveCurrentFiles(): Promise<File> {
    let content = EditorsPane.monacoEditor!.getValue()
    const curFile = this.currentFile()
    if(curFile) {
      return this.api.updateFile({...curFile, content: content})
    } else {
      return Promise.reject('no opened file!')
    }
  }

  handleSaveClicked(): Promise<File> {
    return this.saveCurrentFiles()
      .then(file => {
        this.props.setOpenFile(file)
        this.props.notifyInfo("all files saved!")
        return file
      })
  }
  handleCompileClicked() {
    if(!this.currentFile()) {
      this.props.notifyWarning("Please open a file before compilation!");
    } else {
      this.handleSaveClicked()
        .catch(er => /** no open file; ignore error */ undefined)
        .then(file => {this.setState({ compiling: true }); return file!})
        .then(file => this.api.compile(file))
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
  }

  markErrors(oldMarkers: string[], errors: CompilerError[]): string[] {
    console.log("old markers ", oldMarkers)
    const decos = deltaDecorations(this.currentFile()!, errors)
    return EditorsPane.monacoEditor!.deltaDecorations(oldMarkers, decos)
  }

  render() {
    const fileClicked = R.curry((path:string, ev:any) => {
      ev.preventDefault()
      this.handleFileClicked({relativePath: path, content: ""})
    })
    const errorLine = (e: CompilerError) => (
      <div key={e.file + "-" + e.start}>
        <p><span className="errorGlyph"><Octicon name="alert" /> {e.type.toUpperCase()} </span>
          <span>L:{e.start.line} </span>
          <span>{e.message} </span>
          <span>(<a href="#" onClick={fileClicked(e.file).bind(this)}>{e.file}</a>)</span>
        </p>
      </div>)
    return (
      <WmContainer title={"Session: " + this.props.session.project.name} active="session" sessionId={this.props.session.id}>
        <Row className="editor-row">
          <Col md="2">
            <FileView
              onSaveClicked={this.handleSaveClicked.bind(this)}
              onCompileClicked={this.handleCompileClicked.bind(this)}
              onFileClicked={(f: File) => this.handleFileClicked(f)}
              api={this.api}
              activeFile={this.currentFile()} />
          </Col>
          <Col md={10}>
          <EditorsPane
            file={(this.currentFile()) ? this.currentFile() : undefined}
            api={this.props.api}
            interactive
            shortcuts={this.setupShortcuts()}/>
          </Col>
        </Row>
        <Row>
          <Col md="2"></Col>
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
  return {
    session: state.session,
    compilerErrors: state.session!.compilerErrors,
    openedFile: state.session!.openedFile
  }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ setCompilerErrors, setSessionFiles, setOpenFile, notifyInfo, notifyWarning, notifyError }, dispatch)
}

const SessionPane = connect(mapProps, dispatchToProps)(SessionPaneCon)
export default SessionPane
