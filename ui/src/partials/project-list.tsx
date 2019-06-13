import React, { Component } from 'react';
import { AppState, Project, projectIsPrivate, projectIsPublic, privateVisibility, publicVisibility, ApiError } from '../models/index'
import { ListGroup, Card, Form, Button, Col, Row, Alert, ButtonGroup } from 'react-bootstrap'
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
    const onClick = (currentUserIsOwner) ? (ev: any) => this.props.newSession(ev, p) : (ev: any) => this.props.previewProject(ev, p)
    return (<ListGroup.Item key={p.id}>
      <Row className="editor-row">
        <Col>
          <Button variant="link" href={link} onClick={onClick}>
            <Octicon name="key" className={projectIsPublic(p) ? "text-success" : "text-danger"} />
            &nbsp;&nbsp;<Octicon name="repo" />
            &nbsp;{p.owner} - {p.name}
          </Button>
        </Col>
        <Col>
          <ButtonGroup className="float-right">
            <Button variant="outline-info" href={`#${p.id}/preview`} onClick={(ev: any) => this.props.previewProject(ev, p)}><Octicon name="device-desktop" /></Button>
            <Button variant="outline-primary" onClick={() => this.props.updateVisibility(p)}><Octicon name="key" /></Button>
            <Button variant="outline-danger" onClick={() => this.props.deleteProject(p)}><Octicon name="flame" /></Button>
          </ButtonGroup>
        </Col>
      </Row>
    </ListGroup.Item>)
  }

  render() {
    return (
      <Card>
        <Card.Header>{this.props.title}</Card.Header>
        <ListGroup variant="flush">
          { this.props.projects.map(p => this.renderProjectLine(p)) }
        </ListGroup>
      </Card>
    )
  }
}