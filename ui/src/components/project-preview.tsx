import React, { Component } from 'react';
import { EditorsPane } from './index';
import { AppState, Project, projectIsPrivate, projectIsPublic, File, ProjectPreviewState } from '../models/index'
import { Action, setProjectPreview } from '../redux/index'
import { WmContainer } from '../partials/container'
import { ApiClient } from '../services/api-client'
import { ListGroup, Card, Form, Button, Col, Row } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import * as R from 'ramda'

interface Props {
  api: ApiClient
  projectPreview: ProjectPreviewState
}

interface State {
  previewFile?: File
}

class ProjectPreviewCon extends Component<Props, State> {

  constructor(p:Props) {
    super(p)
    this.state = { previewFile: (p.projectPreview.files.length>0) ? p.projectPreview.files[0] : undefined }
  }

  private updatePreviewFile(previewFile:File): void {
    this.setState({previewFile})
  }

  render() {
    const project = this.props.projectPreview.project
    const files = this.props.projectPreview.files
    return (<WmContainer title={"Preview: "+project.name}>
    <Row>
      <Col xs={2}>
        <ListGroup>
          {files.map(file => (
            <ListGroup.Item key={file.relativePath} onClick={() => this.updatePreviewFile(file)}>
              {file.relativePath}
            </ListGroup.Item>
          ))}
        </ListGroup>
      </Col>
      <Col>
          <EditorsPane
            file={this.state.previewFile}
            api={this.props.api}
          />
      </Col>
    </Row>
    </WmContainer>)
  }
}

function mapToProps(state: AppState) {
  return { projectPreview: state.projectPreview! }
}
function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ }, dispatch)
}

const ProjectPreview = connect(mapToProps, dispatchToProps)(ProjectPreviewCon)
export default ProjectPreview
