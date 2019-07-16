import React, { Component } from 'react';
import { Navbar, Nav, Row, Col, Container } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { AppInfo } from '../models';
import { fetchAppInfos } from '../services/api-client'

interface State {
  infos?: AppInfo
}

export class Footer extends React.Component<any, State> {
  constructor(props:any) {
    super(props)
    this.state = {}
  }

  componentDidMount() {
    fetchAppInfos()
      .then(infos => this.setState({infos}))
  }

  render() {
    return (
      <footer className="footer">
        <Container fluid>
          <Row className="justify-content-xs-center">
            <Col xs="5">
              {this.state.infos && (
                <small>{this.state.infos.copyright}. Distributed under the <a target="_blank" href={this.state.infos.licenseUri}>{this.state.infos.license}</a>.</small>
              )}
            </Col>
            <Col xs="auto">
              {this.state.infos && (
                <small>V: {this.state.infos.version} Rev: <a target="_blank" href={`https://github.com/THM-MoTE/webmodelica/commit/${this.state.infos.commitHash}`}>#{this.state.infos.commitHash}</a></small>)}
            </Col>
            <Col xs="auto">
              <small>
                <a target="_blank" href="https://github.com/thm-mote/webmodelica"><Octicon name="mark-github" />&nbsp;Repository</a>&nbsp;&nbsp;
                <a target="_blank" href="https://github.com/thm-mote/webmodelica/issues"><Octicon name="issue-opened" />&nbsp; Issues</a>&nbsp;&nbsp;
                <a target="_blank" href="/documentation.html"><Octicon name="book" />&nbsp; Doc</a>
              </small>
            </Col>
          </Row>
        </Container>
      </footer>
    )
  }
}
