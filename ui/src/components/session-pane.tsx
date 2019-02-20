import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { Container } from '../layouts'
import { FileView } from './index'
import { EditorsPane } from './editors-pane'
import { ApiClient } from '../services/api-client'
import { Row, Col, Button, ButtonGroup, Container as RContainer, Card } from 'react-bootstrap'
import { File, AppState } from '../models/index'
import { Action, updateSessionFiles } from '../redux/actions'

class SessionPaneCon extends React.Component<any, any> {
  private readonly api: ApiClient

  constructor(props: any) {
    super(props)
    this.api = this.props.api
    this.state = { editingFiles: [] }
  }

  public componentDidMount() {
  }

  private handleFileClicked(f: File): void {
    this.setState({ editingFiles: [f] })
  }
  handleSaveClicked() {
    console.log("save")
    let content = EditorsPane.monacoEditor.getValue()
    let files: File[] = [{ ...this.state.editingFiles[0], content: content }]
    const updatePromises = files.map((f: File) => this.api.updateFile(f))
    Promise.all(updatePromises).then(fs => this.props.updateSessionFiles(fs))
  }
  handleCompileClicked() {
    this.api.compile(this.state.editingFiles[0])
      .then(errors => console.log("got errors", errors))
  }

  render() {
    return (
      <Container>
        <Row>
          <Col lg="2">
            <FileView
              onSaveClicked={this.handleSaveClicked.bind(this)}
              onCompileClicked={this.handleCompileClicked.bind(this)}
              onFileClicked={(f: File) => this.handleFileClicked(f)}
              api={this.api} />
          </Col>
          <EditorsPane
            files={this.state.editingFiles} />
        </Row>
      </Container>
    )
  }
}

function mapProps(state: AppState) {
  return { session: state.session }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ updateSessionFiles }, dispatch)
}

const SessionPane = connect(mapProps, dispatchToProps)(SessionPaneCon)
export default SessionPane
