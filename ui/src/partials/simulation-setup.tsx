import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { Row, Col, Button, Form } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { AppState, Session, File, TableFormat, SimulateRequest, SimulationOption, availableSimulationOptions } from '../models/index'
import { ApiClient } from '../services/api-client'
import { Action, parseSimulationOptions } from '../redux/actions'
import * as utils from '../utils'
import * as R from 'ramda';
import SimulationOptions from './simulation-options'
import { strictEqual } from 'assert';
import { EditorsPane } from '../components';

interface Props {
  api: ApiClient
  openFile?: File
  simulate(sr: SimulateRequest):void
  options: SimulationOption[]
  parseSimulationOptions(options:SimulationOption[]):void
}

interface State {
  validated?:boolean
}

const withinPackagePattern = /within\s+([\w\.]+)/m //extracts 'within a.b.c.examples'
const modelNamePattern = /(?:(?:model)|(?:class))\s+(\w+)/m //extracts the model name: 'model FullModel'
const experimentPattern = /experiment\((.+)\)/m //extracts 'experiment([experimentOptions])'
const experimentOptionsPattern = /(\w+)\s*\=\s*([\w\.\-]+)/g //extracts 'startTime = 0' from result of experimentPattern

class SimulationSetupCon extends React.Component<Props, State> {
  private modelName:string = ""

  constructor(p:Props) {
    super(p)
    this.state = {}
    this.experimentOptions()
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

  //greps the modelname inside of the currently open file
  private openedModelName(): string|undefined {
    if (this.props.openFile) {
      const content = this.props.openFile.content
      const withinMatch = withinPackagePattern.exec(content)
      const modelMatch = modelNamePattern.exec(content)
      const withinPrefix = (withinMatch) ? withinMatch[1]+'.' : ''
      const modelName = (modelMatch) ? withinPrefix+modelMatch[1] : ''
      console.log("within match: ", withinMatch, "modelMatch ", modelMatch)
      return modelName
    } else {
      return undefined
    }
  }

  private experimentOptions() {
    if(this.props.openFile) {
      const content = this.props.openFile.content
      const experimentMatch = experimentPattern.exec(content)
      let optionMatch = (experimentMatch) ? experimentOptionsPattern.exec(experimentMatch[1]) : undefined
      let parsedOptions:SimulationOption[] = []
      while(optionMatch) {
        const name = utils.downCaseString(optionMatch[1])
        const value = optionMatch[2]
        parsedOptions.push({name, value})
        optionMatch = experimentOptionsPattern.exec(experimentMatch![1])
      }
      console.log("options: ", parsedOptions)
      this.props.parseSimulationOptions(parsedOptions)
    }
  }

  render() {
    //make sure we set a model name, even if its taken from the opened file
    this.modelName = this.openedModelName() || ''
    const modelNameChanged = (ev:any) => this.modelName = ev.target.value
    return (<>
      <Form validated={this.state.validated}>
      <Form.Row>
        <Col sm={11}>
            <Form.Control placeholder="model to simulate" defaultValue={this.openedModelName()} onChange={modelNameChanged} required/>
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
  return { options: state.session!.simulation.options, openFile: state.session!.openedFile }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ parseSimulationOptions }, dispatch)
}

const SimulationSetup = connect(mapProps, dispatchToProps)(SimulationSetupCon)
export default SimulationSetup
