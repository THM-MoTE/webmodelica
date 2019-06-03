import React, { Component } from 'react';
import { Navbar, Nav, Row, Col, Container, NavDropdown } from 'react-bootstrap'
import { Footer } from './footer'
import NotificationComponent from './notification'
//@ts-ignore
import Octicon from 'react-octicon'
import { connect } from 'react-redux'
import { compose } from 'redux'
import { AppState, Notification } from '../models/index'
import * as R from 'ramda';
import { oc } from 'ts-optchain';
import { LoadingOverlay } from './loading-overlay';

interface Props {
  title: string
  username?: string
  active?: string
  children: any
  notifications: Notification[]
}

/** Application wrapper, its a wrapper for the whole page and contains from top to bottom:
 * - the navbar
 * - the notifications area
 * - the actual component as children
 * - the loading overlay
 * - the optional footer, only displayed on landing page
*/
class WmContainerCon extends React.Component<any, any> {
  private readonly appName: string = "Webmodelica"
  constructor(props: any) {
    super(props)
  }
  componentDidMount() {
    document.title = this.appName + " " + this.props.title
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
        {this.props.username && (
          <div className="collapse navbar-collapse justify-content-end">
            {this.simLink()}
            <Nav.Item><Nav.Link href="/projects"><Octicon name="repo" /> Projects</Nav.Link></Nav.Item>
            <NavDropdown title={this.props.username} id="nav-profile-dropwdown">
              <NavDropdown.Item href="/logout"><Octicon name="sign-out" /> Logout</NavDropdown.Item>
            </NavDropdown>
          </div>
        )}
      </Navbar>
      <div className="container-fluid">
        <Row><Col xs='12'>
          {this.props.notifications.map((n: Notification, idx: number) => (<NotificationComponent key={idx} notification={n} />))}
        </Col></Row>
        <Row><Col xs='12'>
          {this.props.children}
        </Col></Row>
      </div>
      <LoadingOverlay/>
      {(window.location.pathname === '/') && <Footer />}
    </>)
  }
}

function mapProps(state: AppState) {
  return { username: oc(state).authentication.username(), notifications: state.notifications }
}

export const WmContainer = connect(mapProps, null)(WmContainerCon)
export default WmContainer
