import React, { Component } from 'react';
import { AppState, Project, projectIsPrivate, projectIsPublic} from '../models/index'
import { Action, setProjects, addProject, setSession, setProjectPreview } from '../redux/index'
import { WmContainer } from '../partials/container'
import { ApiClient } from '../services/api-client'
// import * as alerts from './partials/alerts'
import { ListGroup, Card, Form, Button, Col, Row, Alert, ButtonGroup } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import * as R from 'ramda'
import { renderErrors } from '../partials/errors';

class ProjectViewCon extends Component<any, any> {
  private api: ApiClient
  private newProjectName: string

  constructor(props: any) {
    super(props)
    this.api = this.props.api
    this.newProjectName = ''
    this.state = { errors: [] }
  }

  public componentDidMount() {
    this.api.projects()
      .then(this.props.setProjects)
  }

  private clearErrors(): void {
    this.setState({ errors: [] })
  }

  private newSession(ev: any, p: Project): void {
    this.api.newSession(p)
      .then(s => {
        this.props.setSession(s)
        return s
      })
      .then(s => this.props.history.push("/session/" + s.id))
    ev.preventDefault()
  }

  private previewProject(ev: any, p:Project): void {
    ev.preventDefault()
    this.api.projectFiles(p.id)
      .then(files => {
        this.props.setProjectPreview(p, files)
        this.props.history.push(`/projects/${p.id}/preview`)
      })
  }

  private newProject() {
    this.api.newProject(this.props.username, this.newProjectName)
      .then(this.props.addProject)
      .then(this.clearErrors.bind(this))
      .catch(er => this.setState({ errors: [er] }))
  }

  public render() {
    const newProjectNameChanged = (ev: any) => this.newProjectName = ev.target.value

    return (<WmContainer title="Projects">
      {renderErrors(this.state.errors)}
      <Card>
        <Card.Header>Your Projects</Card.Header>
        <ListGroup variant="flush">
          <ListGroup.Item>
            <Form.Row className="justify-content-md-center">
              <Col sm={10}><Form.Control placeholder="Enter project name" onChange={newProjectNameChanged} /></Col>
              <Col sm={1}><Button variant="outline-primary" onClick={this.newProject.bind(this)}>
                <Octicon name="plus" />New Project
            </Button></Col>
            </Form.Row>
          </ListGroup.Item>
          {
            this.props.projects && this.props.projects.map((p: Project) =>
              (<ListGroup.Item key={p.id}>
                <Row>
                  <Col>
                    <Button variant="link" href={"#" + p.id} onClick={(ev: any) => this.newSession(ev, p)}>
                      <Octicon name="key" className={projectIsPublic(p) ? "text-success" : "text-danger"}/>
                      &nbsp;&nbsp;<Octicon name="repo" />
                      &nbsp;{p.owner} - {p.name}
                    </Button>
                  </Col>
                  <Col>
                    <ButtonGroup className="float-right">
                      <Button variant="outline-info" href={`#${p.id}/preview`} onClick={(ev:any) => this.previewProject(ev, p)}><Octicon name="device-desktop"/></Button>
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
  return bindActionCreators({ setProjects, addProject, setSession, setProjectPreview }, dispatch)
}
const ProjectView = connect(mapToProps, dispatchToProps)(ProjectViewCon)
export default ProjectView;
