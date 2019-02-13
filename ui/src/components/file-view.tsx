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
    const fileClicked = this.props.onFileClicked
    return (
      <Col lg="2">
        <ListGroup variant="flush">
          {files.map((f: File) =>
            <ListGroup.Item action
              key={f.relativePath}
              onClick={(ev: any) => fileClicked(f)}>
              {f.relativePath}
            </ListGroup.Item>)
          }
        </ListGroup>
      </Col >
    )
  }
}
