import React from 'react'
import { Col, ListGroup, Nav } from 'react-bootstrap'
import { File } from '../models/index'

export class FileView extends React.Component<any, any> {

  constructor(props: any) {
    super(props)
    console.log("file-view-props", props)
  }

  render() {
    const files = this.props.files
    const fileClicked = this.props.onFileClicked
    return (
      <Col lg="2">
        <Nav className="flex-column border">
          <h5 className="text-secondary">Files</h5>
          {this.props.files.map((f: File) => <Nav.Link href="#">{f.relativePath}</Nav.Link>)}
        </Nav>
      </Col >
    )
  }
}
