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
    this.state = { files: [], editingFiles: [] }
  }

  public componentDidMount() {
    this.api.getFiles()
      .then(files => this.setState({ files: files }))
  }

  private handleFileClicked(f: File): void {
    console.log("SessionPane: file clicked", f)
    this.setState({ editingFiles: [f] })
  }

  render() {
    console.log("state", this.state)
    return (
      <Container>
        <Row>
          <FileView
            files={this.state.files}
            onFileClicked={(f: File) => this.handleFileClicked(f)} />
          <EditorsPane
            files={this.state.editingFiles} />
        </Row>
      </Container>
    )
  }
}
