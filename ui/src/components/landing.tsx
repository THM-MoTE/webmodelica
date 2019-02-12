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
                  <label>Email address</label>
                  <input type="email" className="form-control" id="exampleInputEmail1" aria-describedby="emailHelp" placeholder="Enter email" />
                  <small id="emailHelp" className="form-text text-muted">We'll never share your email with anyone else.</small>
                </div>
                <div className="form-group">
                  <label>Password</label>
                  <input type="password" className="form-control" id="exampleInputPassword1" placeholder="Password" />
                </div>
                <div className="form-group form-check">
                  <input type="checkbox" className="form-check-input" id="exampleCheck1" />
                  <label className="form-check-label">Check me out</label>
                </div>
                <button type="submit" className="btn btn-primary">Submit</button>
                </form>
              </div>
          </div>
        </div>
      </Container>
    )
  }
}
