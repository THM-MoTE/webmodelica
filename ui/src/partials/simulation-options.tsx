import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import {
  Action,
  addOption,
  updateOption,
  deleteOption
} from '../redux/index'
import { Row, Col, Button, Form } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { AppState, Session, TableFormat, SimulateRequest, SimulationOption, availableSimulationOptions } from '../models/index'
import { ApiClient } from '../services/api-client'
import * as R from 'ramda';

type Option = SimulationOption

interface Props {
  options: Option[]
  addOption(o:Option):void
  updateOption(idx:number, o:Option):void
  deleteOption(idx:number):void
}
interface State {
}

class SimulationOptionsCon extends React.Component<Props, State> {
  constructor(p:Props) {
    super(p)
  }

  private updateName(idx:number, value:string): void {
    this.props.updateOption(idx, {...this.props.options[idx], name: value})
  }
  private updateValue(idx: number, value: string): void {
    //convert string to number if it's a true float number
    //parseFloat returns NaN if it couldn't convert to float
    const f = parseFloat(value)
    const v = (!isNaN(f)) ? f : value
    this.props.updateOption(idx, { ...this.props.options[idx], value: v })
  }

  private addOptionField(): void {
    this.props.addOption({ name: "", value: "" })
  }
  private deleteOptionField = this.props.deleteOption

  render() {
    return (<>
      {this.props.options.map((opt, idx) => (
        <Form.Row key={idx}>
          <Col sm={4}>
            <Form.Control as="select" placeholder="name" value={opt.name} onChange={(ev:any) => this.updateName(idx, ev.target.value)}>
              {availableSimulationOptions.map(o => (<option key={o.key}>{o.key}</option>))}
            </Form.Control>
          </Col>
          <Col sm={6}><Form.Control placeholder="value" value={opt.value.toString()} onChange={(ev:any) => this.updateValue(idx, ev.target.value)}/></Col>
          <Col sm={2}><Button variant="outline-danger" onClick={() => this.deleteOptionField(idx)}><Octicon name="x" /></Button></Col>
        </Form.Row>
      ))}
      <Button onClick={this.addOptionField.bind(this)}><Octicon name="plus" /></Button>
      </>
    )
  }
}

function mapProps(state: AppState) {
  return {options: state.session!.simulation.options}
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({
    addOption,
    updateOption,
    deleteOption,
  }, dispatch)
}

const SimulationOptions = connect(mapProps, dispatchToProps)(SimulationOptionsCon)
export default SimulationOptions
