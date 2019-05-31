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

interface State {
  validated?:boolean
}

class SimulationSetupCon extends React.Component<Props, State> {
  private modelName:string = ""

  constructor(p:Props) {
    super(p)
    this.state = {}
  }

  private simulateClicked(ev:any) {
    const form = ev.currentTarget
    this.setState({validated: form.checkValidity()})
    if(form.checkValidity()) {
      this.props.parseSimulationOptions(this.props.options)
      const opts = R.fromPairs(
        this.props.options
          .filter(o => !R.empty(o.name.trim()))
          .map(o => R.pair(o.name, o.value))
        )
      this.props.simulate({modelName: this.modelName, options: opts})
    }
  }

  render() {
    const modelNameChanged = (ev:any) => this.modelName = ev.target.value
    return (<>
      <Form validated={this.state.validated}>
      <Form.Row>
        <Col sm={11}>
          <Form.Control placeholder="model to simulate" onChange={modelNameChanged} required/>
          <Form.Control.Feedback type="invalid">
            Provide a modelname!
          </Form.Control.Feedback>
        </Col>
        <Col sm={1}></Col>
      </Form.Row>
      <SimulationOptions simulateClicked={this.simulateClicked.bind(this)}/>
      </Form>
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
