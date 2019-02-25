import React, { Component } from 'react';
import { Navbar, Nav } from 'react-bootstrap'

export class WmContainer extends React.Component<any, any> {
  constructor(props: any) {
    super(props)
  }
  componentDidMount() { }

  render() {
    return (<>
      <Navbar>
        <Navbar.Brand href="#home">Webmodelica {this.props.title}</Navbar.Brand>
        <Navbar.Toggle />
        <div className="collapse navbar-collapse justify-content-end">
          <Nav.Item><Nav.Link href="/projects">Projects</Nav.Link></Nav.Item>
          <Nav.Item><Nav.Link href="/logout">Logout</Nav.Link></Nav.Item>
        </div>
      </Navbar>
      <div className = "container-fluid">
        { this.props.children }
      </div>
    </>)
  }
}