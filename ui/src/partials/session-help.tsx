import React from 'react'
import { Col, Row, ListGroup, Nav, SplitButton, ButtonGroup, Button, Modal, Form, Alert, Badge, Dropdown } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import * as R from 'ramda'

interface Props {
  onCloseClicked(): void
  display?:boolean
}

export class SessionHelp extends React.Component<Props, {}> {
  render() {
    return (
      <Modal show={this.props.display} onHide={this.props.onCloseClicked}>
        <Modal.Header closeButton>
          <Modal.Title>Help</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>We  are supporting shortcuts:</p>
          <ul>
            <li>Strg+Shift+B compiles the current file</li>
            <li>Strg+Shift+S saves the file</li>
          </ul>
        </Modal.Body>
      </Modal>
    )
  }
}
