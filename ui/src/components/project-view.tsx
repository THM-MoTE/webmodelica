import React, { Component } from 'react';
import { AppState, Project } from '../models/index'
import { Action, setProjects, addProject, setSession } from '../redux/index'
import { Container } from '../layouts'
import { ApiClient } from '../services/api-client'
// import * as alerts from './partials/alerts'
import { ListGroup, Card, Form, Button, Col, Alert } from 'react-bootstrap'
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
    console.log("project clicked", p)
    console.log("history", this.props.history)
    this.api.newSession(p)
      .then(s => {
        this.props.setSession(s)
        return s
      })
      .then(s => this.props.history.push("/session/" + s.id))
    ev.preventDefault()
  }

  private newProject() {
    this.api.newProject(this.props.username, this.newProjectName)
      .then(this.props.addProject)
      .then(this.clearErrors.bind(this))
      .catch(er => this.setState({ errors: [er] }))
  }

  public render() {
    const newProjectNameChanged = (ev: any) => this.newProjectName = ev.target.value

    return (<Container>
      <Alert show={!R.isEmpty(this.state.errors)} variant="danger" onClose={() => undefined}>
        {this.state.errors.join("\n")}
      </Alert>
      <Card>
        <Card.Header>Your Projects</Card.Header>
        <ListGroup variant="flush">
          <ListGroup.Item>
            <Form.Row className="justify-content-md-center">
              <Col sm={10}><Form.Control placeholder="Enter project name" onChange={newProjectNameChanged} /></Col>
              <Col sm={1}><Button variant="outline-primary" onClick={this.newProject.bind(this)}>New Project</Button></Col>
            </Form.Row>
          </ListGroup.Item>
          {
            this.props.projects && this.props.projects.map((p: Project) =>
              (<ListGroup.Item action href={"#" + p.id} onClick={(ev: any) => this.newSession(ev, p)} key={p.id}>{p.owner} - {p.name}</ListGroup.Item>))
          }
        </ListGroup>
      </Card>
    </Container >)
  }
}

function mapToProps(state: AppState) {
  return { projects: state.projects, username: state.authentication!.username }
}
function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ setProjects, addProject, setSession }, dispatch)
}
const ProjectView = connect(mapToProps, dispatchToProps)(ProjectViewCon)
export default ProjectView;
