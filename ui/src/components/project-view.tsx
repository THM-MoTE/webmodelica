import React, { Component } from 'react';
import {Project} from '../models/project'
import {Container} from '../layouts'
import {ApiClient} from '../services/api-client'
import {ListGroup, Card} from 'react-bootstrap'

export class ProjectView extends Component<any,any> {
  private api:ApiClient

  constructor(props:any)  {
    super(props)
    this.api = props.api
    this.state = {projects:[], selectedProject: undefined}
  }

  public componentDidMount() {
    this.api.projects()
      .then(ps => this.setState({projects: ps}))
  }

  private newSession(ev:any, p:Project): void {
    console.log("project clicked", p)
    console.log("history", this.props.history)
    this.props.history.push("/editor")
  }

  public render () {
      return (<Container>
          <Card>
            <Card.Header>Your Projects</Card.Header>
            <ListGroup variant="flush">
            {
              this.state.projects && this.state.projects.map((p:Project) =>
                (<ListGroup.Item action href={"#"+p.id} onClick={(ev:any) => this.newSession(ev, p)} key={p.id}>{p.owner} - {p.name}</ListGroup.Item>))
              }
              </ListGroup>
            </Card>
        </Container>)
  }
}
