import React, { Component } from 'react';
import { Navbar, Nav, Row, Col, Container, NavDropdown, Dropdown } from 'react-bootstrap'
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

  render() {
    return (<>
      <Navbar expand="md" variant="dark" className="bg-primary">
        <Navbar.Brand><h3>{this.appName} {this.props.title}</h3></Navbar.Brand>
        <Navbar.Toggle aria-controls="webmodelica-navbar-nav" />
        {this.props.displayName && (
          <Navbar.Collapse id="webmodelica-navbar-nav" className="justify-content-end">
            <Nav activeKey={this.props.active}>
              {this.props.sessionId && (<>
                <Nav.Item><Nav.Link eventKey="session" as={Link} to={`/session/${this.props.sessionId}`}><Octicon name="gist" /> Session</Nav.Link></Nav.Item>
                <Nav.Item><Nav.Link eventKey="simulate" as={Link} to={`/session/${this.props.sessionId}/simulate`}><Octicon name="rocket" /> Simulate</Nav.Link></Nav.Item>
                </>)}
              <Nav.Item><Nav.Link eventKey="projects" as={Link} to="/projects"><Octicon name="repo" /> Projects</Nav.Link></Nav.Item>
              <Dropdown as={Nav.Item}>
                <Dropdown.Toggle as={Nav.Link} id="nav-user-dropdown">{this.props.displayName}</Dropdown.Toggle>
                <Dropdown.Menu className="dropdown-menu dropdown-menu-right">
                  <Dropdown.Item href="/logout"><Octicon name="sign-out" /> Logout</Dropdown.Item>
                </Dropdown.Menu>
              </Dropdown>
            </Nav>
          </Navbar.Collapse>
        )}
      </Navbar>
      {!R.isEmpty(this.props.notifications) && (
        <div className="position-absolute w-100 d-flex flex-row-reverse p-4 fixed-bottom" style={{ zIndex: 100 }}>
          <div className="d-flex flex-column">
            { /** all info notifications are displayed bottom-right as toasts */
              this.props.notifications.filter((n: Notification) => n.type === NotificationType.Info).map((n: Notification, idx: number) => (<NotificationComponent key={idx} notification={n} />))}
          </div>
        </div>
      )}
      <div className="container-fluid py-2">
        <Row><Col xs='12'>
          { /** all other notifications are displayed as Alerts on-top of all children */
            this.props.notifications.filter((n: Notification) => n.type !== NotificationType.Info).map((n: Notification, idx: number) => (<NotificationComponent key={idx} notification={n} />))}
        </Col></Row>
        <Row><Col xs='12'>
          {this.props.children}
        </Col></Row>
      </div>
      <LoadingOverlay />
      {(window.location.pathname === '/') && <Footer />}
    </>)
  }
}

function mapProps(state: AppState) {
  return { displayName: oc(state).authentication.displayName(), notifications: state.notifications }
}

export const WmContainer = connect(mapProps, null)(WmContainerCon)
export default WmContainer
