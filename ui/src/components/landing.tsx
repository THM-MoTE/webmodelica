import React, { Component } from 'react';
import { WmContainer } from '../partials/container'
import { Button, Form, Alert } from 'react-bootstrap'
import { ApiClient } from '../services/api-client'
import { Redirect } from 'react-router'
import { defaultMapDispatchToProps, mapAuthenticationToProps } from '../redux'
import { Action, login } from '../redux/index'
import { renderErrors } from '../partials/errors'
import { connect } from 'react-redux'
import * as R from 'ramda'

class LandingCon extends Component<any, any> {
  private username: string = ''
  private password: string = ''
  private api: ApiClient

  constructor(props: any) {
    super(props)
    this.api = this.props.api
    this.state = { errors: [] }
  }

  componentDidMount() {
    if (this.props.authentication)
      this.props.history.push("/projects")
  }

  private handleSubmit(ev: any) {
    const props = this.props
    const username = this.username
    const pw = this.password
    this.api.login(username, pw)
      .then(() => props.history.push('/projects'))
      .catch(err => {
        this.setState({ errors: [err] })
      })
    ev.preventDefault()
  }

  render() {
    const usernameChanged = (ev: any) => this.username = ev.target.value
    const passwordChanged = (ev: any) => this.password = ev.target.value
    return (<WmContainer title="">
      <div className="row align-items-center">
        <div className="card sm-10 mx-auto">
          <h5 className="card-title">Login</h5>
          <div className="card-body">
            <Form>
              <Form.Group controlId="formUsername">
                <Form.Label>Username</Form.Label>
                <Form.Control required placeholder="Enter username" onChange={usernameChanged} />
              </Form.Group>
              <Form.Group controlId="formPassword">
                <Form.Label>Password</Form.Label>
                <Form.Control required type="password" placeholder="Password" onChange={passwordChanged} />
              </Form.Group>
              {renderErrors(this.state.errors)}
              <Button variant="primary" type="submit" onClick={this.handleSubmit.bind(this)}>
                Submit
                </Button>
            </Form>
          </div>
        </div>
      </div>
    </WmContainer>)
  }
}

const Landing = connect(mapAuthenticationToProps, defaultMapDispatchToProps)(LandingCon)
export default Landing;
