import React, { Component } from 'react';
import { AppState, Project } from '../models/index'
import { Action, setProjects, addProject, setSession } from '../redux/index'
import { Container } from '../layouts'
import { ApiClient, defaultClient } from '../services/api-client'
import { ListGroup, Card, Form, Button, Col } from 'react-bootstrap'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import * as R from 'ramda'

class ProjectViewCon extends Component<any, any> {
  private api: ApiClient
  private newProjectName: string

  constructor(props: any) {
    super(props)
    this.api = defaultClient
    this.newProjectName = ''
  }

  public componentDidMount() {
    this.api.projects()
      .then(this.props.setProjects)
  }

  private newSession(ev: any, p: Project): void {
    console.log("project clicked", p)
    console.log("history", this.props.history)
    //TODO: use session ide provided by backend ;)
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
  }

  public render() {
    const newProjectNameChanged = (ev: any) => this.newProjectName = ev.target.value

    return (<Container>
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
