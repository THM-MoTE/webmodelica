import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { WmContainer } from '../partials/container'
//@ts-ignore
import Octicon from 'react-octicon'
import { Row, Col, Button, ButtonGroup, Container as RContainer, Card } from 'react-bootstrap'
import { renderErrors } from '../partials/errors';
import * as R from 'ramda';
import { Action } from '../redux/actions'
import { AppState, Session } from '../models/index'

class SimulationPaneCon extends React.Component<any, any> {
  render() {
    return (
      <WmContainer title={"Session: " + this.props.session.project.name} active="simulation" sessionId={this.props.session.id}>
        <p> simulation year {this.props.session.id} </p>
      </WmContainer>
    )
  }
}

function mapProps(state: AppState) {
  return { session: state.session }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({}, dispatch)
}

const SimulationPane = connect(mapProps, dispatchToProps)(SimulationPaneCon)
export default SimulationPane
