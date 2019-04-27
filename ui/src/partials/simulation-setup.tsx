import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { Row, Col, Button, Form } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { AppState, Session, TableFormat, SimulateRequest, SimulationOption, availableSimulationOptions } from '../models/index'
import { ApiClient } from '../services/api-client'
import { Action, parseSimulationOptions } from '../redux/actions'
import * as R from 'ramda';
import SimulationOptions from './simulation-options'
import { strictEqual } from 'assert';

interface Props {
  api: ApiClient
  simulate(sr: SimulateRequest):void
  options: SimulationOption[]
  parseSimulationOptions(options:SimulationOption[]):void
}
type State = any

class SimulationSetupCon extends React.Component<Props, State> {
  private modelName:string = ""

  private simulateClicked() {
    this.props.parseSimulationOptions(this.props.options)
    const opts = R.fromPairs(
      this.props.options
        .filter(o => !R.empty(o.name.trim()))
        .map(o => R.pair(o.name, o.value))
      )
    this.props.simulate({modelName: this.modelName, options: opts})
  }

  render() {
    const modelNameChanged = (ev:any) => this.modelName = ev.target.value
    return (<>
      <Form.Row>
        <Col sm={10}><Form.Control placeholder="model to simulate" onChange={modelNameChanged}/></Col>
        <Col sm={2}><Button variant="outline-success" onClick={this.simulateClicked.bind(this)}><Octicon name="rocket" /> Simulate</Button></Col>
      </Form.Row>
      <SimulationOptions/>
      </>
    )
  }
}

function mapProps(state: AppState) {
  return { options: state.session!.simulation.options }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ parseSimulationOptions }, dispatch)
}

const SimulationSetup = connect(mapProps, dispatchToProps)(SimulationSetupCon)
export default SimulationSetup
