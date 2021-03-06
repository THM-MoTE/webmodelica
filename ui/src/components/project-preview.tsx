import React, { Component } from 'react';
import { EditorsPane, TreeView } from './index';
import { AppState, Project, projectIsPrivate, projectIsPublic, File, FilePath, ProjectPreviewState } from '../models/index'
import { Action, setProjectPreview } from '../redux/index'
import { WmContainer } from '../partials/container'
import { ApiClient } from '../services/api-client'
import { ListGroup, Card, Form, Button, ButtonGroup, Col, Row } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import * as R from 'ramda'
import { CopyProjectDialog } from '../partials/copy-project-dialog';

interface Props {
  api: ApiClient
  projectPreview: ProjectPreviewState
}

interface State {
  previewFile?: File
  projectToCopy?: Project
}

/** The project preview pane, displayed at `projects/:projectId/preview` */
class ProjectPreviewCon extends Component<Props, State> {

  constructor(p:Props) {
    super(p)
    this.state = {}
    EditorsPane.killEditorInstance() //to prevent old monaco editor instances
  }

  private updatePreviewFile(previewFile:File): void {
    this.props.api.getFile(this.props.projectPreview.project, previewFile.relativePath)
      .then(previewFile => this.setState({previewFile}))
  }

  private copyProject(projectToCopy:Project=this.props.projectPreview.project) {
    this.setState({projectToCopy})
  }
  private clearCopyProject() {
    this.setState({projectToCopy: undefined})
  }

  render() {
    const project = this.props.projectPreview.project
    const files = this.props.projectPreview.files

    return (<WmContainer title={"Preview: "+project.name}>
    <Row>
      <Col md={2}>
        <ButtonGroup vertical className="full-width">
            <Button variant="outline-primary" onClick={() => this.copyProject()}><Octicon name="repo-clone" /> Copy Project</Button>
          <Button variant="outline-primary" href={this.props.api.projectDownloadUrl(project.id)}><Octicon name="cloud-download" /> Download Archive</Button>
        </ButtonGroup>
        <h5 className="text-secondary">Files</h5>
        <TreeView tree={files} compilerErrors={[]} onFileClicked={this.updatePreviewFile.bind(this)} />
      </Col>
      <Col md={10}>
          <EditorsPane
            file={this.state.previewFile}
            api={this.props.api}
          />
      </Col>
    </Row>
    <CopyProjectDialog api={this.props.api} project={this.state.projectToCopy} onClose={this.clearCopyProject.bind(this)}/>
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
