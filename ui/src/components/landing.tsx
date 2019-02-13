import React, { Component } from 'react';
import {Container} from '../layouts'
import {Button, Form} from 'react-bootstrap'

export class Landing extends Component {
  private username:string = ''
  private password:string = ''

  constructor(props:any) {
    super(props)
  }

  private handleSubmit() {
    console.log("name", this.username, "pw", this.password)
  }

  render() {
    const usernameChanged = (ev:any) => this.username = ev.target.value
    const passwordChanged = (ev:any) => this.password = ev.target.value

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
