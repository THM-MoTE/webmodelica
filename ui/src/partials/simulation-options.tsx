import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import {
  Action,
  addOption,
  updateOption,
  deleteOption
} from '../redux/index'
import { Row, Col, Button, ButtonGroup, Form, InputGroup } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { AppState, Session, TableFormat, SimulateRequest, SimulationOption, availableSimulationOptions, simulationValuesFor } from '../models/index'
import { ApiClient } from '../services/api-client'
import * as R from 'ramda';

type Option = SimulationOption

//indicates if we should render a select box or input field for the option's name
enum BoxType {
  Select,
  Input
}
function switchBoxType(tpe:BoxType) { return (tpe===BoxType.Input) ? BoxType.Select : BoxType.Input }

interface Props {
  options: Option[]
  addOption(o:Option):void
  updateOption(idx:number, o:Option):void
  deleteOption(idx:number):void
  simulateClicked(ev:any):void
}
interface State {
  optionBoxType:BoxType[]
}

class SimulationOptionsCon extends React.Component<Props, State> {
  constructor(p:Props) {
    super(p)
    this.state = {optionBoxType: R.repeat(BoxType.Select, p.options.length)}
  }

  private updateName(idx:number, value:string): void {
    this.props.updateOption(idx, {...this.props.options[idx], name: value})
  }
  private updateValue(idx: number, value: string): void {
    this.props.updateOption(idx, { ...this.props.options[idx], value: value })
  }

  private addOptionField(): void {
    const dublicate = this.state.optionBoxType.copyWithin(0,0)
    dublicate.push(BoxType.Select)
    this.setState({optionBoxType: dublicate})
    this.props.addOption({ name: "", value: "" })
  }
  private deleteOptionField(idx:number) {
    this.setState({optionBoxType: this.state.optionBoxType.filter((v, i) => i!==idx)})
    this.props.deleteOption(idx)
  }

  private isTextBoxType = (idx:number) => (idx < this.state.optionBoxType.length) && this.state.optionBoxType[idx] === BoxType.Input
  render() {
    //update the option's name input type
    const switchBoxTypeClicked = (idx:number) => {
      //search for idx and replace the value
      const optionBoxType = this.state.optionBoxType.map((v, i) => (i===idx) ? switchBoxType(v) : v)
      this.setState({optionBoxType})
    }
    return (<>
      {this.props.options.map((opt, idx) =>  {
        const values = simulationValuesFor(opt.name)
        return (
          <Form.Row key={idx}>
            <Form.Label column sm={1}>Option</Form.Label>
            <InputGroup as={Col} sm={5}>
              <InputGroup.Prepend><Button variant="secondary" onClick={() => switchBoxTypeClicked(idx)}><Octicon name={(this.isTextBoxType(idx)) ? 'three-bars' : 'pencil'} /></Button></InputGroup.Prepend>
              { this.isTextBoxType(idx) && (
                <Form.Control placeholder="name" value={opt.name} onChange={(ev: any) => this.updateName(idx, ev.target.value)} />
              )}
              { !this.isTextBoxType(idx) && (                
                <Form.Control as='select' placeholder="name" value={opt.name} onChange={(ev:any) => this.updateName(idx, ev.target.value)}>
                  {availableSimulationOptions.map(o => (<option key={o.key}>{o.key}</option>))}
                </Form.Control>)}
            </InputGroup>
            <Col sm={5}>
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
            <Col as={ButtonGroup} sm={1}>
                <Button variant="outline-danger" onClick={() => this.deleteOptionField(idx)}><Octicon name="x" /></Button>
            </Col>
          </Form.Row>
        )
        })
      }
      <Row className="justify-content-center">
        <ButtonGroup className="col-sm-4">
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
