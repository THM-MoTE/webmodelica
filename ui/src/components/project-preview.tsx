import React, { Component } from 'react';
import { AppState, Project, projectIsPrivate, projectIsPublic, ProjectPreviewState } from '../models/index'
import { Action, setProjectPreview } from '../redux/index'
import { WmContainer } from '../partials/container'
import { ApiClient } from '../services/api-client'
import { ListGroup, Card, Form, Button, Col, Alert } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import * as R from 'ramda'

interface Props {
  api: ApiClient
  projectPreview: ProjectPreviewState
}
type State = any

class ProjectPreviewCon extends Component<Props, State> {

  componentDidMount() {
  }

  render() {
    return (<WmContainer title="Project Preview">
      <h4>preview year: {this.props.projectPreview.project.name}</h4>
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
