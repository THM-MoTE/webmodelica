import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { Container } from '../layouts'
import { FileView } from './file-view'
import { EditorsPane } from './editors-pane'
import { ApiClient } from '../services/api-client'
import { Row } from 'react-bootstrap'
import { AppState } from '../models/state'
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
    console.log("SessionPane: file clicked", f)
    this.setState({ editingFiles: [f] })
  }

  render() {
    console.log("state", this.state)
    return (
      <Container>
        <Row>
          <FileView
            files={this.props.session.files}
            onFileClicked={(f: File) => this.handleFileClicked(f)} />
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
