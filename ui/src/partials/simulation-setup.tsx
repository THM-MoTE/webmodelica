import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { Row, Col, Button, Form } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { AppState, Session, TableFormat, SimulateRequest } from '../models/index'
import { ApiClient } from '../services/api-client'
import { Action } from '../redux/actions'
import * as R from 'ramda';

interface Props {
  api: ApiClient
  simulate(sr: SimulateRequest):void
}
type State = any

class SimulationSetupCon extends React.Component<Props, State> {
  private modelName:string = ""


  private simulateClicked() {
    const opts = {
      numberOfIntervals: 10,
      stopTime: 5
    }
    this.props.simulate({modelName: this.modelName, options: opts})
  }

  render() {
    const modelNameChanged = (ev:any) => this.modelName = ev.target.value
    return (
      <Form.Row>
        <Col sm={10}><Form.Control placeholder="model to simulate" onChange={modelNameChanged}/></Col>
        <Col sm={2}><Button variant="outline-success" onClick={this.simulateClicked.bind(this)}><Octicon name="rocket" /> Simulate</Button></Col>
      </Form.Row>
    )
  }
}

function mapProps(state: AppState) {
  return {}
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({}, dispatch)
}

const SimulationSetup = connect(mapProps, dispatchToProps)(SimulationSetupCon)
export default SimulationSetup
