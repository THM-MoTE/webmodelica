import React, { Component } from 'react';
import { WmContainer } from '../partials/container'
import { Button, Form, Alert, Row, Col } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { ApiClient } from '../services/api-client'
import { Redirect } from 'react-router'
import { defaultMapDispatchToProps, mapAuthenticationToProps } from '../redux'
import { Action } from '../redux/index'
import { renderErrors } from '../partials/errors'
import { connect } from 'react-redux'
import * as R from 'ramda'
import { userIsAuthenticated } from '../models';

export class NotFound extends React.Component<any, any> {
  render() {
    return (<WmContainer title="">
      <Row className="justify-content-md-center">
        <Col></Col>
        <Col md={8}>
          <h2>404 NotFound</h2>
          <h4>That's not what you wanted. There's only dust.</h4>
        </Col>
        <Col></Col>
      </Row>
      </WmContainer>
    )
  }
}
