import React, { Component } from 'react';
import {Project} from '../models/project'

export class ProjectView extends Component<any,any> {

  projectBaseAddress: string

  constructor(props:any)  {
    super(props)
    this.projectBaseAddress = location.protocol+"//"+location.host+"/projects"
    this.state = {projects:[]}
  }

  copmonentDidMount() {
    fetch(this.projectBaseAddress)
        .then(res => res.json())
        .then(result => {
          this.setState({projects:result})
        })
  }

  render () {
    return (
      <ul className="list-group">
      {
        this.state && this.state.projects.map((p:Project) =>
          (<li className="list-group-item">{p.name}</li>))
        }
      </ul>
    )
  }
}
