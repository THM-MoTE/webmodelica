import React, { Component } from 'react';
import {Container} from '../layouts'
import {Button, Form} from 'react-bootstrap'
import {ApiClient} from '../services/api-client'
import {Redirect} from 'react-router'

export class Landing extends Component<any,any> {
  private username:string = ''
  private password:string = ''
  private api:ApiClient

  constructor(props:any) {
    super(props)
    this.api = props.api
    this.state = {loggedIn: false}
  }

  private handleSubmit() {
    console.log("name", this.username, "pw", this.password)
    this.api.login(this.username, this.password)
      .then(res => console.log("response:", res))
      .then(() => this.setState({loggedIn: true}))
  }

  render() {
    const usernameChanged = (ev:any) => this.username = ev.target.value
    const passwordChanged = (ev:any) => this.password = ev.target.value
    if(this.state.loggedIn) {
      return (<Redirect to='/project-view' />)
    }
    else {
      return (
          <Container>
          <div className="row align-items-center">
            <div className="card sm-10 mx-auto">
              <h5 className="card-title">Login</h5>
              <div className="card-body">
                  <Form>
                    <Form.Group controlId="formUsername">
                      <Form.Label>Username</Form.Label>
                      <Form.Control placeholder="Enter username" onChange={usernameChanged}/>
                    </Form.Group>
                    <Form.Group controlId="formPassword">
                      <Form.Label>Password</Form.Label>
                      <Form.Control type="password" placeholder="Password" onChange={passwordChanged}/>
                    </Form.Group>
                    <Button variant="primary" onClick={this.handleSubmit.bind(this)}>
                      Submit
                    </Button>
                  </Form>
                </div>
            </div>
          </div>
        </Container>
      )
    }
  }
}
