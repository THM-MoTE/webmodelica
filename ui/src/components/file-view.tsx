import React from 'react'
import { Col, Row, ListGroup, Nav, Button, Modal, Form, Alert, Badge } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import * as R from 'ramda'
import { File, AppState, CompilerError } from '../models/index'
import { newFile, Action } from '../redux/index'
import { ApiClient } from '../services/api-client';
import { renderErrors } from '../partials/errors'

interface Props {
  api: ApiClient
  files: File[]
  compilerErrors: CompilerError[]
  newFile(f: File): Action
  onFileClicked(f: File): void
  onSaveClicked(): void
  onCompileClicked(): void
}

interface State {
  showNewFileDialog: boolean
  errors: string[]
}

class FileViewCon extends React.Component<Props, State> {
  private newFilename?: string = undefined

  private readonly api: ApiClient
  private readonly fileTypes = [
    "Model", "Function", "Connector", "Class"
  ]
  private selectedType: string = this.fileTypes[0]

  constructor(props: any) {
    super(props)
    this.api = this.props.api
    this.state = { showNewFileDialog: false, errors: [] }
  }

  private updateErrors(err: string[]) {
    this.setState({ showNewFileDialog: !R.isEmpty(err), errors: err })
  }
  private fileExists(name: string): Boolean {
    const paths = this.props.files.map(f => f.relativePath)
    return R.any(p => p == name, paths)
  }

  private createNewFile() {
    const extractModelname = (path: string) => {
      //the "model" name is the last part after / or . ;) (my/awesome/Class)
      const m = path.match(/[\.\/]?(\w+)$/)
      return (m) ? m[1] : ""
    }
    const createFilename = (path: string) => path.replace(/\./g, "/") + ".mo"

    if (this.selectedType && this.newFilename && !R.contains(" ", this.newFilename) && !this.fileExists(this.newFilename)) {
      const suffixStripped = this.newFilename!.endsWith(".mo") ? this.newFilename!.substring(0, this.newFilename!.length - 3).trim() : this.newFilename!.trim()
      const tpe = this.selectedType!.toLowerCase()
      const name = extractModelname(suffixStripped)
      const path = createFilename(suffixStripped)
      const content = `${tpe} ${name}\nend ${name};`
      this.api
        .updateFile({ relativePath: path, content: content })
        .then(this.props.newFile)
        .then(() => this.updateErrors([]))
        .catch(er => this.updateErrors(["Creation failed because of: " + er]))
    } else {
      this.updateErrors([
        "The file needs a type and a name!",
        "The filename can't contain spaces!",
        "The file shouldn't already exists!"])
    }
  }

  private deleteFile(ev:any, f:File) {
    ev.preventDefault()
    this.props.api.deleteFile(f)
      .then(() => console.log("deleted: ", f.relativePath))
  }

  private newFileDialog() {
    const handleClose = () => {
      console.log("close: ", this.state)
      if (R.isEmpty(this.state.errors))
        this.setState({ showNewFileDialog: false })
    }
    const handleFilenameChange = (ev: any) => this.newFilename = ev.target.value
    const handleFileTypeSelect = (tpe: string) => this.selectedType = tpe

    return (
      <Modal show={this.state.showNewFileDialog} onHide={handleClose}>
        <Modal.Header closeButton>
          <Modal.Title>Create a new file</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group>
              <Form.Label>Filename</Form.Label>
              <Form.Control type="text" size="lg" placeholder="my.package.awesome-file.mo" onChange={handleFilenameChange} />
            </Form.Group>
            <Form.Group controlId="formModeltype">
              <Form.Label>Type</Form.Label>
              <Form.Control as="select">
                {this.fileTypes.map(tpe => <option key={tpe} onClick={() => handleFileTypeSelect(tpe)}>{tpe}</option>)}
              </Form.Control>
            </Form.Group>
          </Form>
          {renderErrors(this.state.errors)}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="success" type="submit" onClick={this.createNewFile.bind(this)}>Create</Button>
        </Modal.Footer>
      </Modal >)
  }

  render() {
    const files = this.props.files
    const fileClicked = this.props.onFileClicked
    const errorsInFile = (f: File) => this.props.compilerErrors.filter(e => e.file == f.relativePath)
    const newFileClicked = () => {
      this.setState({ showNewFileDialog: true })
    }
    return (<>
      <Nav className="flex-column border">
        <h5 className="text-secondary">Actions</h5>
        <Button variant="outline-success" onClick={this.props.onSaveClicked}><Octicon name="check" /> Save</Button>
        <Button variant="outline-primary" onClick={newFileClicked}>
          <Octicon name="plus" /> New File
            </Button>
        <Button variant="outline-primary" onClick={this.props.onCompileClicked}>
          <Octicon name="gear" /> Compile</Button>
        <h5 className="text-secondary">Files</h5>
        {this.props.files.map((f: File) =>
          <Nav.Link href="#" key={f.relativePath} onSelect={() => fileClicked(f)}>
            <Octicon name="file-code" /> {f.relativePath + "  "}
            {errorsInFile(f).length != 0 &&
              (<Badge variant="danger">{errorsInFile(f).length}</Badge>)
            }
          </Nav.Link>
        )}
      </Nav>
      {this.newFileDialog()}
    </>
    )
  }
}

function mapProps(state: AppState) {
  return { files: state.session!.files, compilerErrors: state.session!.compilerErrors }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ newFile }, dispatch)
}
const FileView = connect(mapProps, dispatchToProps)(FileViewCon)
export default FileView
