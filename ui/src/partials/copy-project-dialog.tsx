import React, { Component } from 'react';
import { Form, Button, Modal, Col, Row } from 'react-bootstrap'
import { AppState, Project, projectIsPrivate, projectIsPublic, File, ProjectPreviewState } from '../models/index'
import { Action, setProjectPreview } from '../redux/index'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import { ApiClient } from '../services/api-client';

interface Props {
  project?:Project
  onClose(): void
  api: ApiClient
}
type State = any

class CopyProjectDialogCon extends React.Component<Props,State> {
  private newProjectName: string = ""

  constructor(p:Props) {
    super(p)
  }

  private copyProject() {
    console.log("copying ", this.props.project, "with name ", this.newProjectName)
    this.props.api
      .copyProject(this.props.project!, this.newProjectName)
      .then(p => {
        console.log("copied to ", p)
        this.props.onClose()
      })
  }

  render() {
    if(this.props.project) {
      this.newProjectName = this.props.project.name
    }

    const handleProjectNameChange = (ev:any) => this.newProjectName = ev.target.value
    return (
      <Modal show={this.props.project !== undefined} onHide={this.props.onClose}>
        <Modal.Header closeButton>
          <Modal.Title>Copy Project</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group>
              <Form.Label>New Name</Form.Label>
              <Form.Control type="text" size="lg" placeholder={this.newProjectName} onChange={handleProjectNameChange} />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="success" type="submit" onClick={this.copyProject.bind(this)}>Copy Project</Button>
        </Modal.Footer>
      </Modal>
    )
  }
}

function mapToProps(state: AppState) {
  return { projectPreview: state.projectPreview! }
}
function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({}, dispatch)
}

export const CopyProjectDialog = connect(mapToProps,dispatchToProps)(CopyProjectDialogCon)
