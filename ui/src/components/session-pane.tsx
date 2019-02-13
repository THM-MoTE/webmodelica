import React from 'react';
import { Container } from '../layouts'
import { FileView } from './file-view'
import { EditorsPane } from './editors-pane'
import { ApiClient } from '../services/api-client'
import { Row } from 'react-bootstrap'

export class SessionPane extends React.Component<any, any> {
  private api: ApiClient

  constructor(props: any) {
    super(props)
    console.log("props", props)
    this.api = props.api
    this.state = { files: [] }
  }

  public componentDidMount() {
    this.api.getFiles()
      .then(files => this.setState({ files: files }))
  }

  render() {
    console.log("state", this.state)
    return (
      <Container>
        <Row>
          <FileView files={this.state.files} />
          <EditorsPane />
        </Row>
      </Container>
    )
  }
}
