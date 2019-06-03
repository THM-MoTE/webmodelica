import React, { Component } from 'react';
import { AppState, Project, projectIsPrivate, projectIsPublic, privateVisibility, publicVisibility, ApiError} from '../models/index'
import { Action, setProjects, setProject, addProject, setSession, setProjectPreview, notifyInfo, notifyError, setBackgroundJobInfo } from '../redux/index'
import { WmContainer } from '../partials/container'
import { withOverlay } from '../partials/loading-overlay'
import { ApiClient } from '../services/api-client'
// import * as alerts from './partials/alerts'
import { ListGroup, Card, Form, Button, Col, Row, Alert, ButtonGroup } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import * as R from 'ramda'

class ProjectViewCon extends Component<any, any> {
  private api: ApiClient
  private newProjectName: string

  constructor(props: any) {
    super(props)
    this.api = this.props.api
    this.newProjectName = ''
  }

  public componentDidMount() {
    this.api.projects()
      .then(this.props.setProjects)
      .catch((er: ApiError) => this.props.notifyError(`Couldn't fetch all projects: ${er.statusText}`))
  }

  private newSession(ev: any, p: Project): void {
    withOverlay(this.props.setBackgroundJobInfo, "opening project ...")(
    this.api.newSession(p)
      .then(s => {
        this.props.setSession(s)
        return s
      })
      .then(s => this.props.history.push("/session/" + s.id)))
    ev.preventDefault()
  }

  private previewProject(ev: any, p:Project): void {
    ev.preventDefault()
    this.api.projectFiles(p.id)
      .then(files => {
        this.props.setProjectPreview(p, files)
        this.props.history.push(`/projects/${p.id}/preview`)
      })
      .catch((er: ApiError) => this.props.notifyError(`Couldn't open files for preview: ${er.statusText}`))
  }

  private newProject(ev:any) {
    ev.preventDefault()
    this.api.newProject(this.props.username, this.newProjectName)
      .then(this.props.addProject)
      .catch((er:ApiError) => this.props.notifyError(`Couldn't create new project: ${er.statusText}`))
  }

  private updateVisibility(p:Project): void {
    const newVisibility = (projectIsPublic(p)) ? privateVisibility : publicVisibility
    this.props.api.updateVisibility(p.id, newVisibility)
      .then(this.props.setProject)
      .then(() => this.props.notifyInfo(`visibility for ${p.name} changed to ${newVisibility}`))
      .catch((er:ApiError) => this.props.notifyError(`Couldn't update visibility: ${er.statusText}`))
  }

  private deleteProject(p:Project): void {
    const newProjects = this.props.projects.filter((other:Project) => other.id!==p.id)
    this.props.api.deleteProject(p)
      .then(() => this.props.setProjects(newProjects))
      .then(() => this.props.notifyInfo(`Project ${p.name} removed!`))
      .catch((er: ApiError) => this.props.notifyError(`Couldn't delete project: ${er.statusText}`))
  }

  private renderProjectLine(p: Project) {
    //if the current user is project owner: create a session, open preview otherwise
    const currentUserIsOwner = p.owner === this.props.username
    const link = (currentUserIsOwner) ? "#" + p.id : `#${p.id}/preview`
    const onClick = (currentUserIsOwner) ? (ev: any) => this.newSession(ev, p) : (ev: any) => this.previewProject(ev, p)
    return (
      <Button variant="link" href={link} onClick={onClick}>
        <Octicon name="key" className={projectIsPublic(p) ? "text-success" : "text-danger"} />
        &nbsp;&nbsp;<Octicon name="repo" />
        &nbsp;{p.owner} - {p.name}
      </Button>
    )
  }

  public render() {
    const newProjectNameChanged = (ev: any) => this.newProjectName = ev.target.value

    return (<WmContainer title="Projects">
      <Card>
        <Card.Header>Your Projects</Card.Header>
        <ListGroup variant="flush">
          <ListGroup.Item>
            <Form onSubmit={this.newProject.bind(this)}>
            <Form.Row className="justify-content-md-center">
              <Col sm={10}><Form.Control placeholder="Enter project name" onChange={newProjectNameChanged} required /></Col>
              <Col sm={1}><Button variant="outline-primary" type="submit"><Octicon name="plus" />New Project</Button></Col>
            </Form.Row>
            </Form>
          </ListGroup.Item>
          {
            this.props.projects && this.props.projects.map((p: Project) =>
              (<ListGroup.Item key={p.id}>
                <Row className="editor-row">
                  <Col>
                    {this.renderProjectLine(p)}
                  </Col>
                  <Col>
                    <ButtonGroup className="float-right">
                      <Button variant="outline-info" href={`#${p.id}/preview`} onClick={(ev:any) => this.previewProject(ev, p)}><Octicon name="device-desktop"/></Button>
                      <Button variant="outline-primary" onClick={() => this.updateVisibility(p)}><Octicon name="key"/></Button>
                      <Button variant="outline-danger" onClick={() => this.deleteProject(p)}><Octicon name="flame"/></Button>
                    </ButtonGroup>
                  </Col>
                </Row>
              </ListGroup.Item>))
          }
        </ListGroup>
      </Card>
    </WmContainer >)
  }
}

function mapToProps(state: AppState) {
  return { projects: state.projects, username: state.authentication!.username }
}
function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ setProjects, setProject, addProject, setSession, setProjectPreview, notifyInfo, notifyError, setBackgroundJobInfo }, dispatch)
}
const ProjectView = connect(mapToProps, dispatchToProps)(ProjectViewCon)
export default ProjectView;
