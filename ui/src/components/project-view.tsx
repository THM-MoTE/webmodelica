import React, { Component } from 'react';
import { AppState, Project, projectIsPrivate, projectIsPublic, privateVisibility, publicVisibility, ApiError} from '../models/index'
import { Action, setProjects, setProject, addProject, setSession, setProjectPreview, notifyInfo, notifyError, setBackgroundJobInfo } from '../redux/index'
import { WmContainer } from '../partials/container'
import { ProjectList } from '../partials/project-list'
import { withOverlay } from '../partials/loading-overlay'
import { ApiClient } from '../services/api-client'
// import * as alerts from './partials/alerts'
import { ListGroup, Card, Form, Button, Col, Row, Alert, ButtonGroup, Tab, Nav } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import * as R from 'ramda'


/** The project view displayed at `/projects`. */
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
        .then(s => this.props.history.push("/session/" + s.id))
    )
    ev.preventDefault()
  }

  private previewProject(ev: any, p:Project): void {
    ev.preventDefault()
    withOverlay(this.props.setBackgroundJobInfo, "opening project ...")(
      this.api.projectFileTree(p.id)
        .then(files => {
          this.props.setProjectPreview(p, files)
          this.props.history.push(`/projects/${p.id}/preview`)
        })
        .catch((er: ApiError) => this.props.notifyError(`Couldn't open files for preview: ${er.statusText}`))
    )
  }

  private newProject(ev:any) {
    ev.preventDefault()
    this.api.newProject(this.props.username, this.newProjectName)
      .then(project => {
        this.props.addProject(project)
        this.props.notifyInfo(`New private project '${project.name}' created.`)
      })
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

  private myProjects(): Project[] {
    return this.props.projects.filter((p:Project) => p.owner === this.props.username)
  }
  private publicProjects(): Project[] {
    return this.props.projects.filter(projectIsPublic)
  }

  public render() {
    const newProjectNameChanged = (ev: any) => this.newProjectName = ev.target.value
    return (<WmContainer title="Projects">
      <Row> <Col md={2} /><Col md={10}>
      <Form onSubmit={this.newProject.bind(this)}>
        <Form.Row>
          <Col md={10}><Form.Control placeholder="Enter project name" onChange={newProjectNameChanged} required /></Col>
          <Col md={2}><Button variant="outline-primary" type="submit" style={{width: '100%'}}><Octicon name="plus" />New Project</Button></Col>
        </Form.Row>
      </Form> </Col></Row>
      <Tab.Container id="project-tabs" defaultActiveKey="my-projects">
        <Row style={{marginTop: '1.5em'}}>
          <Col md={2}>
            <Nav variant="pills" className="flex-column">
              <Nav.Item>
                <Nav.Link eventKey="my-projects">My Projects</Nav.Link>
              </Nav.Item>
              <Nav.Item>
                <Nav.Link eventKey="public-projects">Public Projects</Nav.Link>
              </Nav.Item>
            </Nav>
          </Col>
          <Col md={10}>
            <Tab.Content>
              <Tab.Pane eventKey="my-projects">
                <ProjectList
                  username={this.props.username}
                  title="My Projects"
                  projects={this.myProjects()}
                  newSession={this.newSession.bind(this)}
                  previewProject={this.previewProject.bind(this)}
                  updateVisibility={this.updateVisibility.bind(this)}
                  deleteProject={this.deleteProject.bind(this)} />
              </Tab.Pane>
              <Tab.Pane eventKey="public-projects">
                <ProjectList
                  username={this.props.username}
                  title="Public Projects"
                  projects={this.publicProjects()}
                  newSession={this.newSession.bind(this)}
                  previewProject={this.previewProject.bind(this)}
                  updateVisibility={this.updateVisibility.bind(this)}
                  deleteProject={this.deleteProject.bind(this)} />
              </Tab.Pane>
            </Tab.Content>
          </Col>
        </Row>
      </Tab.Container>
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
