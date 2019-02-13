import React from 'react'
import { Col, ListGroup } from 'react-bootstrap'
import { File } from '../models/file'

export class FileView extends React.Component<any, any> {

  constructor(props: any) {
    super(props)
    console.log("file-view-props", props)
  }

  render() {
    const files = this.props.files
    return (
      <Col lg="2">
        <ListGroup variant="flush">
          {files.map((f: File) =>
            <ListGroup.Item key={f.relativePath}>{f.relativePath}</ListGroup.Item>)
          }
        </ListGroup>
      </Col>
    )
  }
}
