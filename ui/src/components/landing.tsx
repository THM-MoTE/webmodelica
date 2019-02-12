import React, { Component } from 'react';
import {Container} from '../layouts'

export class Landing extends Component {
  render() {
    return (
        <Container>
        <div className="row align-items-center">
          <div className="card sm-10 mx-auto">
            <h5 className="card-title">Login</h5>
            <div className="card-body">
              <form>
                <div className="form-group">
                  <label>Username</label>
                  <input className="form-control" id="username" aria-describedby="usernameHelp" placeholder="Enter username" />
                </div>
                <div className="form-group">
                  <label>Password</label>
                  <input type="password" className="form-control" id="exampleInputPassword1" placeholder="Password" />
                </div>
                <button type="submit" className="btn btn-primary">Login</button>
                </form>
              </div>
          </div>
        </div>
      </Container>
    )
  }
}
