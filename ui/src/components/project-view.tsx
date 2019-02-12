import React, { Component } from 'react';
import {Project} from '../models/project'
import {Container} from '../layouts'

export class ProjectView extends Component<any,any> {

  projectBaseAddress: string

  constructor(props:any)  {
    super(props)
    this.projectBaseAddress = location.protocol+"//"+location.host+"/api/projects"
    this.state = {projects:[]}
  }

  private fetchJson(addr:string):Promise<Project[]> {
    return fetch(addr, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    })
    .then(res => res.json())
  }

  public componentDidMount() {
    const addr = "/projects"
    console.log("fetching projects from:", addr)
    this.fetchJson(addr)
        .then(result => {
          this.setState({projects:result})
        })
  }

  public render () {
    return (
      <Container>
        <ul className="list-group">
        {
          this.state.projects && this.state.projects.map((p:Project) =>
            (<li className="list-group-item">{p.owner} - {p.name}</li>))
          }
        </ul>
      </Container>
    )
  }
}
