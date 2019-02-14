import React, { Component } from 'react';
import { Project } from '../models/project'
import {AppState} from '../models/state'
import {Action, setProjects} from '../redux/actions'
import { Container } from '../layouts'
import { ApiClient, defaultClient } from '../services/api-client'
import { ListGroup, Card, Button } from 'react-bootstrap'
import {connect} from 'react-redux'
import { bindActionCreators } from 'redux'
import * as R from 'ramda'

class ProjectViewCon extends Component<any, any> {
  private api: ApiClient

  constructor(props: any) {
    super(props)
    this.api = defaultClient
  }

  public componentDidMount() {
    this.api.projects()
      .then(ps => {
        this.props.setProjects(ps)
      })
  }

  private newSession(ev: any, p: Project): void {
    console.log("project clicked", p)
    console.log("history", this.props.history)
    this.props.history.push("/session/" + p.id)
    ev.preventDefault()
  }

  public render() {

    const click = () => {
      let rnd = Math.floor((Math.random() * 10) + 1)
      this.props.setProjects([{ id: "12"+rnd, name: "Project "+rnd, owner: "Nico" }])
    }

    return (<Container>
      <Card>
        <Card.Header>Your Projects</Card.Header>
        <ListGroup variant="flush">
          {
            this.props.projects && this.props.projects.map((p: Project) =>
              (<ListGroup.Item action href={"#" + p.id} onClick={(ev: any) => this.newSession(ev, p)} key={p.id}>{p.owner} - {p.name}</ListGroup.Item>))
          }
        </ListGroup>
        <Button onClick={click}>test</Button>
      </Card>
    </Container>)
  }
}

function mapToProps(state:AppState) {
  return { projects: state.projects }
}
function dispatchToProps(dispatch: (a:Action) => any) {
  return bindActionCreators({setProjects}, dispatch)
}
const ProjectView = connect(mapToProps, dispatchToProps)(ProjectViewCon)
export default ProjectView;
