import React, { Component } from 'react';
import { AppState, Project, projectIsPrivate, projectIsPublic, privateVisibility, publicVisibility, ApiError } from '../models/index'
import { ListGroup, Card, Form, Button, Col, Row, Alert, ButtonGroup, Media } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import * as R from 'ramda'

interface Props {
  projects: Project[]
  username: string
  title: string
  newSession(ev:any, p:Project):void
  previewProject(ev:any, p:Project):void
  updateVisibility(p: Project): void
  deleteProject(p: Project): void
}

export class ProjectList extends React.Component<Props, any> {

  private renderProjectLine(p: Project) {
    //if the current user is project owner: create a session, open preview otherwise
    const currentUserIsOwner = p.owner === this.props.username
    const link = (currentUserIsOwner) ? "#" + p.id : `#${p.id}/preview`
    const onClick = R.compose(() => false , (currentUserIsOwner) ? (ev: any) => this.props.newSession(ev, p) : (ev: any) => this.props.previewProject(ev, p))
    return (
      <Media as="li" key={p.id}>
        <a style={{width: 64, height: 64 }} className="mr-3 btn">
          <Octicon name='repo' mega/>
        </a>
        <Media.Body>
          <Row><Col>
            <a href={link} onClick={onClick}>{p.name}</a>
            <h6>
              <Octicon name='person' /> {p.owner} &nbsp;&nbsp;
              <Octicon name="key" className={projectIsPublic(p) ? "text-success" : "text-danger"} />
            </h6>
          </Col>
           <Col><ButtonGroup size="lg" className="float-right">
             <Button variant="outline-info" href={`#${p.id}/preview`} onClick={(ev: any) => this.props.previewProject(ev, p)}><Octicon name="device-desktop" /></Button>
             <Button variant="outline-primary" onClick={() => this.props.updateVisibility(p)}><Octicon name="key" /></Button>
             <Button variant="outline-danger" onClick={() => this.props.deleteProject(p)}><Octicon name="flame" /></Button>
            </ButtonGroup></Col>
          </Row>
        </Media.Body>
      </Media>
    )
  }

  render() {
    return (
      <ul className="list-unstyled">
        {this.props.projects.map(p => this.renderProjectLine(p))}
      </ul>
    )
  }
}