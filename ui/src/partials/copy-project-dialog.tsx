import React, { Component } from 'react';
import { Form, Button, Modal, Col, Row } from 'react-bootstrap'
import { AppState, Project, projectIsPrivate, projectIsPublic, File, ProjectPreviewState, ApiError } from '../models/index'
import { Action, notifyInfo, notifyError} from '../redux/index'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import { ApiClient } from '../services/api-client';

interface Props {
  project?:Project
  onClose(): void
  api: ApiClient
  notifyInfo(msg: string): void
  notifyError(msg: string): void
}
type State = {
  newProjectName: string
}

class CopyProjectDialogCon extends React.Component<Props,State> {

  constructor(p:Props) {
    super(p)
    this.state = { newProjectName: ''  }
  }

  private copyProject() {
    console.log("copying ", this.props.project, "with name ", this.state.newProjectName)
    this.props.api
      .copyProject(this.props.project!, this.state.newProjectName || this.props.project!.name)
      .then(p => {
        this.props.notifyInfo(`project ${this.props.project!.name} copied to ${p.name}`)
        this.props.onClose()
      })
      .catch((er:ApiError) => {
        this.props.notifyError(`Couldn't copy project: ${er.statusText}`)
        this.props.onClose()
      })
  }

  render() {
    const handleProjectNameChange = (ev:any) => this.setState({newProjectName: ev.target.value})
    const placeholder = (this.props.project) ? this.props.project!.name : ''
    return (
      <Modal show={this.props.project !== undefined} onHide={this.props.onClose}>
        <Modal.Header closeButton>
          <Modal.Title>Copy Project</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group>
              <Form.Label>New Name</Form.Label>
              <Form.Control type="text" size="lg" placeholder={placeholder} onChange={handleProjectNameChange} />
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
  return bindActionCreators({ notifyInfo, notifyError}, dispatch)
}

export const CopyProjectDialog = connect(mapToProps,dispatchToProps)(CopyProjectDialogCon)
