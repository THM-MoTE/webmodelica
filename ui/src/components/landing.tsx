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
import { userIsAuthenticated } from '../models';
import LoginComponent from '../partials/login-component';

class LandingCon extends Component<any, any> {
  render() {
    return (<WmContainer title="">
      <div className="row align-items-center">
        <LoginComponent api={this.props.api} history={this.props.history} />
      </div>
    </WmContainer>)
  }
}

const Landing = connect(mapAuthenticationToProps, defaultMapDispatchToProps)(LandingCon)
export default Landing;
