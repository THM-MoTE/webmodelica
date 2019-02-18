import React from 'react'
import { Col, Row, ListGroup, Nav, Button, Modal, Form } from 'react-bootstrap'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import * as R from 'ramda'
import { File, AppState } from '../models/index'
import { newFile, Action } from '../redux/index'

class FileViewCon extends React.Component<any, any> {
  private newFilename?: string = undefined

  private readonly fileTypes = [
    "Model", "Function", "Connector", "Class"
  ]
  private selectedType: string = this.fileTypes[0]

  constructor(props: any) {
    super(props)
    this.state = { showNewFileDialog: false }
  }

  private createNewFile() {
    const extractModelname = (path: string) => {
      //the "model" name is the last part after / or . ;) (my/awesome/Class)
      const m = path.match(/[\.\/]?(\w+)$/)
      return (m) ? m[1] : ""
    }
    const createFilename = (path: string) => path.replace(/\./g, "/") + ".mo"

    if (this.selectedType && this.newFilename) {
      const suffixStripped = this.newFilename!.endsWith(".mo") ? this.newFilename!.substring(0, this.newFilename!.length - 3).trim() : this.newFilename!.trim()
      const tpe = this.selectedType!.toLowerCase()
      const name = extractModelname(suffixStripped)
      const path = createFilename(suffixStripped)
      const content = `${tpe} ${name}\nend ${name};`
      this.props.newFile({ relativePath: path, content: content })
    } else {
      console.error("can't create a new file without name & type!")
    }
  }

  private newFileDialog() {
    const handleClose = () => this.setState({ showNewFileDialog: false })
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
                {this.fileTypes.map(tpe => <option onClick={() => handleFileTypeSelect(tpe)}>{tpe}</option>)}
              </Form.Control>
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="success" type="submit" onClick={R.compose(handleClose, this.createNewFile.bind(this))}>Create</Button>
        </Modal.Footer>
      </Modal >)
  }


  render() {
    const files = this.props.files
    const fileClicked = this.props.onFileClicked
    const newFileClicked = () => {
      console.log("new file clicked")
      this.setState({ showNewFileDialog: true })
    }
    return (<>
      <Col lg="2">
        <Nav className="flex-column border">
          <h5 className="text-secondary">Files</h5>
          <Nav.Link href="#" onSelect={newFileClicked}>New File</Nav.Link>
          {this.props.files.map((f: File) => <Nav.Link href="#" onSelect={() => fileClicked(f)}>{f.relativePath}</Nav.Link>)}
        </Nav>
      </Col >
      {this.newFileDialog()}
    </>
    )
  }
}

function mapProps(state: AppState) {
  return { files: state.session!.files }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ newFile }, dispatch)
}
const FileView = connect(mapProps, dispatchToProps)(FileViewCon)
export default FileView
