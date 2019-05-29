import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import {
  Action,
  addOption,
  updateOption,
  deleteOption
} from '../redux/index'
import { Row, Col, Button, ButtonGroup, Form } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { AppState, Session, TableFormat, SimulateRequest, SimulationOption, availableSimulationOptions, simulationValuesFor } from '../models/index'
import { ApiClient } from '../services/api-client'
import * as R from 'ramda';

type Option = SimulationOption

interface Props {
  options: Option[]
  addOption(o:Option):void
  updateOption(idx:number, o:Option):void
  deleteOption(idx:number):void
  simulateClicked(ev:any):void
}
interface State {
}

class SimulationOptionsCon extends React.Component<Props, State> {
  constructor(p:Props) {
    super(p)
    this.state = {}
  }

  private updateName(idx:number, value:string): void {
    this.props.updateOption(idx, {...this.props.options[idx], name: value})
  }
  private updateValue(idx: number, value: string): void {
    this.props.updateOption(idx, { ...this.props.options[idx], value: value })
  }

  private addOptionField(): void {
    this.props.addOption({ name: "", value: "" })
  }
  private deleteOptionField = this.props.deleteOption

  render() {
    return (<>
      {this.props.options.map((opt, idx) =>  {
        const values = simulationValuesFor(opt.name)
        return (
          <Form.Row key={idx}>
            <Col sm={5}>
              <Form.Control as="select" placeholder="name" value={opt.name} onChange={(ev:any) => this.updateName(idx, ev.target.value)}>
                {availableSimulationOptions.map(o => (<option key={o.key}>{o.key}</option>))}
              </Form.Control>
            </Col>
            <Col sm={6}>
            { //if there are suggestions, provide a selector
              !R.isEmpty(values) &&
              (<Form.Control as="select" placeholder="value" value={opt.value.toString()} onChange={(ev: any) => this.updateValue(idx, ev.target.value)}>
                {values.map(v => (<option key={v}>{v}</option>))}
              </Form.Control>)
            }
            { //if there are no suggestions; provide a textfield
              R.isEmpty(values) && (<Form.Control placeholder="value" value={opt.value.toString()} onChange={(ev:any) => this.updateValue(idx, ev.target.value)}/>)
            }
            </Col>
            <Col sm={1}><Button variant="outline-danger" onClick={() => this.deleteOptionField(idx)}><Octicon name="x" /></Button></Col>
          </Form.Row>
        )
        })
      }
      <Row className="justify-content-center">
        <ButtonGroup className="col-sm-6">
          <Button onClick={this.addOptionField.bind(this)}><Octicon name="plus" /></Button>
          <Button variant="outline-success" onClick={this.props.simulateClicked}><Octicon name="rocket" /> Simulate</Button>
        </ButtonGroup>
      </Row>
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
