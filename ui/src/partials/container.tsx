import React, { Component } from 'react';
import { Navbar, Nav, Row, Col, Container, NavDropdown } from 'react-bootstrap'
import { Footer } from './footer'
import NotificationComponent from './notification'
//@ts-ignore
import Octicon from 'react-octicon'
import { connect } from 'react-redux'
import { compose } from 'redux'
import { AppState, Notification, NotificationType } from '../models/index'
import * as R from 'ramda';
import { oc } from 'ts-optchain';
import { LoadingOverlay } from './loading-overlay';
import { Link } from 'react-router-dom';

interface Props {
  title: string
  username?: string
  active?: string
  children: any
  notifications: Notification[]
  sessionId?: string
  displayName?: string
}

/** Application wrapper, its a wrapper for the whole page and contains from top to bottom:
 * - the navbar
 * - the notifications area
 * - the actual component as children
 * - the loading overlay
 * - the optional footer, only displayed on landing page
*/
class WmContainerCon extends React.PureComponent<Props> {
  private readonly appName: string = "Webmodelica"
  constructor(props: Props) {
    super(props)
  }
  componentDidMount() {
    document.title = this.appName + " " + this.props.title
  }

  simLink() {
    if (this.props.sessionId && this.props.active === 'session') {
      //we are at the session/editor pane => create a link to simulation
      return (<Nav.Item><Nav.Link><Link to={`/session/${this.props.sessionId}/simulate`}><Octicon name="rocket" /> Simulate</Link></Nav.Link></Nav.Item>)
    } else if (this.props.sessionId && this.props.active === 'simulation') {
      //we are at simulation => create a link to editor pane
      return (<Nav.Item><Nav.Link><Link to={`/session/${this.props.sessionId}`}><Octicon name="reply" /> Back to Session</Link></Nav.Link></Nav.Item>)
    }
    else { return undefined } //we don't know => we don't create a link
  }

  render() {
    return (<>
      <Navbar collapseOnSelect expand="md">
        <Navbar.Brand><h3>{this.appName} {this.props.title}</h3></Navbar.Brand>
        <Navbar.Toggle aria-controls="webmodelica-navbar-nav"/>
          {this.props.displayName && (
          <Navbar.Collapse id="webmodelica-navbar-nav" className="justify-content-end">
            {this.simLink()}
            <Nav.Item><Link to="/projects"><Octicon name="repo" /> Projects</Link></Nav.Item>
            <NavDropdown title={this.props.displayName} id="nav-profile-dropwdown">
              <NavDropdown.Item href="/logout"><Octicon name="sign-out" /> Logout</NavDropdown.Item>
            </NavDropdown>
          </Navbar.Collapse>
        )}
      </Navbar>
      { !R.isEmpty(this.props.notifications) && (
        <div className="position-absolute w-100 d-flex flex-row-reverse p-4 fixed-bottom" style={{zIndex:100}}>
          <div className="d-flex flex-column">
              { /** all info notifications are displayed bottom-right as toasts */
                this.props.notifications.filter((n: Notification) => n.type === NotificationType.Info).map((n: Notification, idx: number) => (<NotificationComponent key={idx} notification={n} />))}
            </div>
        </div>
      )}
      <div className="container-fluid py-2">
        <Row><Col xs='12'>
          { /** all other notifications are displayed as Alerts on-top of all children */
            this.props.notifications.filter((n:Notification) => n.type !== NotificationType.Info).map((n: Notification, idx: number) => (<NotificationComponent key={idx} notification={n} />))}
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
  return { displayName: oc(state).authentication.displayName(), notifications: state.notifications }
}

export const WmContainer = connect(mapProps, null)(WmContainerCon)
export default WmContainer
