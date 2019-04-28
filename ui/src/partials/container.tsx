import React, { Component } from 'react';
import { Navbar, Nav, Row, Col, Container } from 'react-bootstrap'
import {Footer} from './footer'
//@ts-ignore
import Octicon from 'react-octicon'

export class WmContainer extends React.Component<any, any> {
  private readonly appName:string = "Webmodelica"
  constructor(props: any) {
    super(props)
  }
  componentDidMount() {
    document.title = this.appName+" "+this.props.title
  }

  simLink() {
    if (this.props.sessionId && this.props.active === 'session') {
      //we are at the session/editor pane => create a link to simulation
      return (<Nav.Item><Nav.Link href={`/session/${this.props.sessionId}/simulate`}><Octicon name="rocket" /> Simulate</Nav.Link></Nav.Item>)
    } else if (this.props.sessionId && this.props.active === 'simulation') {
      //we are at simulation => create a link to editor pane
      return (<Nav.Item><Nav.Link href={`/session/${this.props.sessionId}`}><Octicon name="reply" /> Back to Session</Nav.Link></Nav.Item>)
    }
    else { return undefined } //we don't know => we don't create a link
  }

  render() {
    return (<>
      <Navbar>
        <Navbar.Brand href="#home">{this.appName} {this.props.title}</Navbar.Brand>
        <Navbar.Toggle />
        <div className="collapse navbar-collapse justify-content-end">
          {this.simLink()}
          <Nav.Item><Nav.Link href="/projects"><Octicon name="repo" /> Projects</Nav.Link></Nav.Item>
          <Nav.Item><Nav.Link href="/logout"><Octicon name="sign-out" /> Logout</Nav.Link></Nav.Item>
        </div>
      </Navbar>
      <div className="container-fluid">
        {this.props.children}
      </div>
      <Footer />
    </>)
  }
}
