import React, { Component } from 'react';
import { Project } from '../models/project'
import {AppState} from '../models/state'
import {Action, setProjects} from '../redux/actions'
import { Container } from '../layouts'
import { ApiClient, defaultClient } from '../services/api-client'
import { ListGroup, Card } from 'react-bootstrap'
import {connect} from 'react-redux'
import * as R from 'ramda'

class ProjectViewCon extends Component<any, any> {
  private api: ApiClient

  constructor(props: any) {
    super(props)
    this.api = defaultClient
    this.state = { projects: this.props.projects }
  }

  public componentDidMount() {
    this.api.projects()
      .then(ps => {
        this.props.setProjects(ps)
        this.setState({ projects: ps})
      })
  }

  private newSession(ev: any, p: Project): void {
    console.log("project clicked", p)
    console.log("history", this.props.history)
    this.props.history.push("/session/" + p.id)
  }

  public render() {
    return (<Container>
      <Card>
        <Card.Header>Your Projects</Card.Header>
        <ListGroup variant="flush">
          {
            this.state.projects && this.state.projects.map((p: Project) =>
              (<ListGroup.Item action href={"#" + p.id} onClick={(ev: any) => this.newSession(ev, p)} key={p.id}>{p.owner} - {p.name}</ListGroup.Item>))
          }
        </ListGroup>
      </Card>
    </Container>)
  }
}

function mapToProps(state:AppState) {
  return { projects: state.projects }
}
function dispatchToProps(dispatch: (a:Action) => void) {
  return {
    setProjects: R.compose(dispatch, setProjects)
  }
}
const ProjectView = connect(mapToProps, dispatchToProps)(ProjectViewCon)
export default ProjectView;
