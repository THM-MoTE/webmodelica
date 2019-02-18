import React, { Component } from 'react';
import { Container } from '../layouts'
import { Button, Form, Alert } from 'react-bootstrap'
import { ApiClient, defaultClient } from '../services/api-client'
import { Redirect } from 'react-router'
import { defaultMapDispatchToProps, mapAuthenticationToProps } from '../redux'
import { Action, login } from '../redux/index'
import { connect } from 'react-redux'
import * as R from 'ramda'

class LandingCon extends Component<any, any> {
  private username: string = ''
  private password: string = ''
  private api: ApiClient

  constructor(props: any) {
    super(props)
    this.api = defaultClient
    this.state = { errors: [] }
  }

  componentDidMount() {
    if (this.props.authentication)
      this.props.history.push("/projects")
  }

  private handleSubmit(ev: any) {
    const props = this.props
    console.log("name", this.username, "pw", this.password)
    const username = this.username
    const pw = this.password
    this.api.login(username, pw)
      .then(res => this.props.dispatch(login({ username: username, jwtToken: res.token })))
      .then(() => props.history.push('/projects'))
      .catch(err => {
        console.error("login error:", err)
        this.setState({ errors: [err] })
      })
    ev.preventDefault()
  }

  render() {
    const usernameChanged = (ev: any) => this.username = ev.target.value
    const passwordChanged = (ev: any) => this.password = ev.target.value
    return (<Container>
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
              {!R.isEmpty(this.state.errors) &&
                (<Alert variant="danger">
                  <p>{this.state.errors.join("\n")}</p>
                </Alert>)
              }
              <Button variant="primary" type="submit" onClick={this.handleSubmit.bind(this)}>
                Submit
                </Button>
            </Form>
          </div>
        </div>
      </div>
    </Container>)
  }
}

const Landing = connect(mapAuthenticationToProps, defaultMapDispatchToProps)(LandingCon)
export default Landing;
