import React, { Component } from 'react';
import { WmContainer } from '../partials/container'
import { Button, Form, Alert } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { ApiClient } from '../services/api-client'
import { Redirect } from 'react-router'
import { defaultMapDispatchToProps, mapAuthenticationToProps } from '../redux'
import { Action } from '../redux/index'
import { renderErrors } from '../partials/errors'
import { connect } from 'react-redux'
import * as R from 'ramda'
import { userIsAuthenticated, UserAuth } from '../models/index';

interface Props {
  api: ApiClient
  history: any
  authentication?: UserAuth
}

class LoginComponentCon extends React.Component<Props, any> {
  private username: string = ''
  private password: string = ''

  constructor(p:Props) {
    super(p)
    this.state = { errors: [] }
  }

  private handleSubmit(ev: any) {
    const props = this.props
    const username = this.username
    const pw = this.password
    props.api.login(username, pw)
      .then(() => props.history.push('/projects'))
      .catch(err => {
        this.setState({ errors: [err] })
      })
    ev.preventDefault()
  }

  componentDidMount() {
    if (userIsAuthenticated(this.props.authentication))
      this.props.history.push("/projects")
  }

  render() {
    const usernameChanged = (ev: any) => this.username = ev.target.value
    const passwordChanged = (ev: any) => this.password = ev.target.value
    return (
      <div className="card sm-10 mx-auto">
        <h5 className="card-title">Login</h5>
        <div className="card-body">
          <Form onSubmit={this.handleSubmit.bind(this)}>
            <Form.Group controlId="formUsername">
              <Form.Label>Username</Form.Label>
              <Form.Control required placeholder="Enter username" onChange={usernameChanged} />
            </Form.Group>
            <Form.Group controlId="formPassword">
              <Form.Label>Password</Form.Label>
              <Form.Control required type="password" placeholder="Password" onChange={passwordChanged} />
            </Form.Group>
            {renderErrors(this.state.errors)}
            <Button variant="secondary" href="http://localhost:9000/api/v1/auths/developer">Dev Login</Button>
            <Button variant="primary" type="submit">
              <Octicon name="sign-in" /> Submit
              </Button>
          </Form>
        </div>
      </div>
    )
  }
}

const LoginComponent = connect(mapAuthenticationToProps, defaultMapDispatchToProps)(LoginComponentCon)
export default LoginComponent
