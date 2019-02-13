import React from 'react'
import { Col } from 'react-bootstrap'
import { CodeEditor } from './editor'

export class EditorsPane extends React.Component<any, any> {
  constructor(props: any) {
    super(props)
    console.log("EditorsPane: opened files", this.props.files)
  }

  render() {
    const files = this.props.files
    return (
      <Col>
        {files.length > 0 &&
          <CodeEditor file={files[0]} />
        }
      </Col>
    )
  }
}
