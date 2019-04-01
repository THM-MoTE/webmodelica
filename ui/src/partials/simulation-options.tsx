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

interface Option {
  name: string
  value: number | string
}

interface Props {
}
interface State {
  options: Option[]
}

class SimulationOptionsCon extends React.Component<Props, State> {
  constructor(p:Props) {
    super(p)
    this.state = {options: [
      {name: "startTime", value: 0},
      {name: "stopTime", value: 5},
      {name: "numberOfIntervals", value: 500}
    ]}
  }

  private updateName(idx:number, value:string): void {
    const options = this.state.options.map((old, i) => {
      if(i === idx) return {...old, name: value}
      else return old
    })
    this.setState({options})
  }
  private updateValue(idx: number, value: string): void {
    const options = this.state.options.map((old, i) => {
      //convert string to number if it's a true float number
      //parseFloat returns NaN if it couldn't convert to float
      const f = parseFloat(value)
      const v = (!isNaN(f)) ? f : value
      if (i === idx) return { ...old, value: v }
      else return old
    })
    this.setState({ options })
  }

  private addOptionField(): void {
    const options = R.append({name: "", value: ""}, this.state.options)
    this.setState({ options})
  }

  render() {
    return (<>
      {this.state.options.map((opt, idx) => (
        <Form.Row key={idx}>
          <Col sm={4}><Form.Control placeholder="name" value={opt.name} onChange={(ev:any) => this.updateName(idx, ev.target.value)}/></Col>
          <Col sm={6}><Form.Control placeholder="value" value={opt.value.toString()} onChange={(ev:any) => this.updateValue(idx, ev.target.value)}/></Col>
          <Col sm={2}><Button variant="outline-danger"><Octicon name="x" /></Button></Col>
        </Form.Row>
      ))}
      <Button onClick={this.addOptionField.bind(this)}><Octicon name="plus" /></Button>
      </>
    )
  }
}

function mapProps(state: AppState) {
  return {}
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({}, dispatch)
}

const SimulationOptions = connect(mapProps, dispatchToProps)(SimulationOptionsCon)
export default SimulationOptions
